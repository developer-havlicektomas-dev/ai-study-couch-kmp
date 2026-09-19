plugins { id("study.compose") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":feature:tutor:domain"))
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.resources)
    implementation(libs.compose.preview)
}

compose.resources {
    packageOfResClass = "dev.havlicektomas.studycoach.tutor.resources"
}
