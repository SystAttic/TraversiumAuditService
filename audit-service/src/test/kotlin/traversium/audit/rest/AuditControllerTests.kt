package traversium.audit.rest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import traversium.audit.AuditApplication
import traversium.audit.db.model.TripActivity
import traversium.audit.db.model.UserActivity
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.db.repository.UserActivityRepository
import traversium.audit.security.MockFirebaseConfig
import traversium.audit.security.TestMultitenancyConfig
import java.time.OffsetDateTime

/**
 * Integration tests for AuditController
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
@AutoConfigureMockMvc
@Transactional
@DirtiesContext
class AuditControllerTests @Autowired constructor(
    private val mockMvc: MockMvc,
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

    private fun MockMvc.performWithAuth(requestBuilder: MockHttpServletRequestBuilder): ResultActions =
        perform(requestBuilder.header("Authorization", "Bearer token1"))

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

        // When/Then: Getting user activities
        mockMvc.performWithAuth(get("/api/audit/user/user1/activities")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(5))
    }

    @Test
    fun getUserActivitiesFiltersByAction() {
        // Given: Activities with different actions
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_CREATED", timestamp = OffsetDateTime.now()))
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_DELETED", timestamp = OffsetDateTime.now()))
        userActivityRepository.save(UserActivity(userId = "user1", action = "USER_CREATED", timestamp = OffsetDateTime.now()))

        // When/Then: Getting activities filtered by action
        mockMvc.performWithAuth(get("/api/audit/user/user1/activities")
                .param("action", "USER_CREATED")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].action").value("USER_CREATED"))
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

        // When/Then: Getting trip activities
        mockMvc.performWithAuth(get("/api/audit/trip/456/activities")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(5))
    }

    @Test
    fun getRecentTripActivityReturnsActivitiesWithinWindow() {
        // Given: Activities within and outside window
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "RECENT", timestamp = now.minusDays(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "OLD", timestamp = now.minusDays(10), eventVersion = 2))

        // When/Then: Getting recent activity
        mockMvc.performWithAuth(get("/api/audit/trip/456/recent-activity")
                .param("days", "7")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].action").value("RECENT"))
    }

    @Test
    fun getCompleteEventHistoryReturnsAllEvents() {
        // Given: Multiple events
        repeat(3) { i ->
            tripActivityRepository.save(
                TripActivity(
                    tripId = 456L,
                    userId = "user1",
                    action = "EVENT_${i + 1}",
                    timestamp = OffsetDateTime.now().minusHours(i.toLong()),
                    eventVersion = i + 1
                )
            )
        }

        // When/Then: Getting complete event history
        mockMvc.performWithAuth(get("/api/audit/trip/456/event-history")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun revertTripToStateReturnsRevertResult() {
        // Given: Events for a trip
        val now = OffsetDateTime.now()
        val revertTo = now.minusHours(2)
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_1", timestamp = now.minusHours(3), eventVersion = 1))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_2", timestamp = now.minusHours(2), eventVersion = 2))
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT_3", timestamp = now.minusHours(1), eventVersion = 3))

        // When/Then: Reverting to a state
        mockMvc.performWithAuth(post("/api/audit/trip/456/revert")
                .param("revertToTimestamp", revertTo.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tripId").value(456))
            .andExpect(jsonPath("$.eventsToUndo.length()").value(1))
    }

    @Test
    fun revertTripToStateReturnsBadRequestForOldTimestamp() {
        // Given: Events
        val now = OffsetDateTime.now()
        tripActivityRepository.save(TripActivity(tripId = 456L, userId = "user1", action = "EVENT", timestamp = now, eventVersion = 1))

        // When/Then: Trying to revert to timestamp older than 7 days should return bad request
        mockMvc.performWithAuth(post("/api/audit/trip/456/revert")
                .param("revertToTimestamp", now.minusDays(10).toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
    }
}

