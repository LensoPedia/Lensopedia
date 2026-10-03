# Lensopedia - Android Photography & Media Sharing Platform

Lensopedia is an Android application engineered in **Kotlin** and **AndroidX** for visual content creators. It incorporates **Firebase Authentication**, **Firebase Cloud Firestore**, **Firebase Storage**, and **EmailJS OTP REST API (Volley)**.

---

## 📱 How to Run in AIDE (Mobile Android IDE)
AIDE allows you to build, compile, and run Kotlin/Java Android apps directly on your Android phone without needing a PC.

### Step 1: Download or Extract Project
1. Click the **"Download Project (.ZIP)"** button in the Lensopedia Code Center.
2. Transfer or extract the zip file to your phone's storage, e.g.:
   `/storage/emulated/0/AppProjects/Lensopedia`

### Step 2: Open in AIDE
1. Launch **AIDE** on your Android phone.
2. Tap **Menu** -> **Open Project...**
3. Browse to the `Lensopedia` directory and select `build.gradle` or `app`.
4. AIDE will index the project and load all Kotlin files and XML layouts.

### Step 3: Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a project and add an Android app with package name:
   `lensopedia.myapp`
3. Download `google-services.json` and place it inside the `app/` folder.
4. In Firebase Console:
   - Enable **Authentication** -> **Email/Password**.
   - Enable **Cloud Firestore** in test or production mode.
   - Enable **Cloud Storage** for image uploads.

### Step 4: EmailJS OTP Setup (Optional for Live Emails)
1. Register for free at [EmailJS.com](https://www.emailjs.com/).
2. Create an Email Service (e.g. Gmail) -> get `SERVICE_ID`.
3. Create an Email Template with parameter `{{otp_code}}` -> get `TEMPLATE_ID`.
4. In `RegisterActivity.kt`, paste your Service ID, Template ID, and Public Key:
   ```kotlin
   private val EMAILJS_SERVICE_ID = "YOUR_SERVICE_ID"
   private val EMAILJS_TEMPLATE_ID = "YOUR_TEMPLATE_ID"
   private val EMAILJS_USER_ID = "YOUR_PUBLIC_KEY"
   ```
   *(Note: If you run without custom keys, the app displays a Toast with the 6-digit OTP code directly on screen for testing).*

### Step 5: Build and Run
1. Tap the **Play / Run ▶** button in AIDE.
2. AIDE will compile Kotlin sources, package the APK, and prompt you to install and launch Lensopedia!

---

## 💻 How to Run in Android Studio (PC / Mac / Linux)
1. Open Android Studio -> **File** -> **Open**.
2. Select the `Lensopedia` root folder.
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator or physical device (Min SDK 23 / Android 6.0+).
5. Click **Run 'app'** (`Shift + F10`).

---

## 🏗️ Architecture & Highlights
- **No Synthetic Binding**: Standard `findViewById` ensures zero compatibility bugs in mobile AIDE compilers.
- **Asynchronous Feedback**: Every Firebase call includes `.addOnSuccessListener` and `.addOnFailureListener` with informative `Toast` notifications.
- **Volley Integration**: Sends clean JSON HTTP POST to EmailJS REST endpoints for OTP verification.
- **Storage & Firestore Sync**: Automatically retrieves image download URLs upon Storage upload and links them to Firestore `Posts` documents.
