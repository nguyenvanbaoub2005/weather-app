plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.textn"
    compileSdk = 35
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.textn"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
dependencies {
    // **1️⃣ Google Maps SDK (Hiển thị bản đồ)**
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1") // Lấy vị trí GPS
    // **2️⃣ Retrofit (Gọi API thời tiết)**
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Chuyển đổi JSON sang Kotlin
    // **3️⃣ ViewModel & LiveData (MVVM)**
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // **4️⃣ Gson (Chuyển đổi JSON)**
    implementation("com.google.code.gson:gson:2.9.0")
}
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
dependencies{
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.5")

}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    // Firebase Auth + Google Sign-In
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
//Material Design
    implementation ("com.google.android.material:material:1.11.0")
}
dependencies {
    // Các dependencies WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.8.1")
}
// Firebase Cloud Messaging
dependencies {
    // Firebase Core
    implementation ("com.google.firebase:firebase-core:21.1.1")
    // Firebase Cloud Messaging
    implementation ("com.google.firebase:firebase-messaging:23.3.1")
}


dependencies {
    // Các dependencies mặc định của bạn...

    // Material Components
    implementation ("com.google.android.material:material:1.9.0")

    // ConstraintLayout
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // Facebook Shimmer (hiệu ứng loading)
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    dependencies {
        implementation ("io.noties.markwon:core:4.6.2")
        // Nếu bạn muốn thêm một số tính năng khác, có thể thêm các module sau
        implementation ("io.noties.markwon:ext-strikethrough:4.6.2")
        implementation ("io.noties.markwon:ext-tables:4.6.2")
    }
}

buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")// mới nhất
    }

}


//dependencies {
//    // Google Maps
//    implementation ("com.google.android.gms:play-services-maps:18.1.0")
//    // Retrofit
//    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
//    // Coroutine
//    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4
//}
