plugins { id("study.compose") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.resources)
    implementation(libs.compose.preview)
}
