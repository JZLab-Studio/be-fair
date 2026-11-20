package dev.jakubzika.befair.config

/** Writes a line to the platform console (Logcat on Android, stdout on iOS). */
expect fun consoleLog(message: String)
