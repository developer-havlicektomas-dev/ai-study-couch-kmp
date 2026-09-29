plugins { id("study.compose") }
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    sourceSets.androidMain.dependencies { implementation(libs.ktor.okhttp) }
    sourceSets.iosMain.dependencies { implementation(libs.ktor.darwin) }
    sourceSets.commonMain.dependencies {
        api(project(":core:data"))
        implementation(project(":core:design-system"))
        implementation(project(":feature:tutor:presentation"))
    }
}

kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":feature:tutor:data"))
    implementation(libs.koin.compose)
}

kotlin.sourceSets.getByName("commonTest").dependencies {
    implementation(libs.ktor.mock)
    implementation(libs.coroutines.test)
}
