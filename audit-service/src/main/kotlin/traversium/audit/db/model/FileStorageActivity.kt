package traversium.audit.db.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/**
 * Entity representing file storage activity audit logs
 * @author Jure Zupancic
 */
@Entity
@Table(name = FileStorageActivity.TABLE_NAME, indexes = [
    Index(name = "idx_filestorage_activity_user_timestamp", columnList = "user_id, timestamp DESC"),
    Index(name = "idx_filestorage_activity_user_action", columnList = "user_id, action")
])
data class FileStorageActivity(
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

    @Column(name = "entity_id", length = 100)
    val entityId: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    val metadata: String? = null,

    @Column(name = "timestamp", nullable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
) {
    companion object {
        const val TABLE_NAME = "filestorage_activity"
    }
}