plugins { id("study.compose") }
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    sourceSets.commonMain.dependencies {
        implementation(project(":core:design-system"))
        implementation(project(":feature:tutor:presentation"))
    }
}
