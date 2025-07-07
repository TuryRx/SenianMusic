plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("kotlin-kapt")
    alias(libs.plugins.ksp)                 // Usa el alias que creaste
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.senianmusic"
    compileSdk = 34 // Usa una SDK reciente

    defaultConfig {
        applicationId = "com.example.senianmusic"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Necesario para Room
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true // Facilita el acceso a las vistas
    }
}

dependencies {

    // --- UI (Leanback para TV) ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- ViewModel y LiveData/StateFlow (MVVM) ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // --- Coroutines (para operaciones asíncronas) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Networking (Retrofit para API de Navidrome) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // O moshi
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Para depurar llamadas

    // --- Persistencia de datos ---
    // DataStore para guardar URL del servidor y token de sesión
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // Room para base de datos local (favoritos, descargas)
    implementation("androidx.room:room-runtime:2.6.1")
    //kapt("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("androidx.room:room-ktx:2.6.1")

    // --- Reproductor de Música (ExoPlayer ahora es Media3) ---
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")

    // --- Carga de imágenes (Coil es moderno y basado en coroutines) ---
    implementation("io.coil-kt:coil:2.5.0")

    // Librería para transformaciones de Glide (desenfoque, etc.)
    implementation("jp.wasabeef:glide-transformations:4.3.0")
}