plugins { id("study.kmp") }
kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:domain"))
        api(libs.ktor.core)
        implementation(libs.ktor.json)
        implementation(libs.ktor.negotiation)
    }
    sourceSets.iosMain.dependencies { implementation(libs.ktor.darwin) }
    sourceSets.commonTest.dependencies {
        implementation(libs.ktor.mock)
        implementation(libs.coroutines.test)
    }
}
