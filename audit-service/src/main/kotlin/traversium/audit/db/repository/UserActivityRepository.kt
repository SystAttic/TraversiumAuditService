package traversium.audit.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import traversium.audit.db.model.UserActivity
import java.time.OffsetDateTime

/**
 * Repository for UserActivity entities
 * @author Ozbej Pavc
 */
interface UserActivityRepository : JpaRepository<UserActivity, Long> {
    
    fun findByUserId(userId: String, pageable: Pageable): Page<UserActivity>
    
    fun findByUserIdAndAction(userId: String, action: String, pageable: Pageable): Page<UserActivity>
    
    fun findByUserIdAndTimestampBetween(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<UserActivity>
    
    @Query("""
        SELECT ua FROM UserActivity ua
        WHERE ua.userId = :userId
        AND ua.timestamp >= :since
        ORDER BY ua.timestamp DESC
    """)
    fun findRecentActivitiesByUser(
        @Param("userId") userId: String,
        @Param("since") since: OffsetDateTime
    ): List<UserActivity>
    
    @Query("""
        SELECT ua FROM UserActivity ua
        WHERE ua.timestamp >= :startTime
        AND ua.timestamp <= :endTime
        ORDER BY ua.timestamp ASC
    """)
    fun findAllByTimestampBetween(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): List<UserActivity>

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.action = :action")
    fun countByAction(@Param("action") action: String): Long

    @Query("SELECT COUNT(DISTINCT ua.userId) FROM UserActivity ua WHERE ua.timestamp >= :cutoffDate")
    fun countDistinctUserIdByTimestampAfter(@Param("cutoffDate") cutoffDate: OffsetDateTime): Long

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.action = :action AND ua.timestamp >= :startDate AND ua.timestamp <= :endDate")
    fun countByActionAndTimestampBetween(
        @Param("action") action: String,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.timestamp >= :startDate AND ua.timestamp <= :endDate")
    fun countByTimestampBetween(
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long
}

