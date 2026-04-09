plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.calorite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.calorite"
        minSdk = 24
        targetSdk = 36
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

    packaging {
        resources {
            // Mengabaikan file INDEX.LIST yang duplikat
            excludes += "/META-INF/INDEX.LIST"

            // Kadang file-file ini juga ikut duplikat saat pakai Google AI SDK
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }
    }
}

dependencies {
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation(libs.room.runtime)
    implementation(libs.play.services.tasks)
    implementation(libs.generativeai)
    annotationProcessor(libs.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}