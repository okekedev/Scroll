// Top-level build file. Versions live in gradle/libs.versions.toml.
// AGP 9 ships Kotlin support itself (built-in Kotlin), so only the Compose
// compiler plugin is declared next to it.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
