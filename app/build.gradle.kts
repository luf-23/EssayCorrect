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
    // AndroidX相关
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit网络库相关
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)

    // 测试相关
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.material.v160)

    implementation(libs.okhttp.eventsource)
    implementation(libs.okhttp.v4100)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)

    implementation(libs.okhttp)
    implementation(libs.okhttp.eventsource.v410) // 使用更新版本

    implementation(libs.core)
    implementation(libs.ext.tables)    // 表格支持
    implementation(libs.ext.strikethrough) // 删除线
    //noinspection UseTomlInstead
    implementation("io.noties.markwon:linkify:4.6.2")       // 链接自动识别

}