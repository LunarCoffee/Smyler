package dev.lunarcoffee.smyleserver.routes

import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.yield

var cashier: WebSocketSession? = null

fun Route.cashierRoute() = webSocket("/cashier") {
    println("Got connection from cashier!")
    cashier = this
    while (true)
        yield()
}
