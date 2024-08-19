package dev.rdcl.health

import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

fun main(args: Array<String>): Unit = runBlocking {
    val start = parseDate(args, 0)
    val end = parseDate(args, 1)

    DbConnector.getBetween(start, end)
        .doOnNext { GarminConnector.save(it) }
        .awaitLast()
}

fun parseDate(args: Array<String>, i: Int): LocalDate? =
    if (args.size > i && args[i] != "null")
        LocalDate.parse(args[i])
    else
        null
