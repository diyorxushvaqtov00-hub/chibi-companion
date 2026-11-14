# AIRA — Virtual AI Companion

AIRA is an Android-first virtual companion built in Kotlin for the HONOR X9b target device. The project prioritizes local processing, Uzbek interaction, user-controlled memory, and a movable on-screen avatar.

## Current features
- Local AI: LiteRT-LM runtime with a separately downloaded Qwen3-0.6B model. The model is not bundled in the APK.
- Chat: Uzbek-first companion persona and locally saved chat history.
- Memory: user-entered facts stored in app-private SQLite, with review and delete controls.
- Voice input: Android SpeechRecognizer requested only after the microphone button is tapped; Uzbek language preference is requested.
- Voice output: optional Android TextToSpeech, if an Uzbek voice is installed.
- Vision OCR: user-selected images are processed on-device with ML Kit Latin text recognition. This extracts text; it is not general scene understanding.
- Floating avatar: draggable silver-haired, violet-eyed 2D Canvas companion with idle, listening, thinking and speaking states.
- Overlay controls: start, hide, restore and stop the floating companion.
- Privacy controls: review, share or clear chat history; manage memories separately.

## Automatic app updates

AIRA checks the GitHub latest release on launch (at most once every 24 hours). A user can also check manually from Settings. If a newer release exists, AIRA downloads the APK and opens Android's package installer; Android still requires the user to confirm installation.

To publish an update:
1. Increase `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Push a matching tag such as `v1.8` (the tag version must match `versionName`).
3. The signed-release workflow builds and publishes `aira-release.apk` to GitHub Releases.

One-time GitHub Actions secrets required by the release workflow:
- `ANDROID_KEYSTORE_BASE64`: base64-encoded persistent JKS keystore.
- `ANDROID_KEY_ALIAS`: alias inside that keystore.
- `ANDROID_KEY_PASSWORD`: key password.
- `ANDROID_STORE_PASSWORD`: keystore password.

**Signing migration warning:** existing debug APKs from GitHub Actions are not signed with a persistent key. The first signed release cannot update those installations in place. Install the first signed release once (this may require uninstalling the debug build); later releases signed with the same key can update normally. Export important chat data before this one-time migration.

## Build an APK
1. Open GitHub Actions: https://github.com/diyorxushvaqtov00-hub/chibi-companion/actions
2. Select the latest successful Build AIRA Android APK run on the aira-m8-settings branch.
3. Download the aira-m8-debug-apk artifact.
4. Extract app-debug.apk from the ZIP and install it on Android.
5. Grant overlay permission only if you want the floating companion.

## First run
1. Open AIRA and choose the AI model download screen.
2. Download the approximately 614 MB model over Wi-Fi.
3. Open chat and test a short Uzbek prompt.
4. Try the microphone button and optional voice output.
5. Open the vision screen and select an image containing Latin-script text.
6. Manage saved memories and chat history from the settings screens.

## Known limitations
- Qwen3-0.6B is small and may produce weaker answers than larger models; performance must be measured on the target phone.
- The real model download, checksum verification and inference have not yet been device-validated on HONOR X9b.
- Uzbek speech recognition and Uzbek TTS depend on Android speech services and installed language packs.
- Vision currently means Latin-script OCR only. It does not identify objects, describe scenes, or understand image pixels with the Qwen text model.
- The avatar is a 2D Canvas illustration, not the final rigged 3D character.
- The debug APK is not a signed production release.

## Privacy
- Chat and memory data are stored in app-private local storage.
- The user explicitly chooses which facts to save as memories.
- Images selected for OCR are processed locally by ML Kit and are not uploaded by this feature.
- Speech recognition may use the Android device's configured recognition service; do not assume speech processing is offline.
- Sharing chat history is an explicit user action.

## Development
- Android Gradle Plugin 8.5.2; Kotlin 2.3.20; JDK 17; Gradle 8.7.
- minSdk 26; targetSdk 34.
- See the docs folder for milestone notes and validation checklists.