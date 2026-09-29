package dev.havlicektomas.studycoach

import android.app.Application
import dev.havlicektomas.studycoach.core.data.NetworkConfig
import org.koin.core.context.startKoin

class StudyCoachApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin { modules(studyCoachModules(
            NetworkConfig(BuildConfig.API_BASE_URL, BuildConfig.DEBUG), createPlatformEngine(),
        )) }
    }
}
