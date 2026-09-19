plugins { id("study.kmp") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":feature:tutor:domain"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
}
