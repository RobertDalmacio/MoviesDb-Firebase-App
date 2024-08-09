// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
    id("io.sentry.android.gradle") version "3.12.0" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.4.20" apply false
    id("androidx.navigation.safeargs") version "2.5.3" apply false
}