plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sushant.cpmai.mocks"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sushant.cpmai.mocks"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
            isMinifyEnabled = false
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
        viewBinding = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("com.google.android.material:material:1.12.0")
}

val copyMockAssets = tasks.register<Copy>("copyMockAssets") {
    from(rootProject.file("../mocks")) {
        include("index.html", "mock-exam-1.html", "mock-exam-2.html")
    }
    into(layout.projectDirectory.dir("src/main/assets"))
}

tasks.named("preBuild").configure { dependsOn(copyMockAssets) }
