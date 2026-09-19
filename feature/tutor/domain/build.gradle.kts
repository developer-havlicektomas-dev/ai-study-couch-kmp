plugins { id("study.kmp") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(project(":core:domain"))
}
