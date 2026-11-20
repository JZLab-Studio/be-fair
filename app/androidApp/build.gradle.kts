plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "dev.jakubzika.befair"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.jakubzika.befair"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "APP_MODE", "\"DEV\"")
        }
        getByName("release") {
            buildConfigField("String", "APP_MODE", "\"PROD\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.androidx.activity.compose)
    // @Preview annotation is referenced from main sources, so release compilation needs it too.
    implementation(compose.preview)
    debugImplementation(compose.uiTooling)
}
