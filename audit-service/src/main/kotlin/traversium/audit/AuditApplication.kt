package traversium.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import traversium.audit.kafka.KafkaProperties
import traversium.commonmultitenancy.FlywayTenantMigration
import traversium.commonmultitenancy.MultiTenantAutoConfiguration

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties::class)
@Import(MultiTenantAutoConfiguration::class, FlywayTenantMigration::class)
class AuditApplication

fun main(args: Array<String>) {
    runApplication<AuditApplication>(*args)
}

