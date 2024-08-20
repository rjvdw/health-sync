package dev.rdcl.health

import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

private val ACCEPTABLE_DIFFERENCE_THRESHOLD = BigDecimal("0.5")

private enum class DataSource { DB, API }

fun main(args: Array<String>): Unit = runBlocking {
    val start = parseDate(args, 0)
    val end = parseDate(args, 1)

    val existing = mono { GarminConnector.getBetween(start, end) }
        .flatMapMany { Flux.fromIterable(it) }
        .map { Pair(DataSource.API, it) }

    DbConnector.getBetween(start, end)
        .filter { it.weight != null }
        .map { Pair(DataSource.DB, it) }
        .mergeComparingWith(existing, Comparator.comparing { it.second.date })
        .groupBy { it.second.date }
        .flatMap { gr -> gr.collectList().flatMap { combineRecords(it) } }
        .flatMap { record -> mono { GarminConnector.save(record) } }
        .awaitLast()
}

private fun parseDate(args: Array<String>, i: Int): LocalDate? =
    if (args.size > i && args[i] != "null")
        LocalDate.parse(args[i])
    else
        null

private fun combineRecords(records: List<Pair<DataSource, HealthRecord>>): Mono<HealthRecord> =
    when {
        records.size == 1 && records[0].first == DataSource.DB -> Mono.just(records[0].second)

        records.size == 2 -> {
            val dbRecord: HealthRecord
            val apiRecord: HealthRecord

            if (records[0].first == DataSource.DB) {
                dbRecord = records[0].second
                apiRecord = records[1].second
            } else {
                dbRecord = records[1].second
                apiRecord = records[0].second
            }

            when {
                dbRecord.weight == null -> Mono.empty()
                apiRecord.weight == null -> Mono.just(dbRecord)
                dbRecord.weight.closeEnoughTo(apiRecord.weight) -> Mono.empty()
                else -> Mono.just(dbRecord)
            }
        }

        else -> Mono.empty()
    }

private fun BigDecimal.closeEnoughTo(other: BigDecimal): Boolean =
    (this - other).abs() < ACCEPTABLE_DIFFERENCE_THRESHOLD
