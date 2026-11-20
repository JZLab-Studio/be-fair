import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

object ProductionUrl {
    const val PROPERTY = "befair.productionUrl"
    const val ENV = "BEFAIR_PRODUCTION_URL"

    /** Gradle property wins over environment variable. Blank values count as absent. */
    fun resolve(property: String?, env: String?): String? =
        property?.trim()?.takeIf { it.isNotEmpty() } ?: env?.trim()?.takeIf { it.isNotEmpty() }

    /** Returns an error message, or null when the URL is acceptable for a release build. */
    fun validate(url: String?): String? = when {
        url.isNullOrBlank() -> "Production URL is missing. Set Gradle property '$PROPERTY' or environment variable '$ENV'."
        !url.startsWith("https://") -> "Production URL must use HTTPS (got '$url')."
        else -> null
    }
}

// Inputs are read lazily through providers so the tasks stay configuration-cache compatible
// and re-run when either source changes.
val productionUrl: Provider<String> = providers.gradleProperty(ProductionUrl.PROPERTY)
    .orElse("")
    .zip(providers.environmentVariable(ProductionUrl.ENV).orElse("")) { property, env ->
        ProductionUrl.resolve(property, env).orEmpty()
    }

val generatedBuildConfigDir = layout.buildDirectory.dir("generated/befair/buildConfig/commonMain/kotlin")

val generateBuildConfig by tasks.registering {
    val url = productionUrl
    val outputDir = generatedBuildConfigDir
    inputs.property("productionUrl", url)
    outputs.dir(outputDir)
    doLast {
        val escaped = url.get().replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
        val file = outputDir.get().asFile.resolve("dev/jakubzika/befair/config/GeneratedBuildConfig.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package dev.jakubzika.befair.config

            object GeneratedBuildConfig {
                const val PRODUCTION_URL = "$escaped"
            }

            """.trimIndent()
        )
    }
}

val validateProductionUrl by tasks.registering {
    val url = productionUrl
    doLast {
        ProductionUrl.validate(url.get().ifEmpty { null })?.let { throw GradleException(it) }
    }
}

// Android release builds (hosted by :app:androidApp) require a valid production URL; debug builds do not.
rootProject.findProject(":app:androidApp")?.tasks
    ?.matching { Regex("(assemble|bundle|package|lintVital).*Release").matches(it.name) }
    ?.configureEach { dependsOn(":app:shared:validateProductionUrl") }

// iOS: Xcode sets CONFIGURATION; only Release builds require the URL.
if (providers.environmentVariable("CONFIGURATION").orNull == "Release") {
    tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }
        .configureEach { dependsOn(validateProductionUrl) }
}

room {
    schemaDirectory(layout.projectDirectory.dir("schemas"))
}

compose.resources {
    packageOfResClass = "be_fair.app.shared.generated.resources"
    publicResClass = true
    generateResClass = always
}

kotlin {
    android {
        namespace = "dev.jakubzika.befair.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        @Suppress("OPT_IN_USAGE")
        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateBuildConfig.map { generatedBuildConfigDir })
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(projects.core)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.security.crypto)
            // Ktor client engine for HttpClientFactory.android.kt
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            // Ktor client engine for HttpClientFactory.ios.kt
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
