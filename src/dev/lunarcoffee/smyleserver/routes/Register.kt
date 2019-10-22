package dev.lunarcoffee.smyleserver.routes

import dev.lunarcoffee.smyleserver.*
import dev.lunarcoffee.smyleserver.model.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import khttp.post
import kotlin.random.Random

// god help me :(

private var pack1: RegisterPackage? = null
private var pack2 = mutableListOf<RegisterImage>()

fun Route.register1Route() = post("/register1") {
    pack1 = call.receive()
    call.respondText("ok", status = HttpStatusCode.OK)
}

fun Route.register2Route() = post("/register2") {
    pack2.add(call.receive())
    if (pack2.size == 5) {
        val personId = createPerson(pack1!!.name)
        val user = User(
            pack1!!.name,
            pack1!!.pin,
            listOf(
                Card(
                    "Visa",
                    Random.nextLong(1_000_000_000_000_000, 10_000_000_000_000_000).toString(),
                    Random.nextInt(100, 1_000)
                )
            ),
            personId,
            emptyList()
        )
        userCol.insertOne(user)
        trainPerson(personId, pack2)
        pack2.clear()
    }
    call.respondText("ok", status = HttpStatusCode.OK)
}

private fun createPerson(name: String): String {
    val personIdRegex = """.+?"personId":"([A-z0-9\-]+)".*""".toRegex()
    return personIdRegex.matchEntire(
        post(
            "$MSCS_URI/persongroups/$MSCS_GROUP_NAME/persons",
            mapOf("Content-Type" to "application/json", "Ocp-Apim-Subscription-Key" to MSCS_KEY),
            data = """{"name":"${name.take(128).filter { it.isLetterOrDigit() }}"}"""
        ).text
    )!!.groupValues[1]
}

private fun trainPerson(id: String, i: List<RegisterImage>) {
    // Add the three faces to the face group.
    for (image in i) {
        val imageFile = image.image.drop(23).getImage()
        post(
            "$MSCS_URI/persongroups/$MSCS_GROUP_NAME/persons/$id/persistedFaces",
            mapOf("Content-Type" to "application/json", "Ocp-Apim-Subscription-Key" to MSCS_KEY),
            data = """{"url":"http://34.66.144.105/images/${imageFile.name}"}"""
        ).apply { println("\n\n\n${this.text}\n\n\n") }
    }

    // Actually train the models.
    post(
        "$MSCS_URI/persongroups/$MSCS_GROUP_NAME/train",
        mapOf("Content-Type" to "application/json", "Ocp-Apim-Subscription-Key" to MSCS_KEY)
    )
}
