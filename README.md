# FraudGuard BD SMS Shield 🛡️

**FraudGuard BD SMS Shield** is a high-security Android application developed in Java, specifically engineered to protect users in Bangladesh from the rising wave of fraudulent SMS messages. It was built for the **BuildFest** demo to promote digital safety awareness.

The app features a unique two-tier detection architecture: a high-speed, private **Local Rule-Based Engine** and an optional, advanced **Gemini AI Analysis** layer.

---

## 🚀 Key Features

- **Automated Background Scanning:** Real-time monitoring of incoming SMS using `BroadcastReceiver`.
- **Manual "Check & Paste":** Users can manually verify suspicious messages from any app.
- **Bangla-Optimized Local Engine:** Detects common signatures like bKash/Nagad scams, fake prizes, job fraud, and legal threats without an internet connection.
- **Optional Gemini 1.5 Flash Integration:** Leverages cutting-edge AI for deep linguistic and contextual analysis.
- **Smart Alerts:** Instant high-priority notifications for Medium and High-risk messages.
- **Safe Reply Generator:** Provides pre-written safe responses to handle scammers.
- **Privacy-First Design:** Gemini cloud scanning is **OFF by default** and requires explicit user consent.

---

## 🛠️ Setup Instructions

### 1. Prerequisites
- **Android Studio** (Hedgehog or newer recommended).
- **Android Device/Emulator** (API Level 23 or higher).
- **Gemini API Key** (Optional, for AI features).

### 2. Clone and Open
1. Download or clone this repository.
2. Open Android Studio and select **"Open"**.
3. Navigate to the `FraudGuardBD` folder and click **OK**.

### 3. Gemini API Key Configuration (Optional)
The app works perfectly offline using local rules. To enable AI analysis:
1. Locate the `local.properties` file in your project's root directory.
2. Add the following line at the end:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Android Studio will automatically inject this key into the build process via `BuildConfig`. **Never commit your real key to GitHub.**

---

## 🏃 How to Run the App

1. Connect your Android device via USB or start an emulator.
2. Click the **Run** (Green Play) button in the Android Studio toolbar.
3. **Grant Permissions:** Upon launch, the app will ask for:
   - `RECEIVE_SMS` & `READ_SMS`: To scan incoming messages.
   - `POST_NOTIFICATIONS`: To show alerts (Android 13+).
4. You are now protected! You can test the app using the "Load Scam Sample" button or by sending an SMS to the device.

---

## 🧪 Testing Examples (Bangla)

Try these samples using the **"Load Scam Sample"** button to see the detection in action:

| Sample Message | Expected Type | Risk Level |
| :--- | :--- | :--- |
| আপনার অ্যাকাউন্ট সাময়িকভাবে বন্ধ করা হয়েছে। চালু রাখতে OTP কোডটি রিপ্লাই করুন। | **OTP/PIN Scam** | **High** |
| অভিনন্দন! আপনি ৫০,০০০ টাকা জিতেছেন। রেজিস্ট্রেশন করুন: fake-link | **Prize Scam** | **High** |
| ঘরে বসে দৈনিক ২০০০ টাকা আয় করুন। Join করতে ফি মাত্র ৫০০ টাকা। | **Job Scam** | **Medium/High** |
| আপনার পার্সেল আটকে আছে। ডেলিভারি চার্জ ৭০ টাকা এখনই পাঠান। | **Courier Scam** | **High** |

---

## 📋 BuildFest Demo Flow

For a smooth demonstration, follow these steps:
1. **Initial State:** Show the main screen with "Local Scanner Ready".
2. **Manual Check:** Click **"Load Scam Sample"**, then click **"Check Locally"**. Highlight the Red/Orange risk card and the "Safe Reply".
3. **Gemini Demo:** (If API Key is added) Click **"Check Gemini"** to show how AI provides deeper reasoning for the scam.
4. **Background Test:** Send an SMS from another phone (or emulator console) with the text: *"bKash: Your account is blocked. Send PIN to unlock."*
5. **Notification:** Show the instant high-risk alert popping up in the Android notification tray.
6. **Privacy:** Point out the **"Enable Gemini cloud scan"** checkbox and explain that user data only leaves the device with explicit consent.

---

## 🔐 Permissions & Privacy

- **SMS Permissions:** `RECEIVE_SMS` and `READ_SMS` are required for the core protection feature. The app **does not** store your messages or upload them to any server unless you explicitly use the Gemini AI feature.
- **Privacy Note:** When Gemini AI is enabled, only the text of the specific message being analyzed is sent to Google's Gemini API. No personal identifiers (like phone numbers) are transmitted.
- **Local First:** All incoming messages are first checked locally. If Gemini auto-scan is OFF, **no data leaves your device.**

---

## 👥 Team: Quantum Builders
*Dedicated to building a safer digital Bangladesh.*
