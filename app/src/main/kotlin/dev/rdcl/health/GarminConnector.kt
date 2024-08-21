package dev.rdcl.health

import dev.rdcl.health.garmin.GetResponse
import dev.rdcl.health.garmin.SaveRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

private val BASE_URL = "https://connect.garmin.com/weight-service"
private val WEIGHT_CONVERSION_FACTOR = BigDecimal("1000.00")
private val MIN_DATE = LocalDate.parse("1900-01-01")
private val MAX_DATE = LocalDate.parse("3000-01-01")

private val JSON = Json { ignoreUnknownKeys = true }

class GarminConnector {
    companion object {
        private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(JSON)
            }
            defaultRequest {
                headers {
                    append("DI-Backend", "connectapi.garmin.com")
                    append("Authorization", "Bearer ${getAccessToken()}")
                }
            }
        }

        suspend fun getBetween(start: LocalDate?, end: LocalDate?): List<HealthRecord> {
            val paramStart = start ?: MIN_DATE
            val paramEnd = end ?: MAX_DATE

            return withContext(Dispatchers.IO) {
                client.get("$BASE_URL/weight/range/$paramStart/$paramEnd?includeAll=true") {
                    headers { append("Accept", "application/json") }
                }
                    .body<GetResponse>()
                    .dailyWeightSummaries
                    .filter { it.numOfWeightEntries > 0 }
                    .sortedBy { it.summaryDate }
                    .map {
                        HealthRecord(
                            LocalDate.parse(it.summaryDate),
                            parseWeight(it.allWeightMetrics[0].weight)
                        )
                    }
            }
        }

        suspend fun save(record: HealthRecord) {
            println("Saving record: $record")
            val requestBody = SaveRequest(record.date, record.weight!!)
            withContext(Dispatchers.IO) {
                client.post("$BASE_URL/user-weight") {
                    contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    setBody(JSON.encodeToString(requestBody))
                }
            }
        }
    }
}

private fun getAccessToken(): String {
    val garthOauth2Path = getEnv("GARTH_OAUTH2_PATH") ?: ".garth/session/oauth2_token.json"
    val data = File(garthOauth2Path).readText()
    val token = JSON.decodeFromString<Oauth2Token>(data)

    return token.accessToken
}

@Serializable
private data class Oauth2Token(
    @SerialName("access_token")
    val accessToken: String
)

private fun parseWeight(weight: Double): BigDecimal =
    BigDecimal.valueOf(weight).divide(WEIGHT_CONVERSION_FACTOR)
