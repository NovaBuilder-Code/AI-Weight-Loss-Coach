plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.novaai.calorietracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.novaai.calorietracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.health.connect.client)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

// AGP's connected test task ("Uninstall task is always run") uninstalls the
// app + test APK when the run finishes, which made the sideloaded app vanish
// from the phone after every test session. Reinstall the debug APK right
// after any connected test run so the app stays installed like a normal app.
val adbExecutable = android.sdkDirectory.resolve("platform-tools").resolve(
    if (File.separatorChar == '\\') "adb.exe" else "adb"
).absolutePath

val reinstallDebugApkAfterTests = tasks.register<Exec>("reinstallDebugApkAfterTests") {
    group = "verification"
    description = "Reinstalls the debug APK with adb install -r after connected tests."
    val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
    inputs.file(apk)
    val serial = System.getenv("ANDROID_SERIAL")
    val cmd = mutableListOf(adbExecutable)
    if (!serial.isNullOrBlank()) {
        cmd.add("-s")
        cmd.add(serial)
    }
    cmd.addAll(listOf("install", "-r", apk.get().asFile.absolutePath))
    commandLine(cmd)
}

tasks.matching { it.name == "connectedDebugAndroidTest" || it.name == "connectedAndroidTest" }
    .configureEach { finalizedBy(reinstallDebugApkAfterTests) }
