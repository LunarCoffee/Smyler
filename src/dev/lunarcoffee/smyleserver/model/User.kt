package dev.lunarcoffee.smyleserver.model

class User(
    val name: String,
    val pin: String,
    val cards: List<Card>,
    val id: String,
    val purchaseHistory: List<Purchase>
) {
    var loyaltyPoints = 0
}
