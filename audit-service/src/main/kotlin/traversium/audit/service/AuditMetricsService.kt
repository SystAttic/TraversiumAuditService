package traversium.audit.service

import org.springframework.stereotype.Service
import traversium.audit.db.repository.FileStorageActivityRepository
import traversium.audit.db.repository.SocialActivityRepository
import traversium.audit.db.repository.TripActivityRepository
import traversium.audit.db.repository.UserActivityRepository
import traversium.audit.kafka.UserActivityAction
import traversium.audit.kafka.TripActivityAction
import traversium.audit.kafka.SocialActivityAction
import traversium.audit.kafka.FileStorageActivityAction
import traversium.commonmultitenancy.TenantContext
import java.time.OffsetDateTime

/**
 * Service for providing audit metrics via gRPC
 * This service reads from the audit database tables
 */
@Service
class AuditMetricsService(
    private val userActivityRepository: UserActivityRepository,
    private val tripActivityRepository: TripActivityRepository,
    private val socialActivityRepository: SocialActivityRepository,
    private val fileStorageActivityRepository: FileStorageActivityRepository
) {

    fun getTotalUsersCreated(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            userActivityRepository.countByAction(UserActivityAction.USER_CREATED.name)
        }
    }

    fun getActiveUsers(tenantId: String, days: Int): Long {
        val cutoffDate = OffsetDateTime.now().minusDays(days.toLong())
        return executeInTenantContext(tenantId) {
            userActivityRepository.countDistinctUserIdByTimestampAfter(cutoffDate)
        }
    }

    fun getNewUsersInPeriod(tenantId: String, startDate: OffsetDateTime, endDate: OffsetDateTime): Long {
        return executeInTenantContext(tenantId) {
            userActivityRepository.countByActionAndTimestampBetween(
                UserActivityAction.USER_CREATED.name,
                startDate,
                endDate
            )
        }
    }

    fun getTotalTripsCreated(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            tripActivityRepository.countByAction(TripActivityAction.TRIP_CREATED.name)
        }
    }

    fun getTripsCreatedInPeriod(tenantId: String, startDate: OffsetDateTime, endDate: OffsetDateTime): Long {
        return executeInTenantContext(tenantId) {
            tripActivityRepository.countByActionAndTimestampBetween(
                TripActivityAction.TRIP_CREATED.name,
                startDate,
                endDate
            )
        }
    }

    fun getTotalMediaUploaded(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            tripActivityRepository.countByAction(TripActivityAction.MEDIA_UPLOADED.name)
        }
    }

    fun getMediaUploadedInPeriod(tenantId: String, startDate: OffsetDateTime, endDate: OffsetDateTime): Long {
        return executeInTenantContext(tenantId) {
            tripActivityRepository.countByActionAndTimestampBetween(
                TripActivityAction.MEDIA_UPLOADED.name,
                startDate,
                endDate
            )
        }
    }

    fun getTotalStorageBytes(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            // This is a placeholder - actual implementation would need to extract file size from metadata
            // For now, estimate based on media count (assume average 5MB per file)
            getTotalMediaUploaded(tenantId) * 5 * 1024 * 1024
        }
    }

    fun getTotalSocialInteractions(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            socialActivityRepository.countByActionIn(
                listOf(
                    SocialActivityAction.LIKE_CREATED.name,
                    SocialActivityAction.COMMENT_CREATED.name,
                    SocialActivityAction.COMMENT_REPLY.name
                )
            )
        }
    }

    fun getTotalLikes(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            socialActivityRepository.countByAction(SocialActivityAction.LIKE_CREATED.name)
        }
    }

    fun getTotalComments(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            socialActivityRepository.countByActionIn(
                listOf(
                    SocialActivityAction.COMMENT_CREATED.name,
                    SocialActivityAction.COMMENT_REPLY.name
                )
            )
        }
    }

    fun getSocialInteractionsInPeriod(tenantId: String, startDate: OffsetDateTime, endDate: OffsetDateTime): Long {
        return executeInTenantContext(tenantId) {
            socialActivityRepository.countByActionInAndTimestampBetween(
                listOf(
                    SocialActivityAction.LIKE_CREATED.name,
                    SocialActivityAction.COMMENT_CREATED.name,
                    SocialActivityAction.COMMENT_REPLY.name
                ),
                startDate,
                endDate
            )
        }
    }

    fun getTotalApiCalls(tenantId: String): Long {
        return executeInTenantContext(tenantId) {
            val userCount = userActivityRepository.count()
            val tripCount = tripActivityRepository.count()
            val socialCount = socialActivityRepository.count()
            val fileCount = fileStorageActivityRepository.count()
            userCount + tripCount + socialCount + fileCount
        }
    }

    fun getApiCallsInPeriod(tenantId: String, startDate: OffsetDateTime, endDate: OffsetDateTime): Long {
        return executeInTenantContext(tenantId) {
            val userCount = userActivityRepository.countByTimestampBetween(startDate, endDate)
            val tripCount = tripActivityRepository.countByTimestampBetween(startDate, endDate)
            val socialCount = socialActivityRepository.countByTimestampBetween(startDate, endDate)
            val fileCount = fileStorageActivityRepository.countByTimestampBetween(startDate, endDate)
            userCount + tripCount + socialCount + fileCount
        }
    }

    private fun <T> executeInTenantContext(tenantId: String, action: () -> T): T {
        val currentTenant = TenantContext.getTenant()
        TenantContext.setTenant(tenantId)
        try {
            return action()
        } finally {
            TenantContext.setTenant(currentTenant)
        }
    }
}

