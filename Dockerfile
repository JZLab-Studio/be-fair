# syntax=docker/dockerfile:1

# ---- build -----------------------------------------------------------------------------
# A JDK is all the builder needs. `:server` depends only on `:core`'s `jvm()` target, and the
# Android/iOS targets in the other modules are never configured into the task graph, so no
# Android SDK has to be installed here.
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# Wrapper and version catalog first. They change far less often than sources, so the Gradle
# distribution download stays cached when only Kotlin files change.
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew --version

# `settings.gradle.kts` includes the mobile modules, so their build scripts must be present
# for Gradle to configure the build even though nothing here compiles them.
COPY core core
COPY server server
COPY app app

# `installDist` (application plugin) rather than `buildFatJar`, deliberately. Shading merges
# every dependency into one jar, and the Ktor shadow task drops duplicate resource paths --
# which silently discards flyway-core's META-INF/services entries and leaves Flyway unable to
# recognise its own migrations. The distribution keeps dependencies as separate jars, so
# service discovery behaves exactly as it does under `:server:run` and `:server:test`.
#
# The daemon and configuration cache only pay off across repeated builds; a container build
# runs Gradle exactly once and then throws the layer away.
#
# The heap caps override gradle.properties, which asks for 4G for Gradle plus 3G for the
# Kotlin daemon -- tuned for a developer workstation and enough to get the build OOM-killed
# inside a default-sized Docker VM. Compiling one JVM module needs far less.
RUN ./gradlew :server:installDist \
        --no-daemon \
        --no-configuration-cache \
        -Dorg.gradle.jvmargs=-Xmx2g \
        -Pkotlin.daemon.jvmargs=-Xmx1g

# ---- runtime ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Nothing in the image needs write access, so the server runs as an unprivileged user.
RUN addgroup -S befair && adduser -S -G befair befair

WORKDIR /app
# Dependencies first: they change far less often than application code, so this layer stays
# cached across ordinary code-only rebuilds.
COPY --from=build /build/server/build/install/server/lib lib
COPY --from=build /build/server/build/install/server/bin bin
USER befair

# Mirrors `SERVER_PORT` in core/src/commonMain/kotlin/dev/jakubzika/befair/Constants.kt.
# Keep the two in step; the JVM reads the constant, this line is only metadata.
EXPOSE 8080

# Without MaxRAMPercentage the JVM sizes its heap from the host's memory rather than the
# container limit, and gets OOM-killed under a tight limit. The generated start script passes
# JAVA_OPTS through to the JVM.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["/app/bin/server"]
