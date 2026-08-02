import FirebaseCore
import GoogleSignIn
import SwiftUI
import UIKit
import ComposeApp

private final class GoogleSignInBridge {
    static let shared = GoogleSignInBridge()

    private var requestObserver: NSObjectProtocol?

    private init() {}

    func install() {
        guard requestObserver == nil else { return }

        requestObserver = NotificationCenter.default.addObserver(
            forName: Notification.Name("com.shverma.kinetic.googleSignInRequest"),
            object: nil,
            queue: .main,
        ) { [weak self] notification in
            self?.handleSignInRequest(notification)
        }
    }

    func configureFirebaseIfAvailable() {
        guard FirebaseApp.app() == nil else { return }
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let options = FirebaseOptions(contentsOfFile: path) else {
            print("Kinetic: GoogleService-Info.plist is not present; Firebase runtime is not configured.")
            return
        }
        FirebaseApp.configure(options: options)
    }

    private func handleSignInRequest(_ notification: Notification) {
        guard let requestId = notification.userInfo?["requestId"] as? String else { return }
        guard let clientId = googleClientId() else {
            complete(
                requestId: requestId,
                idToken: nil,
                errorMessage: "The iOS OAuth client ID is missing from Xcode configuration and GoogleService-Info.plist.",
                cancelled: false,
            )
            return
        }
        guard let presentingViewController = topViewController() else {
            complete(
                requestId: requestId,
                idToken: nil,
                errorMessage: "No active iOS view controller is available for Google Sign-In.",
                cancelled: false,
            )
            return
        }

        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientId)
        GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
            let nsError = error as NSError?
            self.complete(
                requestId: requestId,
                idToken: result?.user.idToken?.tokenString,
                errorMessage: nsError?.localizedDescription,
                cancelled: nsError?.domain == kGIDSignInErrorDomain && nsError?.code == -5,
            )
        }
    }

    private func googleClientId() -> String? {
        if let configuredClientId = Bundle.main.object(
            forInfoDictionaryKey: "GOOGLE_IOS_CLIENT_ID"
        ) as? String,
           !configuredClientId.isEmpty {
            return configuredClientId
        }

        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let values = NSDictionary(contentsOfFile: path) as? [String: Any],
              let clientId = values["CLIENT_ID"] as? String,
              !clientId.isEmpty else {
            return nil
        }
        return clientId
    }

    private func complete(
        requestId: String,
        idToken: String?,
        errorMessage: String?,
        cancelled: Bool,
    ) {
        IosGoogleSignInBridge_iosKt.completeIosGoogleSignIn(
            requestId: requestId,
            idToken: idToken,
            errorMessage: errorMessage,
            cancelled: cancelled,
        )
    }

    private func topViewController() -> UIViewController? {
        let root = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?.rootViewController
        return topViewController(from: root)
    }

    private func topViewController(from viewController: UIViewController?) -> UIViewController? {
        if let presented = viewController?.presentedViewController {
            return topViewController(from: presented)
        }
        if let navigation = viewController as? UINavigationController {
            return topViewController(from: navigation.visibleViewController)
        }
        if let tab = viewController as? UITabBarController {
            return topViewController(from: tab.selectedViewController)
        }
        return viewController
    }
}

@main
struct iOSApp: App {
	init() {
		GoogleSignInBridge.shared.install()
		GoogleSignInBridge.shared.configureFirebaseIfAvailable()
	}

	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in
					GIDSignIn.sharedInstance.handle(url)
				}
		}
	}
}
