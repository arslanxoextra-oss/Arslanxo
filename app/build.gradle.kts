import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.arslanaziz.promptxo"
        minSdk = 24
        targetSdk = 36

        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 2
        versionName = System.getenv("VERSION_NAME") ?: "1.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            /*
             * Codemagic کے Android signing variables:
             *
             * CM_KEYSTORE_PATH
             * CM_KEYSTORE_PASSWORD
             * CM_KEY_ALIAS
             * CM_KEY_PASSWORD
             */

            val codemagicKeystorePath =
                System.getenv("CM_KEYSTORE_PATH")

            val customKeystorePath =
                System.getenv("KEYSTORE_PATH")

            val keystorePath =
                codemagicKeystorePath
                    ?: customKeystorePath

            val keystorePassword =
                System.getenv("CM_KEYSTORE_PASSWORD")
                    ?: System.getenv("STORE_PASSWORD")

            val alias =
                System.getenv("CM_KEY_ALIAS")
                    ?: System.getenv("KEY_ALIAS")
                    ?: "newapp-gzihyu"

            val password =
                System.getenv("CM_KEY_PASSWORD")
                    ?: System.getenv("KEY_PASSWORD")

            if (
                !keystorePath.isNullOrBlank() &&
                !keystorePassword.isNullOrBlank() &&
                !password.isNullOrBlank() &&
                file(keystorePath).exists()
            ) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = alias
                keyPassword = password
            } else if (
                file("${rootDir}/my-upload-key.jks").exists() &&
                !keystorePassword.isNullOrBlank() &&
                !password.isNullOrBlank()
            ) {
                storeFile = file("${rootDir}/my-upload-key.jks")
                storePassword = keystorePassword
                keyAlias = alias
                keyPassword = password
            } else {
                throw GradleException(
                    """
                    Release signing keystore نہیں ملی۔

                    Codemagic میں یہ variables چیک کریں:
                    CM_KEYSTORE_PATH
                    CM_KEYSTORE_PASSWORD
                    CM_KEY_ALIAS
                    CM_KEY_PASSWORD

                    اور یقینی بنائیں کہ اصل upload keystore موجود ہے۔
                    """.trimIndent()
                )
            }
        }

        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

/*
 * Secrets Gradle Plugin configuration
 */
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

/*
 * Firebase Google Services configuration
 */
googleServices {
    missingGoogleServicesStrategy =
        MissingGoogleServicesStrategy.WARN
}

/*
 * Dependencies
 */
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(libs.coil.compose)

    implementation(libs.converter.moshi)

    implementation(libs.firebase.ai)
    implementation(libs.firebase.firestore)

    implementation(libs.play.services.ads)

    /*
     * Firebase Auth اور Google Sign-In کے لیے ضرورت ہونے پر
     * یہ چاروں dependencies ایک ساتھ uncomment کریں۔
     */
    // implementation(libs.firebase.auth)
    // implementation(libs.androidx.credentials)
    // implementation(libs.androidx.credentials.play.services)
    // implementation(libs.googleid)

    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.firebase.appcheck.debug)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.logging.interceptor)

    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)

    implementation(libs.retrofit)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}
