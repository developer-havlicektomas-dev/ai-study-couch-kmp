plugins { id("study.android.application") }
dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
}
