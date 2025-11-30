package traversium.audit

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import traversium.audit.security.TestMultitenancyConfig

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@SpringBootTest(
    classes = [
        AuditApplication::class,
        TestMultitenancyConfig::class
    ]
)
@DirtiesContext
class AuditApplicationTests {

    @Test
    fun contextLoads() {
    }
}

