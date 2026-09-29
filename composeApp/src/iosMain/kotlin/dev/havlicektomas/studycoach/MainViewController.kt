package dev.havlicektomas.studycoach

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(): platform.UIKit.UIViewController {
    IosDependencyInjection.initialize()
    return ComposeUIViewController { App() }
}
