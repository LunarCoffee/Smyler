package dev.lunarcoffee.smyleserver.routes

import com.google.gson.Gson
import dev.lunarcoffee.smyleserver.model.User
import dev.lunarcoffee.smyleserver.userCol
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import org.litote.kmongo.eq

fun Route.historyRoute() = get("/history") {
    val username = call.request.queryParameters["name"]!!

    val user = userCol.findOne(User::name eq username)!!
    val history = Gson().toJson(user.purchaseHistory)

    call.respondText(history, ContentType.Application.Json)
}
