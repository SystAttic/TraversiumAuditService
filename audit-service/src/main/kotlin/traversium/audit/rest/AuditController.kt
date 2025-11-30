package traversium.audit.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.audit.db.model.TripActivity
import traversium.audit.db.model.UserActivity
import traversium.audit.service.AuditService
import traversium.audit.service.TripEventSourcingService
import traversium.audit.service.TripRevertResult
import java.time.OffsetDateTime

/**
 * REST controller for audit service
 * @author Ozbej Pavc
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit service endpoints")
class AuditController(
    private val auditService: AuditService,
    private val tripEventSourcingService: TripEventSourcingService
) {

    @GetMapping("/user/{userId}/activities")
    @Operation(summary = "Get user activities", description = "Retrieve audit logs for a specific user")
    fun getUserActivities(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: OffsetDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: OffsetDateTime?
    ): ResponseEntity<Page<UserActivity>> {
        val pageable = PageRequest.of(page, size)
        
        val activities = when {
            action != null && startTime != null && endTime != null -> {
                // Filter by action and time range
                auditService.getUserActivitiesByActionAndTimeRange(userId, action, startTime, endTime, pageable)
            }
            action != null -> {
                auditService.getUserActivitiesByAction(userId, action, pageable)
            }
            startTime != null && endTime != null -> {
                auditService.getUserActivitiesByTimeRange(userId, startTime, endTime, pageable)
            }
            else -> {
                auditService.getUserActivities(userId, pageable)
            }
        }
        
        return ResponseEntity.ok(activities)
    }

    @GetMapping("/trip/{tripId}/activities")
    @Operation(summary = "Get trip activities", description = "Retrieve audit logs for a specific trip")
    fun getTripActivities(
        @PathVariable tripId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: OffsetDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: OffsetDateTime?
    ): ResponseEntity<Page<TripActivity>> {
        val pageable = PageRequest.of(page, size)
        
        val activities = when {
            action != null && startTime != null && endTime != null -> {
                auditService.getTripActivitiesByActionAndTimeRange(tripId, action, startTime, endTime, pageable)
            }
            action != null -> {
                auditService.getTripActivitiesByAction(tripId, action, pageable)
            }
            startTime != null && endTime != null -> {
                auditService.getTripActivitiesByTimeRange(tripId, startTime, endTime, pageable)
            }
            else -> {
                auditService.getTripActivities(tripId, pageable)
            }
        }
        
        return ResponseEntity.ok(activities)
    }

    @GetMapping("/trip/{tripId}/recent-activity")
    @Operation(summary = "Get recent trip activity", description = "Get recent activity for a trip (last 7 days by default)")
    fun getRecentTripActivity(
        @PathVariable tripId: Long,
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<List<TripActivity>> {
        val activities = tripEventSourcingService.getRecentActivity(tripId, days)
        return ResponseEntity.ok(activities)
    }

    @GetMapping("/trip/{tripId}/event-history")
    @Operation(summary = "Get complete event history", description = "Get complete event sourcing history for a trip")
    fun getCompleteEventHistory(
        @PathVariable tripId: Long
    ): ResponseEntity<List<TripActivity>> {
        val events = tripEventSourcingService.getCompleteEventHistory(tripId)
        return ResponseEntity.ok(events)
    }

    @PostMapping("/trip/{tripId}/revert")
    @Operation(
        summary = "Revert trip to previous state",
        description = "Revert a trip to a previous state within the last 7 days. Returns the state that will be restored."
    )
    fun revertTripToState(
        @PathVariable tripId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) revertToTimestamp: OffsetDateTime
    ): ResponseEntity<TripRevertResult> {
        val result = tripEventSourcingService.revertTripToState(tripId, revertToTimestamp)
        return ResponseEntity.ok(result)
    }
}

