package traversium.audit.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import traversium.audit.AuditApplication
import traversium.audit.db.model.TripActivity
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.security.MockFirebaseConfig
import traversium.audit.security.TestMultitenancyConfig
import java.time.OffsetDateTime

/**
 * Integration tests for TripEventSourcingService
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
class TripEventSourcingServiceTests @Autowired constructor(
    private val tripEventSourcingService: TripEventSourcingService,
    private val tripActivityRepository: TripActivityRepository,
    private val firebaseConfig: MockFirebaseConfig
) {

    @BeforeEach
    fun setup() {
        tripActivityRepository.deleteAll()
        firebaseConfig.setTokenData("token1", "user1", "mail@example.com")
    }

    @Test
    fun getRecentActivityReturnsActivitiesWithinWindow() {
        // Given: Activities within and outside 7-day window
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "RECENT_1", timestamp = now.minusDays(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "RECENT_2", timestamp = now.minusDays(5), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "OLD", timestamp = now.minusDays(10), eventVersion = 3))

        // When: Getting recent activity
        val recent = tripEventSourcingService.getRecentActivity(456L, 7)

        // Then: Should return only activities within window
        assertEquals(2, recent.size)
        assertTrue(recent.all { it.timestamp.isAfter(now.minusDays(7)) })
    }

    @Test
    fun getRecentActivityReturnsEmptyListWhenNoActivities() {
        // When: Getting recent activity for trip with no activities
        val recent = tripEventSourcingService.getRecentActivity(456L, 7)

        // Then: Should return empty list
        assertTrue(recent.isEmpty())
    }

    @Test
    fun getCompleteEventHistoryReturnsAllEventsInOrder() {
        // Given: Multiple events for a trip
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_1", timestamp = now.minusHours(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_2", timestamp = now.minusHours(2), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_3", timestamp = now.minusHours(1), eventVersion = 3))
        tripActivityRepository.save(TripActivity(tripId = 789L, userId = "user1", action = "OTHER_TRIP", timestamp = now, eventVersion = 1))

        // When: Getting complete event history
        val history = tripEventSourcingService.getCompleteEventHistory(456L)

        // Then: Should return all events for the trip in order
        assertEquals(3, history.size)
        assertEquals("EVENT_1", history[0].action)
        assertEquals("EVENT_2", history[1].action)
        assertEquals("EVENT_3", history[2].action)
    }

    @Test
    fun getEventsSinceReturnsEventsAfterTimestamp() {
        // Given: Events at different times
        val now = OffsetDateTime.now()
        val cutoff = now.minusHours(2)
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "BEFORE", timestamp = now.minusHours(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "AFTER_1", timestamp = now.minusHours(1), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "AFTER_2", timestamp = now, eventVersion = 3))

        // When: Getting events since cutoff
        val events = tripEventSourcingService.getEventsSince(456L, cutoff)

        // Then: Should return only events after cutoff
        assertEquals(2, events.size)
        assertTrue(events.all { it.timestamp.isAfter(cutoff) || it.timestamp.isEqual(cutoff) })
        assertTrue(events.any { it.action == "AFTER_1" })
        assertTrue(events.any { it.action == "AFTER_2" })
    }

    @Test
    fun getEventsUntilReturnsEventsBeforeTimestamp() {
        // Given: Events at different times
        val now = OffsetDateTime.now()
        val cutoff = now.minusHours(1)
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "BEFORE_1", timestamp = now.minusHours(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "BEFORE_2", timestamp = now.minusHours(2), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "AFTER", timestamp = now, eventVersion = 3))

        // When: Getting events until cutoff
        val events = tripEventSourcingService.getEventsUntil(456L, cutoff)

        // Then: Should return only events before or at cutoff
        assertEquals(2, events.size)
        assertTrue(events.all { it.timestamp.isBefore(cutoff) || it.timestamp.isEqual(cutoff) })
        assertTrue(events.any { it.action == "BEFORE_1" })
        assertTrue(events.any { it.action == "BEFORE_2" })
    }

    @Test
    fun revertTripToStateThrowsExceptionForOldTimestamp() {
        // Given: Activities older than 7 days
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "OLD_EVENT", timestamp = now.minusDays(10), eventVersion = 1))

        // When/Then: Trying to revert to timestamp older than 7 days should throw exception
        assertThrows(IllegalArgumentException::class.java) {
            tripEventSourcingService.revertTripToState(456L, now.minusDays(10))
        }
    }

    @Test
    fun revertTripToStateThrowsExceptionForFutureTimestamp() {
        // Given: Current activities
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT", timestamp = now, eventVersion = 1))

        // When/Then: Trying to revert to future timestamp should throw exception
        assertThrows(IllegalArgumentException::class.java) {
            tripEventSourcingService.revertTripToState(456L, now.plusDays(1))
        }
    }

    @Test
    fun revertTripToStateReturnsCorrectResult() {
        // Given: Multiple events for a trip
        val now = OffsetDateTime.now()
        val revertTo = now.minusHours(2)
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_1", timestamp = now.minusHours(3), eventVersion = 1, stateSnapshot = "snapshot1"))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_2", timestamp = now.minusHours(2), eventVersion = 2, stateSnapshot = "snapshot2"))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_3", timestamp = now.minusHours(1), eventVersion = 3))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_4", timestamp = now, eventVersion = 4))

        // When: Reverting to a timestamp
        val result = tripEventSourcingService.revertTripToState(456L, revertTo)

        // Then: Should return correct result
        assertEquals(456L, result.tripId)
        assertEquals(revertTo, result.revertToTimestamp)
        assertNotNull(result.targetStateSnapshot)
        assertEquals(2, result.eventsToUndo.size) // EVENT_3 and EVENT_4 should be undone
        assertTrue(result.eventsToUndo.all { it.timestamp.isAfter(revertTo) })
    }

    @Test
    fun revertTripToStateThrowsExceptionWhenNoEventsFound() {
        // Given: No events for the trip

        // When/Then: Trying to revert should throw exception
        assertThrows(IllegalStateException::class.java) {
            tripEventSourcingService.revertTripToState(456L, OffsetDateTime.now().minusHours(1))
        }
    }
}

