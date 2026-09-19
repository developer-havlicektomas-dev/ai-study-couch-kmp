plugins { id("study.kmp") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":core:domain"))
}
