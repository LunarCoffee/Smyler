package dev.lunarcoffee.smyleserver

import java.io.File
import java.util.*

fun String.getImage(): File {
    val binaryData = Base64.getDecoder().decode(this)
    return File("resources/static/images/${System.currentTimeMillis()}.jpg")
        .apply { writeBytes(binaryData) }
}
