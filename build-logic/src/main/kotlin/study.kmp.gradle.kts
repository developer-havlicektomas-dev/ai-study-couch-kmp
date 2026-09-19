import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
kotlin {
    jvmToolchain(catalog.findVersion("java").get().requiredVersion.toInt())
    extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        namespace = "dev.havlicektomas.studycoach" + project.path.replace(":", ".").replace("-", "")
        compileSdk = catalog.findVersion("compileSdk").get().requiredVersion.toInt()
        minSdk = catalog.findVersion("minSdk").get().requiredVersion.toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
        androidResources { enable = true }
        withHostTestBuilder {}.configure {}
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets.commonTest.dependencies { implementation(catalog.findLibrary("kotlin-test").get()) }
}
