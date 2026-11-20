import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let mode = Bundle.main.object(forInfoDictionaryKey: "BeFairAppMode") as? String
        return MainViewControllerKt.MainViewController(appMode: mode)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



