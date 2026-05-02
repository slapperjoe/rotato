plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val buildNumber = project.findProperty("BUILD_NUMBER")?.toString()?.toIntOrNull() ?: 1

android {
    namespace = "com.rotato"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rotato"
        minSdk = 34
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
