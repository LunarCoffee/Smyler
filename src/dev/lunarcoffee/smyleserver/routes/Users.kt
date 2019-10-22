package dev.lunarcoffee.smyleserver.routes

import com.google.gson.Gson
import dev.lunarcoffee.smyleserver.userCol
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.usersRoute() = get("/users") {
    val users = Gson().toJson(userCol.find().toList())
    call.respondText(users, ContentType.Application.Json)
}
