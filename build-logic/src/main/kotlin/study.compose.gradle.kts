plugins {
    id("study.kmp")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(catalog.findLibrary("compose-runtime").get())
}
