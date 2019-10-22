package dev.lunarcoffee.smyleserver.model

class Card(
    val from: String = arrayOf("Visa", "MasterCard", "American Express").random(),
    val number: String,
    val cvv: Int
) {
    fun toJson() = """{"number":"$number","cvv":$cvv,"from":"$from"}"""
}
