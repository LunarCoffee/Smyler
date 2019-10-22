package dev.lunarcoffee.smyleserver.routes

import com.google.gson.Gson
import dev.lunarcoffee.smyleserver.*
import dev.lunarcoffee.smyleserver.model.*
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import khttp.post
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.setValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private val FACE_ID_REGEX = """[^"]+"faceId":"([A-z0-9\-]+)".+""".toRegex()
private val PERSON_ID_REGEX = """.+?"personId":"([A-z0-9\-]+)".+""".toRegex()

private val GSON = Gson()

fun Route.identifyRoute() = post("/identify") {
    val file = call.receive<Base64Image>().image.getImage()
    println("Received image!")

    delay(1000L)

    val personId = "908a0bd6-2bc6-4a75-a09c-9d660bc81817"
    val user = userCol.findOne(User::id eq personId)!!
    val cards = user.cards.joinToString(",") { it.toJson() }
    val history = user
        .purchaseHistory
        .joinToString(",") {
            """{"card":${it.card.toJson()},
                |"amount":${it.amount},
                |"date":"${it.date}",
                |"location":"${it.location}"
                |}""".trimMargin()
        }
    val json = """{"name":"${user.name}","cards":[$cards],"purchaseHistory":[$history]}"""

    // Send JSON with user information to the kiosk stand to prepare card information.
    call.respondText(json, ContentType.parse("application/json"))
    println("Sent kiosk user information!")

    // Wait for the cashier to connect, then send the user's JSON information.
    while (cashier == null)
        yield()
    cashier!!.send("""${json.dropLast(1)},"image":"http://35.202.54.214:20000/images/${file.name}"}""")
    println("Sent cashier user information!")

    val cost: Int
    var loyaltySent = false
    var items: List<CashierPurchase> = emptyList()
    // Wait for payment details from the cashier.
    for (frame in cashier!!.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()

            // Deal with the loyalty card.
            if (text == "Lunar Coffee Rewards" && !loyaltySent) {
                kiosk!!.send("Loyalty.")
                println("Sent loyalty ")
                loyaltySent = true
                continue
            }

            // Get the payment details.
            items = text
                .drop(1)
                .dropLast(1)
                .trim { it in arrayOf(',', '\n', ' ') }
                .split(",")
                .chunked(2)
                .map { GSON.fromJson(it.joinToString(), CashierPurchase::class.java) }
            cost = items.sumBy { (it.cost * 100).roundToInt() }
            println("Got ${items.size} items for $cost cents!")

            val daily = dailyRevenueCol.findOne(DailyRevenue::sales gt -1)!!
            val revenue = daily.revenue
            val sales = daily.sales
            dailyRevenueCol.updateOne(
                DailyRevenue::sales eq sales,
                and(
                    setValue(DailyRevenue::revenue, revenue + cost),
                    setValue(DailyRevenue::sales, sales + items.size)
                )
            )

            while (kiosk == null)
                yield()
            kiosk!!.send("payment$text")
            println("Payment info sent to kiosk!")

            break
        }
    }

    // Wait for smile to pay confirmation from the kiosk.
    lateinit var card: Card
    for (frame in kiosk!!.incoming) {
        if (frame is Frame.Text) {
            // Get the card selected for payment.
            card = user.cards.find { it.number == frame.readText() }!!
            break
        }
    }
    println("Received card #${card.number}!")

    // Update new purchases.
    val newPurchases = items.map {
        Purchase(
            it.name,
            card,
            "15/09/2019",
            (it.cost * 100).roundToLong(),
            "Lunar Coffee"
        )
    }
    userCol.updateOne(
        User::id eq personId,
        setValue(User::purchaseHistory, user.purchaseHistory + newPurchases)
    )

    // Alert the cashier of the successful transaction.
    cashier!!.send("Success.")
}
