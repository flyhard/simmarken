import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import java.util.Base64
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.play.publisher)
}

data class ReleaseSigningCredentials(
    val storeFile: java.io.File,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

fun Project.resolveReleaseSigningCredentials(): ReleaseSigningCredentials? {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        val props = Properties().apply { propsFile.inputStream().use { load(it) } }
        val storeFilePath = props.getProperty("storeFile") ?: return null
        val storeFile = rootProject.file(storeFilePath)
        val storePassword = props.getProperty("storePassword")
        val keyAlias = props.getProperty("keyAlias")
        val keyPassword = props.getProperty("keyPassword")
        if (storeFile.exists() && !storePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
        ) {
            return ReleaseSigningCredentials(storeFile, storePassword, keyAlias, keyPassword)
        }
        return null
    }

    val storePassword = System.getenv("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD")

    // Local builds with 1Password (ADR-0019): the keystore file is downloaded from
    // 1Password to a path outside the checkout, and `op run --env-file=release.env`
    // injects the passwords for the duration of the build only.
    val keystorePath = System.getenv("ANDROID_KEYSTORE_FILE")?.trim().orEmpty()
    if (keystorePath.isNotEmpty() && !storePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
    ) {
        val expanded = if (keystorePath == "~" || keystorePath.startsWith("~/")) {
            System.getProperty("user.home") + keystorePath.removePrefix("~")
        } else {
            keystorePath
        }
        val storeFile = rootProject.file(expanded)
        if (!storeFile.exists()) {
            throw GradleException(
                "ANDROID_KEYSTORE_FILE points to $storeFile, which does not exist. " +
                    "Download the keystore from 1Password first (see release.env).",
            )
        }
        return ReleaseSigningCredentials(storeFile, storePassword, keyAlias, keyPassword)
    }

    // CI: the keystore arrives base64-encoded in a secret and is decoded into build/.
    val base64 = System.getenv("ANDROID_KEYSTORE_BASE64")?.trim().orEmpty()
    if (base64.isNotEmpty() && !storePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
    ) {
        val decoded = layout.buildDirectory.file("signing/ci-upload.jks").get().asFile
        decoded.parentFile.mkdirs()
        decoded.writeBytes(Base64.getDecoder().decode(base64))
        return ReleaseSigningCredentials(decoded, storePassword, keyAlias, keyPassword)
    }
    return null
}

android {
    namespace = "se.simmarken"
    compileSdk = 36

    defaultConfig {
        applicationId = "se.simmarken"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseSigning = resolveReleaseSigningCredentials()

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                storeFile = releaseSigning.storeFile
                storePassword = releaseSigning.storePassword
                keyAlias = releaseSigning.keyAlias
                keyPassword = releaseSigning.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Play uploads (ADR-0014, ADR-0020): only .github/workflows/release.yml publishes.
// It writes the service-account JSON outside the checkout and passes its path in
// PLAY_SERVICE_ACCOUNT_FILE. AUTO asks Play for the highest versionCode already
// uploaded and uses the next one; `versionCode` in defaultConfig only applies to
// local builds and the manual first upload (ADR-0015). Without the file the
// plugin is off, so local `bundleRelease` never needs Play credentials.
val playServiceAccountFile = System.getenv("PLAY_SERVICE_ACCOUNT_FILE")?.trim().orEmpty()

play {
    enabled.set(playServiceAccountFile.isNotEmpty())
    if (playServiceAccountFile.isNotEmpty()) {
        serviceAccountCredentials.set(file(playServiceAccountFile))
    }
    track.set("internal")
    defaultToAppBundles.set(true)
    resolutionStrategy.set(ResolutionStrategy.AUTO)
}

gradle.taskGraph.whenReady {
    val isReleaseBuild = allTasks.any { task ->
        val n = task.name
        n == "bundleRelease" || n == "assembleRelease" ||
            n.endsWith("BundleRelease") || n.endsWith("AssembleRelease") ||
            n.endsWith("ReleaseBundle") // signReleaseBundle, publishReleaseBundle (ADR-0020)
    }
    if (isReleaseBuild) {
        val cfg = android.signingConfigs.findByName("release")
        val store = cfg?.storeFile
        if (cfg == null || store == null || !store.exists()) {
            throw GradleException(
                """
                Release signing is not configured.
                Local (1Password): op run --env-file=release.env -- ./gradlew bundleRelease
                Local (file): copy keystore.properties.example → keystore.properties and run scripts/generate-upload-keystore.sh
                CI: set ANDROID_KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
                See ADR-0019 for details.
                """.trimIndent(),
            )
        }
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(libs.versions.kotlin.get())
            because("Align Kotlin artifacts with project compiler")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
