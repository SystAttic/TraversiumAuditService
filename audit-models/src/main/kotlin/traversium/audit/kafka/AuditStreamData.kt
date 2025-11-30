package traversium.audit.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

/**
 * Data class representing audit stream data received from Kafka
 * @author Ozbej Pavc
 */
data class AuditStreamData(
    @JsonProperty("timestamp")
    val timestamp: OffsetDateTime? = OffsetDateTime.now(),
    
    @JsonProperty("userId")
    val userId: String,
    
    @JsonProperty("activityType")
    val activityType: ActivityType,
    
    @JsonProperty("action")
    val action: String,
    
    @JsonProperty("entityType")
    val entityType: EntityType? = null,
    
    @JsonProperty("entityId")
    val entityId: Long? = null,
    
    @JsonProperty("tripId")
    val tripId: Long? = null,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any>? = null,
)

/**
 * Enum representing the type of activity being audited
 */
enum class ActivityType {
    USER_ACTIVITY,
    TRIP_ACTIVITY
}

/**
 * Enum representing the type of entity being audited
 */
enum class EntityType {
    USER,
    TRIP,
    ALBUM,
    MOMENT,
    PHOTO,
    COLLABORATOR,
    VIEWER
}

