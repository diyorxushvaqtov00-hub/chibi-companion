# AIRA M4 — Voice Interaction

## Implemented
- Microphone permission is requested only after the user taps the microphone button.
- Android SpeechRecognizer is configured for Uzbek (uz-UZ).
- Recognized speech is placed into the chat input; the user decides when to send it.
- Optional spoken replies use Android TextToSpeech with Uzbek locale.
- Voice output is off by default and can be toggled by the user.
- Recognition is cancelled when the chat screen leaves the foreground.
- Microphone and TTS resources are released when the chat closes.

## Device-dependent limitations
- Uzbek speech recognition depends on the Android recognition service and installed language support.
- Uzbek text-to-speech depends on an installed Uzbek voice. The app reports when it is unavailable.
- Speech recognition may use the device's configured recognition service; this feature is not guaranteed to be fully offline.

## Test on HONOR X9b
1. Open AIRA chat and tap the microphone.
2. Grant microphone permission.
3. Say a short Uzbek sentence.
4. Confirm the recognized text appears in the input field without being sent automatically.
5. Tap Send.
6. Turn on voice output if an Uzbek TTS voice is installed.
7. Leave the chat and confirm listening stops.
