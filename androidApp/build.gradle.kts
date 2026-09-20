import java.net.URI

plugins { id("study.android.application") }
dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
}

val developmentApiUrl = providers.gradleProperty("studyCoach.devApiUrl").orElse("http://10.0.2.2:8000")
val productionApiUrl = providers.gradleProperty("studyCoach.apiUrl").orElse("https://example.invalid")
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
android {
    buildFeatures { buildConfig = true }
    buildTypes {
        getByName("debug") { buildConfigField("String", "API_BASE_URL", quoted(developmentApiUrl.get())) }
        getByName("release") { buildConfigField("String", "API_BASE_URL", quoted(productionApiUrl.get())) }
    }
}
val validateReleaseApiUrl by tasks.registering {
    doLast {
        val url = URI(productionApiUrl.get())
        check(url.scheme == "https" && !url.host.isNullOrBlank() && url.host != "example.invalid" &&
            url.userInfo == null && url.query == null && url.fragment == null) {
            "Release requires -PstudyCoach.apiUrl=https://your-api-host"
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(validateReleaseApiUrl) }
