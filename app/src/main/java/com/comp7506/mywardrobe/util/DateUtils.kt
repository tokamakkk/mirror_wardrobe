package com.comp7506.mywardrobe.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val IsoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun LocalDate.toIsoDateString(): String = format(IsoDateFormatter)

fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this, IsoDateFormatter) }.getOrNull()

fun YearMonth.startIsoDate(): String = atDay(1).toIsoDateString()

fun YearMonth.endIsoDate(): String = atEndOfMonth().toIsoDateString()
