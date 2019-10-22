package dev.lunarcoffee.smyleserver

import dev.lunarcoffee.smyleserver.model.Card
import dev.lunarcoffee.smyleserver.model.User
import dev.lunarcoffee.smyleserver.routes.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level
import java.io.File
import java.text.DateFormat
import kotlin.random.Random

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
internal fun Application.module() {
//    val user = User(
//        "Yash",
//        "1234",
//        listOf(
//            Card(
//                "Visa",
//                Random.nextLong(1_000_000_000_000_000, 10_000_000_000_000_000).toString(),
//                Random.nextInt(100, 1_000)
//            )
//        ),
//        "908a0bd6-2bc6-4a75-a09c-9d660bc81817",
//        emptyList()
//    )
//    runBlocking { userCol.insertOne(user) }

    install(AutoHeadResponse)
    install(CachingHeaders) {
        val imageCacheTypes = listOf(ContentType.Image.PNG, ContentType.Image.JPEG)
        options {
            val monthTime = CacheControl.MaxAge(2_678_400)
            if (it.contentType in imageCacheTypes) CachingOptions(monthTime) else null
        }
    }

    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) { gson { setDateFormat(DateFormat.LONG) } }
    install(CORS) { anyHost() }
    install(WebSockets)

    routing {
        static {
            staticRootFolder = File("resources/static")
            static("images") { files("images") }
        }

        route("/") {
            identifyRoute()
            kioskRoute()
            cashierRoute()
            dailyStatsRoute()
            historyRoute()
            usersRoute()

            register1Route()
            register2Route()
        }
    }
}
