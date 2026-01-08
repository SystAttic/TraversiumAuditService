package traversium.audit.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import traversium.audit.db.model.SocialActivity
import java.time.OffsetDateTime

/**
 * Repository for SocialActivity entities
 * @author Ozbej Pavc
 */
interface SocialActivityRepository : JpaRepository<SocialActivity, Long> {
    
    fun findByUserId(userId: String, pageable: Pageable): Page<SocialActivity>
    
    fun findByUserIdAndAction(userId: String, action: String, pageable: Pageable): Page<SocialActivity>
    
    fun findByMediaId(mediaId: Long, pageable: Pageable): Page<SocialActivity>
    
    fun findByTripId(tripId: Long, pageable: Pageable): Page<SocialActivity>
    
    fun findByUserIdAndTimestampBetween(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<SocialActivity>
    
    fun findByMediaIdAndTimestampBetween(
        mediaId: Long,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<SocialActivity>
    
    @Query("""
        SELECT sa FROM SocialActivity sa
        WHERE sa.userId = :userId
        AND sa.timestamp >= :since
        ORDER BY sa.timestamp DESC
    """)
    fun findRecentActivitiesByUser(
        @Param("userId") userId: String,
        @Param("since") since: OffsetDateTime
    ): List<SocialActivity>
    
    @Query("""
        SELECT sa FROM SocialActivity sa
        WHERE sa.timestamp >= :startTime
        AND sa.timestamp <= :endTime
        ORDER BY sa.timestamp ASC
    """)
    fun findAllByTimestampBetween(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): List<SocialActivity>

    @Query("SELECT COUNT(sa) FROM SocialActivity sa WHERE sa.action = :action")
    fun countByAction(@Param("action") action: String): Long

    @Query("SELECT COUNT(sa) FROM SocialActivity sa WHERE sa.action IN :actions")
    fun countByActionIn(@Param("actions") actions: List<String>): Long

    @Query("SELECT COUNT(sa) FROM SocialActivity sa WHERE sa.action IN :actions AND sa.timestamp >= :startDate AND sa.timestamp <= :endDate")
    fun countByActionInAndTimestampBetween(
        @Param("actions") actions: List<String>,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long

    @Query("SELECT COUNT(sa) FROM SocialActivity sa WHERE sa.timestamp >= :startDate AND sa.timestamp <= :endDate")
    fun countByTimestampBetween(
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long
}

