package dev.jakubzika.befair.config

import android.util.Log

actual fun consoleLog(message: String) {
    Log.d("BeFair", message)
}
