package dev.jakubzika.befair.util

/**
 * ISO 8601 (yyyy-MM-dd) date helpers backing the Add Item screen's purchase-date
 * field. Actuals avoid `java.time` (requires API 26+, and the app targets
 * minSdk 24 without core library desugaring) and instead use
 * `SimpleDateFormat`/`NSDateFormatter` on Android/iOS respectively.
 */
expect fun todayIso(): String

expect fun currentTimeMillis(): Long
