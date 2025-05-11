plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xjyzs.zhixue"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xjyzs.zhixue"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        androidResources.localeFilters += listOf("zh")
    }
    signingConfigs {
        create("release") {
            storeFile = file("${project.rootDir}/keystore.jks")
            storePassword = System.getenv("KEY_STORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            enableV1Signing=false
        }
    }

    flavorDimensions += "abi"
    productFlavors {
        create("x86") {
            dimension = "abi"
            ndk { abiFilters.add("x86") }
            signingConfig = signingConfigs.getByName("release")
        }
        create("x86_64") {
            dimension = "abi"
            ndk { abiFilters.add("x86_64") }
            signingConfig = signingConfigs.getByName("release")
        }
        create("arm") {
            dimension = "abi"
            ndk { abiFilters.add("armeabi-v7a") }
            signingConfig = signingConfigs.getByName("release")
        }
        create("arm64") {
            dimension = "abi"
            ndk { abiFilters.add("arm64-v8a") }
            signingConfig = signingConfigs.getByName("release")
        }
        create("universal") {
            dimension = "abi"
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources=true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            packaging {
                resources {
                    excludes += setOf(
                        "DebugProbesKt.bin",
                        "kotlin-tooling-metadata.json",
                        "okhttp3/**",
                        "META-INF/*version*"
                    )
                }
            }
            androidResources {
                noCompress += setOf("so", "arsc")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)

    // 可选：OkHttp 日志拦截器，用于调试网络请求
    implementation(libs.logging.interceptor)

    // 协程支持（如需处理异步调用）
    implementation(libs.kotlinx.coroutines.android)

    //okhttp
    implementation(libs.okhttp)

    //json
    implementation(libs.gson)

    //Coil
    implementation(libs.coil.compose)

    implementation (libs.androidx.material.v161)
    implementation(libs.subsampling.scale.image.view.androidx)
}