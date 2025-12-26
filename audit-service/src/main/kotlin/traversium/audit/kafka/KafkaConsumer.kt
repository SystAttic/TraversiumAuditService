package traversium.audit.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.kafka.listener.MessageListener
import traversium.audit.kafka.AuditStreamData
import traversium.audit.kafka.ActivityType
import traversium.commonmultitenancy.TenantContext
import traversium.commonmultitenancy.TenantUtils
import traversium.audit.service.AuditService
import java.nio.charset.StandardCharsets

/**
 * Kafka consumer for audit events
 * @author Ozbej Pavc
 */
class KafkaConsumer(
    private val auditService: AuditService
) : MessageListener<String, AuditStreamData>, Logging {

    companion object {
        private const val TENANT_HEADER_KEY = "tenantId"
        private const val DEFAULT_TENANT = "public"
    }

    override fun onMessage(data: ConsumerRecord<String?, AuditStreamData?>) {
        val payload = data.value() ?: run {
            logger.warn { "Received null AuditStreamData payload on topic=${data.topic()}, partition=${data.partition()}, offset=${data.offset()}" }
            return
        }

        val tenantId = extractTenantFromHeaders(data.headers())
        logger.debug { "Extracted tenant ID: $tenantId from Kafka headers" }

        try {
            TenantContext.setTenant(TenantUtils.sanitizeTenantIdForSchema(tenantId))

            logger.debug { "Processing audit event for tenant=$tenantId: topic=${data.topic()}, partition=${data.partition()}, offset=${data.offset()}, payload=$payload" }
            
            when (payload.activityType) {
                ActivityType.USER_ACTIVITY -> {
                    auditService.saveUserActivity(payload)
                }
                ActivityType.TRIP_ACTIVITY -> {
                    auditService.saveTripActivity(payload)
                }
                ActivityType.SOCIAL_ACTIVITY -> {
                    auditService.saveSocialActivity(payload)
                }
                ActivityType.FILE_STORAGE_ACTIVITY -> {
                    auditService.saveFileStorageActivity(payload)
                }
            }
            
            logger.info { "Successfully processed audit event for tenant=$tenantId: activityType=${payload.activityType}, action=${payload.action}, offset=${data.offset()}" }
        } catch (e: Exception) {
            logger.error(e) {
                "Error processing audit event message: tenant=$tenantId, topic=${data.topic()}, partition=${data.partition()}, offset=${data.offset()}, payload=$payload"
            }
            throw e
        } finally {
            TenantContext.clear()
        }
    }

    private fun extractTenantFromHeaders(headers: Headers): String {
        val tenantHeader = headers.lastHeader(TENANT_HEADER_KEY)
        return if (tenantHeader != null) {
            String(tenantHeader.value(), StandardCharsets.UTF_8)
        } else {
            DEFAULT_TENANT
        }
    }
}

