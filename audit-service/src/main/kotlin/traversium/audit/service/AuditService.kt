package traversium.audit.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.audit.db.model.FileStorageActivity
import traversium.audit.db.model.SocialActivity
import traversium.audit.db.model.TripActivity
import traversium.audit.db.model.UserActivity
import traversium.audit.db.repository.FileStorageActivityRepository
import traversium.audit.db.repository.SocialActivityRepository
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.db.repository.UserActivityRepository
import traversium.audit.kafka.AuditStreamData
import java.time.OffsetDateTime

/**
 * Service for handling audit events
 * @author Ozbej Pavc
 */
@Service
class AuditService(
    private val userActivityRepository: UserActivityRepository,
    private val tripActivityRepository: TripActivityRepository,
    private val socialActivityRepository: SocialActivityRepository,
    private val fileStorageActivityRepository: FileStorageActivityRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * Saves a user activity audit event
     */
    @Transactional
    fun saveUserActivity(streamData: AuditStreamData): UserActivity {
        val metadataJson = streamData.metadata?.let { objectMapper.writeValueAsString(it) }
        
        val userActivity = UserActivity(
            userId = streamData.userId,
            action = streamData.action,
            entityType = streamData.entityType?.name,
            entityId = streamData.entityId,
            metadata = metadataJson,
            timestamp = streamData.timestamp ?: OffsetDateTime.now()
        )
        
        return userActivityRepository.save(userActivity)
    }

    /**
     * Saves a trip activity audit event with Event Sourcing support
     */
    @Transactional
    fun saveTripActivity(streamData: AuditStreamData): TripActivity {
        val tripId = requireNotNull(streamData.tripId) { "Trip ID is required for trip activity" }
        
        val metadataJson = streamData.metadata?.let { objectMapper.writeValueAsString(it) }
        
        // Get the latest event version for this trip to maintain event ordering
        val latestEvent = tripActivityRepository
            .findByTripIdOrderByTimestampAscEventVersionAsc(tripId)
            .lastOrNull()
        
        val nextVersion = (latestEvent?.eventVersion ?: 0) + 1
        
        // Create state snapshot if this is a significant state change
        val stateSnapshot = createStateSnapshotIfNeeded(streamData, tripId, metadataJson)
        
        val tripActivity = TripActivity(
            tripId = tripId,
            userId = streamData.userId,
            action = streamData.action,
            entityType = streamData.entityType?.name,
            entityId = streamData.entityId,
            metadata = metadataJson,
            stateSnapshot = stateSnapshot,
            timestamp = streamData.timestamp ?: OffsetDateTime.now(),
            eventVersion = nextVersion
        )
        
        return tripActivityRepository.save(tripActivity)
    }

    /**
     * Creates a state snapshot for significant state changes
     * This helps with faster state reconstruction
     */
    private fun createStateSnapshotIfNeeded(
        streamData: AuditStreamData,
        tripId: Long,
        metadataJson: String?
    ): String? {
        // Create snapshots for major state changes
        val snapshotActions = setOf(
            "TRIP_CREATED",
            "TRIP_DELETED",
            "TRIP_VISIBILITY_CHANGED",
            "COLLABORATOR_ADDED",
            "COLLABORATOR_REMOVED",
            "VIEWER_ADDED",
            "VIEWER_REMOVED"
        )
        
        return if (snapshotActions.contains(streamData.action.uppercase())) {
            // Store the current state as JSON snapshot
            val snapshot = mapOf(
                "tripId" to tripId,
                "action" to streamData.action,
                "timestamp" to (streamData.timestamp ?: OffsetDateTime.now()).toString(),
                "metadata" to (streamData.metadata ?: emptyMap())
            )
            objectMapper.writeValueAsString(snapshot)
        } else {
            null
        }
    }

    // Query methods for UserActivity

    fun getUserActivities(userId: String, pageable: Pageable): Page<UserActivity> {
        return userActivityRepository.findByUserId(userId, pageable)
    }

    fun getUserActivitiesByAction(userId: String, action: String, pageable: Pageable): Page<UserActivity> {
        return userActivityRepository.findByUserIdAndAction(userId, action, pageable)
    }

    fun getUserActivitiesByTimeRange(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<UserActivity> {
        return userActivityRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime, pageable)
    }

    fun getUserActivitiesByActionAndTimeRange(
        userId: String,
        action: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<UserActivity> {
        // Use time range query and filter by action in memory
        // Can be optimized with a custom repository method if needed
        val byTimeRange = userActivityRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime, pageable)
        val filtered = byTimeRange.content.filter { it.action == action }
        return org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
    }

    // Query methods for TripActivity

    fun getTripActivities(tripId: Long, pageable: Pageable): Page<TripActivity> {
        return tripActivityRepository.findByTripId(tripId, pageable)
    }

    fun getTripActivitiesByAction(tripId: Long, action: String, pageable: Pageable): Page<TripActivity> {
        return tripActivityRepository.findByTripIdAndAction(tripId, action, pageable)
    }

    fun getTripActivitiesByTimeRange(
        tripId: Long,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<TripActivity> {
        return tripActivityRepository.findByTripIdAndTimestampBetween(tripId, startTime, endTime, pageable)
    }

    fun getTripActivitiesByActionAndTimeRange(
        tripId: Long,
        action: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<TripActivity> {
        // Use time range query and filter by action in memory
        // Can be optimized with a custom repository method if needed
        val byTimeRange = tripActivityRepository.findByTripIdAndTimestampBetween(tripId, startTime, endTime, pageable)
        val filtered = byTimeRange.content.filter { it.action == action }
        return org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
    }

    /**
     * Saves a social activity audit event
     */
    @Transactional
    fun saveSocialActivity(streamData: AuditStreamData): SocialActivity {
        val metadataJson = streamData.metadata?.let { objectMapper.writeValueAsString(it) }
        
        // Extract mediaId from metadata if available
        val mediaId = streamData.metadata?.get("mediaId")?.let {
            when (it) {
                is Number -> it.toLong()
                is String -> it.toLongOrNull()
                else -> null
            }
        }
        
        val socialActivity = SocialActivity(
            userId = streamData.userId,
            action = streamData.action,
            entityType = streamData.entityType?.name,
            entityId = streamData.entityId,
            mediaId = mediaId,
            tripId = streamData.tripId,
            metadata = metadataJson,
            timestamp = streamData.timestamp ?: OffsetDateTime.now()
        )
        
        return socialActivityRepository.save(socialActivity)
    }

    // Query methods for SocialActivity

    fun getSocialActivities(userId: String, pageable: Pageable): Page<SocialActivity> {
        return socialActivityRepository.findByUserId(userId, pageable)
    }

    fun getSocialActivitiesByAction(userId: String, action: String, pageable: Pageable): Page<SocialActivity> {
        return socialActivityRepository.findByUserIdAndAction(userId, action, pageable)
    }

    fun getSocialActivitiesByMedia(mediaId: Long, pageable: Pageable): Page<SocialActivity> {
        return socialActivityRepository.findByMediaId(mediaId, pageable)
    }

    fun getSocialActivitiesByTrip(tripId: Long, pageable: Pageable): Page<SocialActivity> {
        return socialActivityRepository.findByTripId(tripId, pageable)
    }

    fun getSocialActivitiesByTimeRange(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<SocialActivity> {
        return socialActivityRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime, pageable)
    }


    /**
     * Saves a social activity audit event
     */
    @Transactional
    fun saveFileStorageActivity(streamData: AuditStreamData): FileStorageActivity {
        val metadataJson = streamData.metadata?.let { objectMapper.writeValueAsString(it) }

        // Extract filename from metadata
        val filename = streamData.metadata?.get("filename")?.let {
            when (it) {
                is String -> it
                else -> null
            }
        }

        val fileStorageActivity = FileStorageActivity(
            userId = streamData.userId,
            action = streamData.action,
            entityType = streamData.entityType?.name,
            entityId = filename,
            metadata = metadataJson,
            timestamp = streamData.timestamp ?: OffsetDateTime.now()
        )

        return fileStorageActivityRepository.save(fileStorageActivity)
    }

    // Query methods for FileStorageActivity

    fun getFileStorageActivities(userId: String, pageable: Pageable): Page<FileStorageActivity> {
        return fileStorageActivityRepository.findByUserId(userId, pageable)
    }

    fun getFileStorageActivitiesByAction(userId: String, action: String, pageable: Pageable): Page<FileStorageActivity> {
        return fileStorageActivityRepository.findByUserIdAndAction(userId, action, pageable)
    }

    fun getFileStorageActivitiesByTimeRange(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<FileStorageActivity> {
        return fileStorageActivityRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime, pageable)
    }


    // Methods for backup service - fetch all activities by date range

    fun getAllUserActivitiesByDateRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): List<UserActivity> {
        return userActivityRepository.findAllByTimestampBetween(startTime, endTime)
    }

    fun getAllTripActivitiesByDateRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): List<TripActivity> {
        return tripActivityRepository.findAllByTimestampBetween(startTime, endTime)
    }

    fun getAllSocialActivitiesByDateRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): List<SocialActivity> {
        return socialActivityRepository.findAllByTimestampBetween(startTime, endTime)
    }

    fun getAllFileStorageActivitiesByDateRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): List<FileStorageActivity> {
        return fileStorageActivityRepository.findAllByTimestampBetween(startTime, endTime)
    }
}

