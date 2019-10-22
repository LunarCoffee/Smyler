package dev.lunarcoffee.smyleserver.model

class Purchase(
    val item: String,
    val card: Card,
    val date: String,
    val amount: Long,
    val location: String
)
