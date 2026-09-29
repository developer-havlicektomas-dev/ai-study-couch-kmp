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

kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
}

kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.koin.viewmodel)
    api(libs.lifecycle.viewmodel)
    api(libs.lifecycle.savedstate)
    implementation(libs.lifecycle.compose)
}

kotlin.sourceSets.getByName("commonTest").dependencies { implementation(libs.coroutines.test) }
