package dev.havlicektomas.studycoach

import android.app.Application
import dev.havlicektomas.studycoach.core.data.NetworkConfig

class StudyCoachApplication : Application() {
    /** Application-scoped instance; KMP-05 will inject it into the feature. */
    val httpClient by lazy {
        createPlatformHttpClient(NetworkConfig(BuildConfig.API_BASE_URL, BuildConfig.DEBUG))
    }
}
