import SwiftUI
import ComposeApp

@main
struct StudyCoachApp: App {
    var body: some Scene {
        WindowGroup { ComposeView().ignoresSafeArea() }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
