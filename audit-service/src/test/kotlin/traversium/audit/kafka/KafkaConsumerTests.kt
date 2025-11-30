package traversium.audit.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import traversium.audit.AuditApplication
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.db.repository.UserActivityRepository
import traversium.audit.security.MockFirebaseConfig
import traversium.audit.security.TestMultitenancyConfig
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Integration tests for Kafka consumer
 * @author Ozbej Pavc
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@SpringBootTest(
    classes = [
        AuditApplication::class,
        TestMultitenancyConfig::class,
        MockFirebaseConfig::class
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["test-audit-topic"], bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@ActiveProfiles("test")
@DirtiesContext
class KafkaConsumerTests @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val userActivityRepository: UserActivityRepository,
    private val tripActivityRepository: TripActivityRepository
) {

    @BeforeEach
    fun setup() {
        userActivityRepository.deleteAll()
        tripActivityRepository.deleteAll()
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun testKafkaConsumerProcessesUserActivity() {
        // Given: User activity stream data
        val testData = AuditStreamData(
            timestamp = OffsetDateTime.now(),
            userId = "user1",
            activityType = ActivityType.USER_ACTIVITY,
            action = "USER_CREATED",
            entityType = EntityType.USER,
            entityId = 123L,
            metadata = mapOf("email" to "test@example.com")
        )

        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(testData)

        // When: Sending message to Kafka
        kafkaTemplate.send("test-audit-topic", "key1", json)

        // Then: Should process and save the activity
        await().atMost(5, TimeUnit.SECONDS).until {
            userActivityRepository.count() >= 1
        }

        val savedActivity = userActivityRepository.findAll().first()
        assertThat(savedActivity.userId).isEqualTo("user1")
        assertThat(savedActivity.action).isEqualTo("USER_CREATED")
        assertThat(savedActivity.entityId).isEqualTo(123L)

        userActivityRepository.delete(savedActivity)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun testKafkaConsumerProcessesTripActivity() {
        // Given: Trip activity stream data
        val testData = AuditStreamData(
            timestamp = OffsetDateTime.now(),
            userId = "user1",
            activityType = ActivityType.TRIP_ACTIVITY,
            action = "TRIP_CREATED",
            entityType = EntityType.TRIP,
            tripId = 456L,
            metadata = mapOf("title" to "Summer Vacation")
        )

        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(testData)

        // When: Sending message to Kafka
        kafkaTemplate.send("test-audit-topic", "key1", json)

        // Then: Should process and save the activity
        await().atMost(5, TimeUnit.SECONDS).until {
            tripActivityRepository.count() >= 1
        }

        val savedActivity = tripActivityRepository.findAll().first()
        assertThat(savedActivity.userId).isEqualTo("user1")
        assertThat(savedActivity.tripId).isEqualTo(456L)
        assertThat(savedActivity.action).isEqualTo("TRIP_CREATED")
        assertThat(savedActivity.eventVersion).isEqualTo(1)

        tripActivityRepository.delete(savedActivity)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun testKafkaConsumerHandlesNullPayload() {
        // Given: Null payload
        val nullJson = "null"

        // When: Sending null message to Kafka
        kafkaTemplate.send("test-audit-topic", "key1", nullJson)

        // Then: Should not save anything (consumer should log warning and return)
        Thread.sleep(1000) // Give consumer time to process

        assertThat(userActivityRepository.count()).isEqualTo(0)
        assertThat(tripActivityRepository.count()).isEqualTo(0)
    }
}

