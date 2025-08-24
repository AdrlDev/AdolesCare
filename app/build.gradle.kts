plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.adriele.adolescare"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.adriele.adolescare"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    viewBinding {
        enable = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        compileOptions {
            @Suppress("DEPRECATION")
            jvmTarget = "11"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material3)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ✅ Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Optional

    implementation(libs.material.calendar.view)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.facebook.shimmer)

    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.common)
    implementation(libs.exoplayer.ui)

    implementation(libs.pdfbox.android)
    implementation(libs.photoview)
    implementation(libs.pdfView)

    implementation(libs.mlkit.text.recognition)

    implementation(libs.jbcrypt)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.testing)

    implementation(libs.java.websocket)

    implementation(project(":language"))
    implementation(project(":calendarview"))
    implementation(project(":adolescal"))
    implementation(project(":settings:themes"))
}