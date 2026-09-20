package dev.havlicektomas.studycoach.core.data

import io.ktor.http.URLProtocol
import io.ktor.http.Url

/** Build configuration, not learner input. Invalid configuration fails before a request is sent. */
class NetworkConfig(baseUrl: String, val development: Boolean = false) {
    val baseUrl: String
    init {
        require(baseUrl.startsWith("https://") || (development && baseUrl.startsWith("http://"))) {
            "API URL must use HTTPS outside development builds"
        }
        val parsed = Url(baseUrl)
        require(parsed.host.isNotBlank() && parsed.user == null && parsed.password == null &&
            parsed.parameters.isEmpty() && parsed.fragment.isEmpty()) { "API URL must not contain credentials, query or fragment" }
        require(parsed.protocol == URLProtocol.HTTPS || (development && parsed.protocol == URLProtocol.HTTP))
        this.baseUrl = baseUrl.trimEnd('/') + "/"
    }
}
