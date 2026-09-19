tasks.register("verifyModuleBoundaries") {
    group = "verification"
    description = "Checks feature-first project dependency boundaries."
    doLast {
        val allowed = mapOf(
            ":core:domain" to emptySet<String>(),
            ":core:data" to setOf(":core:domain"),
            ":core:presentation" to setOf(":core:domain", ":core:design-system"),
            ":core:design-system" to emptySet(),
            ":feature:tutor:domain" to setOf(":core:domain"),
            ":feature:tutor:data" to setOf(":feature:tutor:domain", ":core:domain", ":core:data"),
            ":feature:tutor:presentation" to setOf(":feature:tutor:domain", ":core:domain", ":core:presentation", ":core:design-system"),
        )
        allowed.forEach { (path, permitted) ->
            project(path).configurations.forEach { configuration ->
                configuration.dependencies.withType<ProjectDependency>().forEach { dependency ->
                    check(dependency.path == path || dependency.path in permitted) { "$path cannot depend on ${dependency.path}" }
                }
            }
        }
    }
}
