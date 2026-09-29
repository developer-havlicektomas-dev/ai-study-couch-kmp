plugins { `kotlin-dsl` }
kotlin { jvmToolchain(libs.versions.java.get().toInt()) }
dependencies {
    implementation(libs.kotlin.serialization.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.compose.compiler.plugin)
}
