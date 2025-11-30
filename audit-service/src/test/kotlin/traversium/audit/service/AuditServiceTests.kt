package traversium.audit.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import traversium.audit.AuditApplication
import traversium.audit.db.model.TripActivity
import traversium.audit.db.model.UserActivity
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.db.repository.UserActivityRepository
import traversium.audit.kafka.ActivityType
import traversium.audit.kafka.AuditStreamData
import traversium.audit.kafka.EntityType
import traversium.audit.security.MockFirebaseConfig
import traversium.audit.security.TestMultitenancyConfig
import java.time.OffsetDateTime

/**
 * Integration tests for AuditService
 * @author Ozbej Pavc
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@SpringBootTest(
    classes = [
        AuditApplication::class,
        MockFirebaseConfig::class,
        TestMultitenancyConfig::class
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
@DirtiesContext
class AuditServiceTests @Autowired constructor(
    private val auditService: AuditService,
    private val userActivityRepository: UserActivityRepository,
    private val tripActivityRepository: TripActivityRepository,
    private val firebaseConfig: MockFirebaseConfig
) {

    @BeforeEach
    fun setup() {
        userActivityRepository.deleteAll()
        tripActivityRepository.deleteAll()

        firebaseConfig.setTokenData("token1", "user1", "mail@example.com")
    }

    @Test
    fun saveUserActivitySavesCorrectly() {
        // Given: User activity stream data
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.USER_ACTIVITY,
            action = "USER_CREATED",
            entityType = EntityType.USER,
            entityId = 123L,
            metadata = mapOf("email" to "test@example.com", "name" to "Test User"),
            timestamp = OffsetDateTime.now()
        )

        // When: Saving user activity
        val saved = auditService.saveUserActivity(streamData)

        // Then: Should be saved correctly
        assertNotNull(saved.activityId)
        assertEquals("user1", saved.userId)
        assertEquals("USER_CREATED", saved.action)
        assertEquals("USER", saved.entityType)
        assertEquals(123L, saved.entityId)
        assertNotNull(saved.metadata)
        assertNotNull(saved.timestamp)
    }

    @Test
    fun saveUserActivityHandlesNullMetadata() {
        // Given: User activity without metadata
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.USER_ACTIVITY,
            action = "USER_DELETED",
            timestamp = OffsetDateTime.now()
        )

        // When: Saving user activity
        val saved = auditService.saveUserActivity(streamData)

        // Then: Should save with null metadata
        assertEquals("user1", saved.userId)
        assertEquals("USER_DELETED", saved.action)
        assertNull(saved.metadata)
    }

    @Test
    fun saveTripActivitySavesCorrectly() {
        // Given: Trip activity stream data
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "TRIP_CREATED",
            entityType = EntityType.TRIP,
            tripId = 456L,
            metadata = mapOf("title" to "Summer Vacation", "visibility" to "PUBLIC"),
            timestamp = OffsetDateTime.now()
        )

        // When: Saving trip activity
        val saved = auditService.saveTripActivity(streamData)

        // Then: Should be saved correctly
        assertNotNull(saved.activityId)
        assertEquals(456L, saved.tripId)
        assertEquals("user1", saved.userId)
        assertEquals("TRIP_CREATED", saved.action)
        assertEquals("TRIP", saved.entityType)
        assertEquals(1, saved.eventVersion)
        assertNotNull(saved.stateSnapshot) // TRIP_CREATED should create a snapshot
        assertNotNull(saved.timestamp)
    }

    @Test
    fun saveTripActivityIncrementsEventVersion() {
        // Given: First trip activity
        val firstStreamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "TRIP_CREATED",
            tripId = 456L,
            timestamp = OffsetDateTime.now()
        )
        val first = auditService.saveTripActivity(firstStreamData)

        // When: Saving second activity for same trip
        val secondStreamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "PHOTO_ADDED",
            tripId = 456L,
            timestamp = OffsetDateTime.now()
        )
        val second = auditService.saveTripActivity(secondStreamData)

        // Then: Event version should increment
        assertEquals(1, first.eventVersion)
        assertEquals(2, second.eventVersion)
    }

    @Test
    fun saveTripActivityThrowsExceptionWhenTripIdIsNull() {
        // Given: Trip activity without tripId
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "TRIP_CREATED",
            tripId = null,
            timestamp = OffsetDateTime.now()
        )

        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException::class.java) {
            auditService.saveTripActivity(streamData)
        }
    }

    @Test
    fun saveTripActivityCreatesSnapshotForMajorStateChanges() {
        // Given: Major state change action
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "TRIP_VISIBILITY_CHANGED",
            tripId = 456L,
            metadata = mapOf("oldVisibility" to "PRIVATE", "newVisibility" to "PUBLIC"),
            timestamp = OffsetDateTime.now()
        )

        // When: Saving trip activity
        val saved = auditService.saveTripActivity(streamData)

        // Then: Should have state snapshot
        assertNotNull(saved.stateSnapshot)
        assertTrue(saved.stateSnapshot!!.contains("tripId"))
        assertTrue(saved.stateSnapshot!!.contains("TRIP_VISIBILITY_CHANGED"))
    }

    @Test
    fun saveTripActivityDoesNotCreateSnapshotForMinorChanges() {
        // Given: Minor change action
        val streamData = AuditStreamData(
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "PHOTO_ADDED",
            tripId = 456L,
            metadata = mapOf("photoId" to "789"),
            timestamp = OffsetDateTime.now()
        )

        // When: Saving trip activity
        val saved = auditService.saveTripActivity(streamData)

        // Then: Should not have state snapshot
        assertNull(saved.stateSnapshot)
    }

    @Test
    fun getUserActivitiesReturnsPaginatedResults() {
        // Given: Multiple user activities
        repeat(5) { i ->
            userActivityRepository.save(
                UserActivity(
                    userId = "user1",
                    action = "ACTION_$i",
                    timestamp = OffsetDateTime.now().minusHours(i.toLong())
                )
            )
        }

        // When: Getting activities with pagination
        val page1 = auditService.getUserActivities("user1", PageRequest.of(0, 2))
        val page2 = auditService.getUserActivities("user1", PageRequest.of(1, 2))

        // Then: Should return paginated results
        assertEquals(2, page1.content.size)
        assertEquals(2, page2.content.size)
        assertEquals(5, page1.totalElements)
    }

    @Test
    fun getUserActivitiesByActionFiltersCorrectly() {
        // Given: Activities with different actions
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_CREATED", timestamp = OffsetDateTime.now()))
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_DELETED", timestamp = OffsetDateTime.now()))
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_CREATED", timestamp = OffsetDateTime.now()))

        // When: Getting activities by action
        val result = auditService.getUserActivitiesByAction("user1", "USER_CREATED", PageRequest.of(0, 10))

        // Then: Should return only matching activities
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.action == "USER_CREATED" })
    }

    @Test
    fun getTripActivitiesReturnsPaginatedResults() {
        // Given: Multiple trip activities
        repeat(5) { i ->
            tripActivityRepository.save(
                TripActivity(
                    tripId = 456L,
                    userId = "user1",
                    action = "ACTION_$i",
                    timestamp = OffsetDateTime.now().minusHours(i.toLong()),
                    eventVersion = i + 1
                )
            )
        }

        // When: Getting activities with pagination
        val page1 = auditService.getTripActivities(456L, PageRequest.of(0, 2))
        val page2 = auditService.getTripActivities(456L, PageRequest.of(1, 2))

        // Then: Should return paginated results
        assertEquals(2, page1.content.size)
        assertEquals(2, page2.content.size)
        assertEquals(5, page1.totalElements)
    }

    @Test
    fun getTripActivitiesByActionFiltersCorrectly() {
        // Given: Activities with different actions
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "TRIP_CREATED", timestamp = OffsetDateTime.now(), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "PHOTO_ADDED", timestamp = OffsetDateTime.now(), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "TRIP_CREATED", timestamp = OffsetDateTime.now(), eventVersion = 3))

        // When: Getting activities by action
        val result = auditService.getTripActivitiesByAction(456L, "TRIP_CREATED", PageRequest.of(0, 10))

        // Then: Should return only matching activities
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.action == "TRIP_CREATED" })
    }

    @Test
    fun getTripActivitiesByTimeRangeFiltersCorrectly() {
        // Given: Activities at different times
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "ACTION_1", timestamp = now.minusHours(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "ACTION_2", timestamp = now.minusHours(2), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "ACTION_3", timestamp = now.minusHours(1), eventVersion = 3))

        // When: Getting activities in time range
        val result = auditService.getTripActivitiesByTimeRange(
            456L,
            now.minusHours(2).minusMinutes(30),
            now.minusMinutes(30),
            PageRequest.of(0, 10)
        )

        // Then: Should return only activities in range
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { 
            it.timestamp.isAfter(now.minusHours(2).minusMinutes(30)) && 
            it.timestamp.isBefore(now.minusMinutes(30))
        })
    }
}

