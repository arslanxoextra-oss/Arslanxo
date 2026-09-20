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

            val keystorePath = System.getenv("CM_KEYSTORE_PATH")
            val keystorePassword = System.getenv("CM_KEYSTORE_PASSWORD")
            val alias = System.getenv("CM_KEY_ALIAS")
            val keyPassword = System.getenv("CM_KEY_PASSWORD")

            if (keystorePath.isNullOrBlank()) {
                throw GradleException(
                    """
                    CM_KEYSTORE_PATH is missing.

                    Please make sure the Android keystore is configured
                    in Codemagic Code signing settings.
                    """.trimIndent()
                )
            }

            if (keystorePassword.isNullOrBlank()) {
                throw GradleException(
                    """
                    CM_KEYSTORE_PASSWORD is missing.

                    Please check the keystore configuration in Codemagic.
                    """.trimIndent()
                )
            }

            if (alias.isNullOrBlank()) {
                throw GradleException(
                    """
                    CM_KEY_ALIAS is missing.

                    Please check the key alias in Codemagic.
                    """.trimIndent()
                )
            }

            if (keyPassword.isNullOrBlank()) {
                throw GradleException(
                    """
                    CM_KEY_PASSWORD is missing.

                    Please check the key password in Codemagic.
                    """.trimIndent()
                )
            }

            val keystoreFile = file(keystorePath)

            if (!keystoreFile.exists()) {
                throw GradleException(
                    """
                    Release signing keystore was not found.

                    Expected keystore path:
                    $keystorePath

                    Please make sure the correct Android upload keystore
                    is uploaded and selected in Codemagic Code signing.
                    """.trimIndent()
                )
            }

            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = alias
            keyPassword = keyPassword
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
