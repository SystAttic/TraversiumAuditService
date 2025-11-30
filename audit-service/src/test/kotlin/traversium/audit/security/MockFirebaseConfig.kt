package traversium.audit.security

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Mock Firebase configuration for tests
 * @author Ozbej Pavc
 */
@TestConfiguration
class MockFirebaseConfig {

    private val tokenMap = mutableMapOf<String, Pair<String, String>>() // token -> (uid, email)

    fun setTokenData(token: String, uid: String, email: String) {
        tokenMap[token] = uid to email
    }

    @Bean
    @Primary
    fun initializeFirebase(): FirebaseApp {
        return mock(FirebaseApp::class.java)
    }

    @Bean
    @Primary
    fun firebaseAuth(): FirebaseAuth {
        val mockAuth = mock(FirebaseAuth::class.java)
        
        `when`(mockAuth.verifyIdToken(any())).thenAnswer { invocation ->
            val token = invocation.arguments[0] as String
            val (uid, email) = tokenMap[token] ?: ("firebase123" to "test@example.com")
            
            val mockToken = mock(FirebaseToken::class.java)
            `when`(mockToken.uid).thenReturn(uid)
            `when`(mockToken.email).thenReturn(email)
            // Set tenantId to null so the filter uses regular getUser() instead of tenant manager
            `when`(mockToken.tenantId).thenReturn(null)
            mockToken
        }
        
        `when`(mockAuth.getUser(any())).thenAnswer { invocation ->
            val uid = invocation.arguments[0] as String
            val (_, email) = tokenMap.values.find { it.first == uid } ?: ("firebase123" to "test@example.com")
            
            val mockUserRecord = mock(UserRecord::class.java)
            `when`(mockUserRecord.uid).thenReturn(uid)
            `when`(mockUserRecord.email).thenReturn(email)
            `when`(mockUserRecord.photoUrl).thenReturn(null)
            mockUserRecord
        }

        return mockAuth
    }
}

