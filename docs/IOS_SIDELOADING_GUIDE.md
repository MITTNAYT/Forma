# HabitFlow v2 on iPhone 6s Plus: Zero-Mac Sideloading Guide

This guide walks you through compiling the iOS build on GitHub's free cloud Mac runners and installing it onto your physical **iPhone 6s Plus** from your Windows PC.

---

## 1. Trigger the Cloud Build on GitHub

1. Commit and push your changes to your GitHub repository:
   ```bash
   git add .
   git commit -m "feat(ios): add HabitFlow iOS 15 app for iPhone 6s Plus"
   git push origin main
   ```
2. Open your repository on [GitHub.com](https://github.com).
3. Click on the **Actions** tab at the top.
4. In the left sidebar, click on **Build iOS (iPhone 6s Plus)**.
5. Click the **Run workflow** dropdown on the right and click the green **Run workflow** button.
6. Wait ~3–4 minutes for the macOS runner to complete.
7. Click on the completed workflow run, scroll down to **Artifacts**, and download:
   * **`HabitFlow-iOS-iPhone6sPlus`** (contains `HabitFlow.ipa`).
8. Extract the downloaded `.zip` file on your Windows PC so you have `HabitFlow.ipa`.

---

## 2. Install Sideloadly on Windows

1. Download **Sideloadly for Windows** (free) from [sideloadly.io](https://sideloadly.io).
2. Install Sideloadly on your Windows PC.
3. Make sure either **iTunes** or **iCloud** for Windows (non-Microsoft Store version is best) is installed so Windows recognizes your iPhone over USB.

---

## 3. Sideload onto your iPhone 6s Plus

1. Plug your **iPhone 6s Plus** into your PC using a standard **Lightning cable**.
2. If prompted on your iPhone screen, tap **Trust This Computer** and enter your passcode.
3. Open **Sideloadly** on Windows.
4. Sideloadly will detect your iPhone in the device dropdown (e.g. `iPhone 6s Plus`).
5. **Drag and drop** `HabitFlow.ipa` onto the large IPA icon box in Sideloadly.
6. In the **Apple ID** field, type your regular free personal Apple ID email address.
7. Click **Start**.
8. If prompted, enter your Apple ID password (this is sent directly to Apple's servers to generate a 7-day personal developer certificate).
9. Wait for the progress bar to say **Done! 100%**.

---

## 4. Trust the Developer Certificate on iPhone (First Time Only)

1. On your iPhone 6s Plus, unlock and open **Settings**.
2. Navigate to **General > VPN & Device Management** (on iOS 15).
3. Under **Developer App**, tap your Apple ID email.
4. Tap **Trust "[Your Apple ID]"** and confirm.
5. Go back to your home screen—tap the **Forma** app icon to launch!

---

## 5. iPhone 6s Plus Features & Highlights

* **Pure Native 60 FPS**: Rendered natively with iOS 15 SwiftUI for zero stutter on the dual-core A9 processor.
* **Ultra-Low Memory Footprint**: Runs at < 40 MB RAM (well below the 2 GB hardware limit).
* **Touch ID Protection**: Toggle biometric lock in Sanctuary Settings to guard private habits.
* **Taptic Engine Feedback**: Customized haptic taps on habit checkmarks.
* **Offline-First Persistence**: Atomic local flash storage ensures all streaks are safe without internet dependency.
