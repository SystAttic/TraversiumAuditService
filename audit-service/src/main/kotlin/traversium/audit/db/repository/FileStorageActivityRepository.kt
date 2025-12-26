package traversium.audit.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import traversium.audit.db.model.FileStorageActivity
import java.time.OffsetDateTime

/**
 * Repository for FileStorageActivity entities
 * @author Jure Zupancic
 */
interface FileStorageActivityRepository : JpaRepository<FileStorageActivity, Long> {

    fun findByUserId(userId: String, pageable: Pageable): Page<FileStorageActivity>

    fun findByUserIdAndAction(userId: String, action: String, pageable: Pageable): Page<FileStorageActivity>

    fun findByUserIdAndTimestampBetween(
        userId: String,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<FileStorageActivity>


    @Query("""
        SELECT fsa FROM FileStorageActivity fsa
        WHERE fsa.userId = :userId
        AND fsa.timestamp >= :since
        ORDER BY fsa.timestamp DESC
    """)
    fun findRecentActivitiesByUser(
        @Param("userId") userId: String,
        @Param("since") since: OffsetDateTime
    ): List<FileStorageActivity>

    @Query("""
        SELECT fsa FROM FileStorageActivity fsa
        WHERE fsa.timestamp >= :startTime
        AND fsa.timestamp <= :endTime
        ORDER BY fsa.timestamp ASC
    """)
    fun findAllByTimestampBetween(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): List<FileStorageActivity>
}