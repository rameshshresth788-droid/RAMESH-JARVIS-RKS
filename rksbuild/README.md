# RAMESH JARVIS / RKS

Android 35, Kotlin + Jetpack Compose, GitHub Actions compatible.

## Features
- Gemini, OpenAI/ChatGPT and OpenRouter provider selector.
- API key stored using Android Keystore-backed encrypted preferences.
- Foreground microphone voice service with RKS wake-word detection.
- Hindi/English/Hinglish speech recognition and Text-to-Speech.
- Basic voice app launching: "RKS open YouTube", etc.
- Floating RKS overlay when overlay permission is granted.
- Optional Accessibility Service entry for future device/UI automation; Android requires the user to enable it manually.
- Boot receiver attempts to keep the configured service ready, subject to Android background/foreground-service restrictions.
- No API keys are embedded in source code.

## Setup
1. Install APK.
2. Grant microphone permission.
3. Open Settings and select an AI provider.
4. Enter only that provider's API key; endpoint/model are automatic.
5. Set wake word (default `RKS`).
6. Grant floating-window permission if you want the small always-visible logo.
7. Enable RKS Accessibility Service only if you want supported UI automation features.
8. Tap the core once to start the voice foreground service.

## Android limitation
A third-party Android app cannot receive unrestricted control of the entire device. Protected settings, secure screens, permissions, banking apps and other restricted actions remain controlled by Android and the user. The app never attempts to bypass those protections.
