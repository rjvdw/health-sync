package dev.rdcl.health

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

class DbConnector {
    companion object {
        private val cf: ConnectionFactory = ConnectionFactories.get(
            builder()
                .option(DRIVER, getEnv("DB_DRIVER") ?: "postgresql")
                .option(HOST, getEnv("DB_HOST") ?: "localhost")
                .option(PORT, getEnv("DB_PORT")?.toInt() ?: 5432)
                .option(USER, getEnvRequired("DB_USER"))
                .option(PASSWORD, getEnvRequired("DB_PASSWORD"))
                .option(DATABASE, getEnvRequired("DB_NAME"))
                .build()
        )

        fun getBetween(start: LocalDate?, end: LocalDate?): Flux<HealthRecord> =
            conn()
                .flatMapMany {
                    it
                        .createStatement(
                            """
                        select date,
                               (data ->> 'weight')::numeric as weight
                        from health
                        where date between $1 and $2
                        order by date
                        """.trimIndent()
                        )
                        .bind(0, start ?: LocalDate.MIN)
                        .bind(1, end ?: LocalDate.MAX)
                        .execute()
                }
                .flatMap {
                    it.map { row, _ ->
                        HealthRecord(
                            row.get("date", LocalDate::class.java)!!,
                            row.get("weight", BigDecimal::class.java)
                        )
                    }
                }

        private fun conn() = Mono.from(cf.create())
    }
}
