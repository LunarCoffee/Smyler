package dev.lunarcoffee.smyleserver.routes

import dev.lunarcoffee.smyleserver.dailyRevenueCol
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.dailyStatsRoute() = get("/daily") {
    val daily = dailyRevenueCol.find().first()!!
    val revenue = daily.revenue
    val sales = daily.sales

    call.respondText(
        """{"revenue":$revenue,"sales":$sales}""",
        ContentType.parse("application/json")
    )
}
