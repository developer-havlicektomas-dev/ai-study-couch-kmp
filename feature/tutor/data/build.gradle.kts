plugins {
    id("study.kmp")
    id("study.serialization")
}
kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":feature:tutor:domain"))
        implementation(project(":core:domain"))
        implementation(project(":core:data"))
        api(libs.ktor.core)
        implementation(libs.ktor.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(libs.ktor.mock)
        implementation(libs.coroutines.test)
    }
}
