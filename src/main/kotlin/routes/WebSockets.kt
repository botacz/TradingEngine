package routes

import dto.toDto
import engine.Trade
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class TradeBroadcaster {
    private val sessions = ConcurrentHashMap.newKeySet<DefaultWebSocketServerSession>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun addSession(session: DefaultWebSocketServerSession) {
        sessions.add(session)
    }

    fun removeSession(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
    }

    fun broadcastTrade(trade: Trade) {
        val jsonMessage = Json.encodeToString(trade.toDto())
        scope.launch {
            for (session in sessions) {
                try {
                    session.send(Frame.Text(jsonMessage))
                } catch (e: Exception) {
                    sessions.remove(session)
                }
            }
        }
    }
}

fun Application.configureWebSockets(broadcaster: TradeBroadcaster) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/ws/trades") {
            broadcaster.addSession(this)
            try {
                for (frame in incoming) {
                }
            } finally {
                broadcaster.removeSession(this)
            }
        }
    }
}