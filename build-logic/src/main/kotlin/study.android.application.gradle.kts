plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
android {
    namespace = "dev.havlicektomas.studycoach"
    compileSdk = catalog.findVersion("compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        applicationId = "dev.havlicektomas.studycoach"
        minSdk = catalog.findVersion("minSdk").get().requiredVersion.toInt()
        targetSdk = catalog.findVersion("targetSdk").get().requiredVersion.toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { compose = true }
}
