import SwiftUI
import LocalAuthentication

public struct SettingsView: View {
    @AppStorage("biometricsEnabled") private var biometricsEnabled: Bool = false
    @AppStorage("soundscapesEnabled") private var soundscapesEnabled: Bool = true
    @State private var showingExportAlert: Bool = false
    @State private var showingBiometricError: Bool = false
    @State private var biometricErrorMessage: String = ""

    public var body: some View {
        NavigationView {
            ZStack {
                FormaTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        // Profile / Sanctuary Emblem
                        VStack(spacing: 8) {
                            ZStack {
                                Circle()
                                    .fill(FormaTheme.surfaceElevated)
                                    .frame(width: 64, height: 64)
                                    .overlay(
                                        Circle()
                                            .stroke(FormaTheme.accent, lineWidth: 1.5)
                                    )

                                Image(systemName: "sparkles")
                                    .font(.system(size: 24))
                                    .foregroundColor(FormaTheme.accent)
                            }

                            Text("Forma Sanctuary")
                                .font(.system(size: 18, weight: .bold, design: .serif))
                                .foregroundColor(FormaTheme.textPrimary)

                            Text("Tailored for iPhone 6s Plus • iOS 15")
                                .font(.system(size: 12))
                                .foregroundColor(FormaTheme.textMuted)
                        }
                        .padding(.top, 12)

                        // Privacy & Biometrics
                        VStack(alignment: .leading, spacing: 12) {
                            Text("SECURITY & BIOMETRICS")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            Toggle(isOn: $biometricsEnabled) {
                                HStack(spacing: 12) {
                                    Image(systemName: "touchid")
                                        .font(.system(size: 18))
                                        .foregroundColor(FormaTheme.accent)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Touch ID / Passcode")
                                            .font(.system(size: 15, weight: .medium))
                                            .foregroundColor(FormaTheme.textPrimary)
                                        Text("Lock private reflection notes")
                                            .font(.system(size: 12))
                                            .foregroundColor(FormaTheme.textMuted)
                                    }
                                }
                            }
                            .onChange(of: biometricsEnabled) { enabled in
                                if enabled {
                                    requestBiometricAuth()
                                }
                            }
                        }
                        .padding(16)
                        .background(FormaTheme.surface)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(FormaTheme.border, lineWidth: 1)
                        )

                        // Sensory Experience
                        VStack(alignment: .leading, spacing: 12) {
                            Text("SENSORY & SOUND")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            Toggle(isOn: $soundscapesEnabled) {
                                HStack(spacing: 12) {
                                    Image(systemName: "speaker.wave.2.fill")
                                        .font(.system(size: 18))
                                        .foregroundColor(FormaTheme.accent)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Zen Soundscapes")
                                            .font(.system(size: 15, weight: .medium))
                                            .foregroundColor(FormaTheme.textPrimary)
                                        Text("Acoustic feedback on completion")
                                            .font(.system(size: 12))
                                            .foregroundColor(FormaTheme.textMuted)
                                    }
                                }
                            }
                        }
                        .padding(16)
                        .background(FormaTheme.surface)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(FormaTheme.border, lineWidth: 1)
                        )

                        // Data Export
                        VStack(alignment: .leading, spacing: 12) {
                            Text("DATA & BACKUP")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            Button(action: { showingExportAlert = true }) {
                                HStack {
                                    Image(systemName: "square.and.arrow.up")
                                        .font(.system(size: 16))
                                    Text("Export JSON Backup")
                                        .font(.system(size: 14, weight: .medium))
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 12))
                                }
                                .foregroundColor(FormaTheme.textPrimary)
                            }
                        }
                        .padding(16)
                        .background(FormaTheme.surface)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(FormaTheme.border, lineWidth: 1)
                        )

                        // App Version footer
                        VStack(spacing: 4) {
                            Text("Forma iOS v2.0.0 (Build 1)")
                                .font(.system(size: 12, design: .monospaced))
                                .foregroundColor(FormaTheme.textMuted)
                            Text("Crafted for iPhone 6s Plus • Zero-Cloud Local First")
                                .font(.system(size: 11))
                                .foregroundColor(FormaTheme.textMuted.opacity(0.7))
                        }
                        .padding(.top, 16)
                    }
                    .padding(16)
                }
            }
            .navigationBarTitle("Sanctuary Settings", displayMode: .inline)
            .alert(isPresented: $showingExportAlert) {
                Alert(
                    title: Text("Data Safe & Local"),
                    message: Text("Your habits and completion history are stored securely on this iPhone's local flash storage."),
                    dismissButton: .default(Text("OK"))
                )
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }

    private func requestBiometricAuth() {
        let context = LAContext()
        var error: NSError?

        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "Authenticate with Touch ID to secure your sanctuary"
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, _ in
                DispatchQueue.main.async {
                    if !success {
                        biometricsEnabled = false
                    }
                }
            }
        } else {
            biometricsEnabled = false
        }
    }
}
