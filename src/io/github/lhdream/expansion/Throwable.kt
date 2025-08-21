package io.github.lhdream.expansion

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.stackTraceToString(): String {
    val sw = StringWriter()
    PrintWriter(sw).use { pw -> this.printStackTrace(pw) }
    return sw.toString()
}