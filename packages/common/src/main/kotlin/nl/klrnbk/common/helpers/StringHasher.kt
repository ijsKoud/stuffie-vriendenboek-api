package nl.klrnbk.common.helpers

import java.security.MessageDigest

fun hash(
    sub: String,
    salt: String,
): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest("$salt:$sub".toByteArray())
        .joinToString("") { "%02x".format(it) }
