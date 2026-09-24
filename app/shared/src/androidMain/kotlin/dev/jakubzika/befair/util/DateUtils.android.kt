package dev.jakubzika.befair.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun todayIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
