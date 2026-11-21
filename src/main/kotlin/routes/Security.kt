package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import java.util.*
import kotlin.time.Duration.Companion.seconds

object JwtConfig {
    const val SECRET = "k9P#mX7vL2@nQ8*zR4&wT6!yB5^cF1"
    const val ISSUER = "trading-engine"
    const val AUDIENCE = "trading-users"
    const val REALM = "TradingEngine API"

    fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
            .sign(Algorithm.HMAC256(SECRET))
    }
}

fun Application.configureSecurity() {
    install(RateLimit) {
        register(RateLimitName("api-limit")) {
            rateLimiter(limit = 10, refillPeriod = 1.seconds)
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.SECRET))
                    .withAudience(JwtConfig.AUDIENCE)
                    .withIssuer(JwtConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is missing or invalid.")
            }
        }
    }
}