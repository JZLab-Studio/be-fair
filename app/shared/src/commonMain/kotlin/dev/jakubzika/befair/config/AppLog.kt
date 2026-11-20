package dev.jakubzika.befair.config

/** Application-authored logging. Output is produced in DEV mode only. */
object AppLog {
    fun d(message: () -> String) = log(AppConfig.isDev, ::consoleLog, message)
}

/** Never evaluates [message] when [isDev] is false. */
internal fun log(isDev: Boolean, sink: (String) -> Unit, message: () -> String) {
    if (isDev) sink(message())
}
