package dev.rdcl.health

import java.math.BigDecimal
import java.time.LocalDate

data class HealthRecord(val date: LocalDate, val weight: BigDecimal?)
