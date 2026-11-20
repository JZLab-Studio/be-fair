package dev.jakubzika.befair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jakubzika.befair.config.AppConfig
import dev.jakubzika.befair.config.AppMode
import dev.jakubzika.befair.data.storage.BeFairAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Provide the application context to core (needed by EncryptedSharedPreferences-backed
        // TokenStorage) before any AppContainer / TokenStorage is constructed in App().
        BeFairAndroidContext.init(applicationContext)
        AppConfig.initialize(AppMode.parse(BuildConfig.APP_MODE))

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}