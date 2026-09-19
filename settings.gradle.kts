pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "AIStudyCoach"
include(":androidApp", ":composeApp")
include(":core:domain", ":core:data", ":core:presentation", ":core:design-system")
include(":feature:tutor:domain", ":feature:tutor:data", ":feature:tutor:presentation")
