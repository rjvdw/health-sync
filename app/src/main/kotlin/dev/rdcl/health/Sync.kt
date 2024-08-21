package dev.rdcl.health

import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

private val ACCEPTABLE_DIFFERENCE_THRESHOLD = BigDecimal("0.5")

typealias Item = Triple<LocalDate, HealthRecord?, HealthRecord?>

fun main(args: Array<String>): Unit = runBlocking {
    val start = parseDate(args, 0)
    val end = parseDate(args, 1)

    DbConnector.getBetween(start, end)
        .filter { it.weight != null }
        .map { Item(it.date, it, null) }
        .mergeComparingWith(
            mono { GarminConnector.getBetween(start, end) }
                .flatMapMany { Flux.fromIterable(it) }
                .map { Item(it.date, null, it) },
            Comparator.comparing(Item::first)
                .thenComparing { a, b ->
                    when {
                        a.third == null && b.third != null -> 1
                        a.third != null && b.third == null -> -1
                        else -> 0
                    }
                }
        )
        .scan { a, b ->
            when {
                a.first == b.first && a.second != null -> Triple(a.first, a.second, b.third)
                a.first == b.first && a.third != null -> Triple(a.first, b.second, a.third)
                else -> b
            }
        }
        .filter { it.second != null }
        .flatMap {
            when {
                it.second?.weight == null -> Mono.empty()
                it.third?.weight == null -> Mono.just(it.second!!)
                it.second!!.weight!!.closeEnoughTo(it.third!!.weight!!) -> Mono.empty()
                else -> Mono.just(it.second!!)
            }
        }
        .flatMap { record -> mono { GarminConnector.save(record) } }
        .defaultIfEmpty(Unit)
        .awaitLast()
}

private fun parseDate(args: Array<String>, i: Int): LocalDate? = when {
    args.size > i && args[i] != "null" -> LocalDate.parse(args[i])
    else -> null
}

private fun BigDecimal.closeEnoughTo(other: BigDecimal): Boolean =
    (this - other).abs() < ACCEPTABLE_DIFFERENCE_THRESHOLD
