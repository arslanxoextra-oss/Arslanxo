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

        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 6
        versionName = System.getenv("VERSION_NAME") ?: "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {

            val codemagicKeystorePath =
                System.getenv("CM_KEYSTORE_PATH")

            val customKeystorePath =
                System.getenv("KEYSTORE_PATH")

            val keystorePath =
                codemagicKeystorePath
                    ?: customKeystorePath

            val signingStorePassword =
                System.getenv("CM_KEYSTORE_PASSWORD")
                    ?: System.getenv("STORE_PASSWORD")

            val signingKeyAlias =
                System.getenv("CM_KEY_ALIAS")
                    ?: System.getenv("KEY_ALIAS")
                    ?: "newapp-gzihyu"

            val signingKeyPassword =
                System.getenv("CM_KEY_PASSWORD")
                    ?: System.getenv("KEY_PASSWORD")

            val localKeystore =
                file("${rootDir}/my-upload-key.jks")

            if (
                !keystorePath.isNullOrBlank() &&
                !signingStorePassword.isNullOrBlank() &&
                !signingKeyPassword.isNullOrBlank()
            ) {
                val codemagicKeystore =
                    file(keystorePath)

                if (!codemagicKeystore.exists()) {
                    throw GradleException(
                        """
                        Release keystore was not found.

                        Path:
                        $keystorePath

                        Please check the Android keystore configuration
                        in Codemagic.
                        """.trimIndent()
                    )
                }

                storeFile = codemagicKeystore
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword

            } else if (
                localKeystore.exists() &&
                !signingStorePassword.isNullOrBlank() &&
                !signingKeyPassword.isNullOrBlank()
            ) {
                storeFile = localKeystore
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword

            } else {
                throw GradleException(
                    """
                    Release signing configuration is missing.

                    Please make sure the following are configured
                    in Codemagic:

                    CM_KEYSTORE_PATH
                    CM_KEYSTORE_PASSWORD
                    CM_KEY_ALIAS
                    CM_KEY_PASSWORD

                    Or make sure my-upload-key.jks exists in the
                    project root with the correct signing credentials.
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

            signingConfig =
                signingConfigs.getByName("release")
        }

        debug {
            signingConfig =
                signingConfigs.getByName("debugConfig")
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

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices {
    missingGoogleServicesStrategy =
        MissingGoogleServicesStrategy.WARN
}

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

    // Firebase Auth / Google Sign-In
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
