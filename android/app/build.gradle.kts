import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing reads keystore/keystore.properties (git-ignored):
//   storeFile=upload-keystore.jks
//   storePassword=...
//   keyAlias=upload
//   keyPassword=...
// There is no fallback to the debug key: without the file, debug builds, unit
// tests and lint still work, but packaging a release APK or AAB fails.
val keystorePropsFile = rootProject.file("keystore/keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
val hasUploadKey = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    .all { !keystoreProps.getProperty(it).isNullOrBlank() }

android {
    namespace = "com.christianokeke.scroll"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.christianokeke.scroll"
        minSdk = 26
        targetSdk = 37
        // versionCode must go up by at least one on every Play upload.
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = rootProject.file("keystore/${keystoreProps.getProperty("storeFile")}")
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasUploadKey) signingConfigs.getByName("upload") else null
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // Kotlin's jvmTarget follows targetCompatibility under built-in Kotlin.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }

    lint {
        // Newer library versions are a judgment call, not a defect.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }
}

// Stop a release from being packaged unsigned (or with the debug key) when the
// upload key is not on this machine. A task of its own, with no outputs, so it
// runs on every release build and can never be skipped as up to date.
val checkReleaseSigning = tasks.register("checkReleaseSigning") {
    val signed = hasUploadKey
    val expected = keystorePropsFile.path
    doLast {
        if (!signed) {
            throw GradleException(
                "Release signing is not set up: $expected is missing or incomplete " +
                    "(needs storeFile, storePassword, keyAlias, keyPassword). See README.md."
            )
        }
    }
}
val releasePackagingTasks = setOf(
    "packageRelease", "assembleRelease",
    "packageReleaseBundle", "signReleaseBundle", "bundleRelease",
)
tasks.configureEach {
    if (name in releasePackagingTasks) dependsOn(checkReleaseSigning)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
