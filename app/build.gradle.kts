plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.essaycorrect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.essaycorrect"
        minSdk = 26
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
}

dependencies {
    // AndroidX核心库
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 网络请求库
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)

    // SSE支持
    implementation(libs.okhttp.eventsource.v410)

    // Markdown渲染
    implementation(libs.core)
    implementation(libs.ext.tables)
    implementation(libs.ext.strikethrough)
    implementation("io.noties.markwon:linkify:4.6.2")

    // 测试库
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}