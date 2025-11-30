package traversium.audit.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.audit.db.model.TripActivity
import traversium.audit.db.repository.TripActivityRepository
import java.time.OffsetDateTime

/**
 * Service for Event Sourcing and CQRS operations on trip activities
 * Allows reconstructing trip state and reverting to previous states
 * @author Ozbej Pavc
 */
@Service
class TripEventSourcingService(
    private val tripActivityRepository: TripActivityRepository
) {

    companion object {
        private const val REVERT_WINDOW_DAYS = 7
    }

    /**
     * Gets all events for a trip since a given timestamp
     * Used for reconstructing trip state
     */
    fun getEventsSince(tripId: Long, since: OffsetDateTime): List<TripActivity> {
        return tripActivityRepository.findEventsForTripSince(tripId, since)
    }

    /**
     * Gets all events for a trip up to a given timestamp
     * Used for reconstructing trip state at a specific point in time
     */
    fun getEventsUntil(tripId: Long, until: OffsetDateTime): List<TripActivity> {
        return tripActivityRepository.findEventsForTripUntil(tripId, until)
    }

    /**
     * Gets recent activity for a trip (last 7 days by default)
     * Used for displaying recent activity to collaborators
     */
    fun getRecentActivity(tripId: Long, days: Int = REVERT_WINDOW_DAYS): List<TripActivity> {
        val since = OffsetDateTime.now().minusDays(days.toLong())
        return tripActivityRepository.findEventsForTripSince(tripId, since)
    }

    /**
     * Gets the complete event history for a trip
     * Used for full state reconstruction
     */
    fun getCompleteEventHistory(tripId: Long): List<TripActivity> {
        return tripActivityRepository.findByTripIdOrderByTimestampAscEventVersionAsc(tripId)
    }

    /**
     * Reverts a trip to a previous state based on a specific timestamp
     * This will call TripService endpoints to apply the revert
     * 
     * TODO: Implement actual TripService API calls to revert trip state
     * 
     * @param tripId The trip to revert
     * @param revertToTimestamp The timestamp to revert to (must be within last 7 days)
     * @return The state that the trip will be reverted to
     */
    @Transactional(readOnly = true)
    fun revertTripToState(tripId: Long, revertToTimestamp: OffsetDateTime): TripRevertResult {
        val now = OffsetDateTime.now()
        val windowStart = now.minusDays(REVERT_WINDOW_DAYS.toLong())
        
        // Validate revert window
        if (revertToTimestamp.isBefore(windowStart)) {
            throw IllegalArgumentException(
                "Cannot revert to a state older than $REVERT_WINDOW_DAYS days. " +
                "Requested: $revertToTimestamp, Window start: $windowStart"
            )
        }
        
        if (revertToTimestamp.isAfter(now)) {
            throw IllegalArgumentException("Cannot revert to a future timestamp")
        }
        
        // Get all events up to the revert timestamp
        val eventsUntilRevert = getEventsUntil(tripId, revertToTimestamp)
        
        if (eventsUntilRevert.isEmpty()) {
            throw IllegalStateException("No events found for trip $tripId up to timestamp $revertToTimestamp")
        }
        
        // Find the latest state snapshot before or at the revert timestamp
        val stateSnapshot = eventsUntilRevert
            .filter { it.stateSnapshot != null }
            .lastOrNull()
        
        // Get events that would be "undone" (events after the revert timestamp)
        val eventsToUndo = tripActivityRepository.findEventsForTripSince(tripId, revertToTimestamp)
            .filter { it.timestamp.isAfter(revertToTimestamp) }
        
        // TODO: Call TripService endpoints to revert the trip state
        // This would involve:
        // 1. Getting the current trip state from TripService
        // 2. Applying reverse operations for events that need to be undone
        // 3. Restoring state from snapshot if available
        // 4. Calling TripService update endpoints
        
        return TripRevertResult(
            tripId = tripId,
            revertToTimestamp = revertToTimestamp,
            targetStateSnapshot = stateSnapshot,
            eventsToUndo = eventsToUndo,
            reconstructedState = reconstructStateFromEvents(eventsUntilRevert)
        )
    }

    /**
     * Reconstructs trip state from a list of events
     * This is a CQRS read model construction
     */
    private fun reconstructStateFromEvents(events: List<TripActivity>): Map<String, Any> {
        // Start with the latest snapshot if available
        val latestSnapshot = events
            .filter { it.stateSnapshot != null }
            .lastOrNull()
            ?.stateSnapshot
        
        val state = mutableMapOf<String, Any>()
        
        // If we have a snapshot, use it as base
        if (latestSnapshot != null) {
            // TODO: Parse snapshot JSON and merge into state
            // For now, we'll build from events
        }
        
        // Apply all events to reconstruct state
        events.forEach { event ->
            when (event.action.uppercase()) {
                "TRIP_CREATED" -> {
                    state["tripId"] = event.tripId
                    state["createdBy"] = event.userId
                    state["createdAt"] = event.timestamp.toString()
                }
                "TRIP_VISIBILITY_CHANGED" -> {
                    // Extract visibility from metadata
                    state["visibility"] = "UPDATED"
                }
                "COLLABORATOR_ADDED" -> {
                    // Track collaborators
                    val collaborators = state.getOrDefault("collaborators", mutableListOf<String>()) as MutableList<String>
                    event.entityId?.let { collaborators.add(it.toString()) }
                    state["collaborators"] = collaborators
                }
                "VIEWER_ADDED" -> {
                    // Track viewers
                    val viewers = state.getOrDefault("viewers", mutableListOf<String>()) as MutableList<String>
                    event.entityId?.let { viewers.add(it.toString()) }
                    state["viewers"] = viewers
                }
                // Add more event handlers as needed
            }
        }
        
        return state
    }
}

/**
 * Result of a trip revert operation
 */
data class TripRevertResult(
    val tripId: Long,
    val revertToTimestamp: OffsetDateTime,
    val targetStateSnapshot: TripActivity?,
    val eventsToUndo: List<TripActivity>,
    val reconstructedState: Map<String, Any>
)

