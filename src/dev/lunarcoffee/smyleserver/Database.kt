package dev.lunarcoffee.smyleserver

import dev.lunarcoffee.smyleserver.model.DailyRevenue
import dev.lunarcoffee.smyleserver.model.User
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.gt
import org.litote.kmongo.reactivestreams.KMongo

val database = KMongo.createClient().coroutine.getDatabase("Smyle")
val userCol = database.getCollection<User>()
val dailyRevenueCol = database.getCollection<DailyRevenue>().apply {
    runBlocking {
        if (findOne(DailyRevenue::sales gt -1) == null)
            insertOne(DailyRevenue(0, 0))
    }
}
