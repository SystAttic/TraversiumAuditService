package traversium.audit.db.model

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * Entity representing trip activity audit logs with Event Sourcing support
 * Stores events that can be used to reconstruct trip state
 * @author Ozbej Pavc
 */
@Entity
@Table(name = TripActivity.TABLE_NAME, indexes = [
    Index(name = "idx_trip_activity_trip_timestamp", columnList = "trip_id, timestamp DESC"),
    Index(name = "idx_trip_activity_trip_action", columnList = "trip_id, action"),
    Index(name = "idx_trip_activity_user_trip", columnList = "user_id, trip_id, timestamp DESC")
])
data class TripActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id", unique = true, nullable = false, updatable = false)
    val activityId: Long? = null,

    @Column(name = "trip_id", nullable = false)
    val tripId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "action", nullable = false, length = 100)
    val action: String,

    @Column(name = "entity_type", length = 50)
    val entityType: String? = null,

    @Column(name = "entity_id")
    val entityId: Long? = null,

    @Column(name = "metadata", length = 10000)
    val metadata: String? = null,

    @Column(name = "state_snapshot", length = 10000)
    val stateSnapshot: String? = null,

    @Column(name = "timestamp", nullable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "event_version", nullable = false)
    val eventVersion: Int = 1,
) {
    companion object {
        const val TABLE_NAME = "trip_activity"
        const val REVERT_WINDOW_DAYS = 7
    }
}

