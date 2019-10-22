package dev.lunarcoffee.smyleserver.routes

import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.yield

var kiosk: WebSocketSession? = null

fun Route.kioskRoute() = webSocket("/kiosk") {
    println("Got connection from kiosk!")
    kiosk = this
    while (true)
        yield()
}
