plugins {
    id("com.android.application")
    // id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.memecio.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.memecio.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 103
        versionName = "1.03.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            val rootKeystore = file("${rootDir}/debug.keystore")
            if (rootKeystore.exists()) {
                storeFile = rootKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
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
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    // implementation(composeBom)
    // androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    // implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    // implementation("androidx.activity:activity-compose:1.9.3")

    // implementation("androidx.compose.ui:ui")
    // implementation("androidx.compose.ui:ui-graphics")
    // implementation("androidx.compose.ui:ui-tooling-preview")
    // implementation("androidx.compose.material3:material3")
    // implementation("androidx.compose.material:material-icons-extended")

    // Media3 ExoPlayer for video, audio & HLS streaming
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-common:1.5.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.0")
    implementation("androidx.media3:media3-session:1.5.0")
    implementation("androidx.media3:media3-database:1.5.0")
    implementation("androidx.media3:media3-effect:1.5.0")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("org.mozilla:rhino:1.7.14")

    // Image loading
    // implementation("io.coil-kt:coil-compose:2.7.0")

    // Networking / Document parsing
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
}
