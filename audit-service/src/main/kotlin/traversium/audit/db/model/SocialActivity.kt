package traversium.audit.db.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/**
 * Entity representing social activity audit logs
 * Stores likes and comments on media within trips
 * @author Ozbej Pavc
 */
@Entity
@Table(name = SocialActivity.TABLE_NAME, indexes = [
    Index(name = "idx_social_activity_user_timestamp", columnList = "user_id, timestamp DESC"),
    Index(name = "idx_social_activity_user_action", columnList = "user_id, action"),
    Index(name = "idx_social_activity_media", columnList = "media_id, timestamp DESC"),
    Index(name = "idx_social_activity_trip_media", columnList = "trip_id, media_id, timestamp DESC")
])
data class SocialActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id", unique = true, nullable = false, updatable = false)
    val activityId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "action", nullable = false, length = 100)
    val action: String,

    @Column(name = "entity_type", length = 50)
    val entityType: String? = null,

    @Column(name = "entity_id")
    val entityId: Long? = null,

    @Column(name = "media_id")
    val mediaId: Long? = null,

    @Column(name = "trip_id")
    val tripId: Long? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    val metadata: String? = null,

    @Column(name = "timestamp", nullable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
) {
    companion object {
        const val TABLE_NAME = "social_activity"
    }
}

