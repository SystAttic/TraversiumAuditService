package traversium.audit.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import traversium.audit.db.model.TripActivity
import java.time.OffsetDateTime

/**
 * Repository for TripActivity entities with Event Sourcing support
 * @author Ozbej Pavc
 */
interface TripActivityRepository : JpaRepository<TripActivity, Long> {
    
    fun findByTripId(tripId: Long, pageable: Pageable): Page<TripActivity>
    
    fun findByTripIdAndAction(tripId: Long, action: String, pageable: Pageable): Page<TripActivity>
    
    fun findByTripIdAndTimestampBetween(
        tripId: Long,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        pageable: Pageable
    ): Page<TripActivity>
    
    @Query("""
        SELECT ta FROM TripActivity ta
        WHERE ta.tripId = :tripId
        AND ta.timestamp >= :since
        ORDER BY ta.timestamp ASC, ta.eventVersion ASC
    """)
    fun findEventsForTripSince(
        @Param("tripId") tripId: Long,
        @Param("since") since: OffsetDateTime
    ): List<TripActivity>
    
    @Query("""
        SELECT ta FROM TripActivity ta
        WHERE ta.tripId = :tripId
        AND ta.timestamp <= :until
        ORDER BY ta.timestamp ASC, ta.eventVersion ASC
    """)
    fun findEventsForTripUntil(
        @Param("tripId") tripId: Long,
        @Param("until") until: OffsetDateTime
    ): List<TripActivity>
    
    @Query("""
        SELECT ta FROM TripActivity ta
        WHERE ta.tripId = :tripId
        AND ta.userId = :userId
        AND ta.timestamp >= :since
        ORDER BY ta.timestamp DESC
    """)
    fun findRecentActivitiesByUserAndTrip(
        @Param("tripId") tripId: Long,
        @Param("userId") userId: String,
        @Param("since") since: OffsetDateTime
    ): List<TripActivity>
    
    fun findByTripIdOrderByTimestampAscEventVersionAsc(tripId: Long): List<TripActivity>
    
    @Query("""
        SELECT ta FROM TripActivity ta
        WHERE ta.timestamp >= :startTime
        AND ta.timestamp <= :endTime
        ORDER BY ta.timestamp ASC, ta.eventVersion ASC
    """)
    fun findAllByTimestampBetween(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): List<TripActivity>
}

