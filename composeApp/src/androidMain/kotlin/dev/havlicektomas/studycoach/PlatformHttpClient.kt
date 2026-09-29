package dev.havlicektomas.studycoach

import io.ktor.client.engine.okhttp.OkHttp

fun createPlatformEngine() = OkHttp.create()
