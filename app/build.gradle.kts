plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.daggerHiltAndroid)
    alias(libs.plugins.googleDevtoolsKsp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrainsKotlinKapt)
    alias(libs.plugins.room)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.shverma.androidstarter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shverma.androidstarter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}


room {
    schemaDirectory("$projectDir/schemas")
}


dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.org.jetbrains.kotlinx.coroutines.android)
    implementation(libs.androidx.multidex)

    implementation(libs.kotlinx.serialization.json)

    /*Jetpack Compose*/
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.android)
    implementation(libs.androidx.compose.ui.graphics.android)
    implementation(libs.androidx.compose.ui.tooling.preview.android)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation)

    /*Retrofit*/
    implementation(libs.com.squareup.okhttp3.logging.intercepter)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.retrofit2.retrofit)

    /*Dagger Hilt*/
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.androidx.hilt.compiler)
    kapt(libs.google.dagger.hilt.android.compiler)

    /*Data Store*/
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    /*Coil Image*/
    implementation(libs.io.coil.kt.compose)
    implementation(libs.coil.svg)

    /*JWT Token*/
    implementation(libs.org.bitbucket.b.c.jose4j)

    /*Exo-Player*/
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    /*Work Manager*/
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    /*Timber*/
    implementation(libs.jakewharton.timber)

    /*Room database*/
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    /*Testing*/
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.squareup.okhttp3.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.jetbrains.kotlin.reflect)
}


kapt {
    correctErrorTypes = true
}
