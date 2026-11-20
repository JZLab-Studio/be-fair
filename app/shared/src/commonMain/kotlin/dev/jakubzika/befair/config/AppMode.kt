package dev.jakubzika.befair.config

/** Build mode of the mobile app. Fixed at build time by the host project. */
enum class AppMode {
    DEV,
    PROD;

    companion object {
        /** Anything other than exactly "DEV" (including null or blank) is PROD. */
        fun parse(raw: String?): AppMode = if (raw == "DEV") DEV else PROD
    }
}
