# AIRA M9 — HONOR X9b Device Validation

This checklist must be completed on the real target phone. A successful GitHub Actions build does not prove the app works on-device.

## Installation
- [ ] Download the newest successful APK artifact.
- [ ] Extract app-debug.apk from the artifact ZIP.
- [ ] Install and launch AIRA.
- [ ] Confirm the home screen opens without crashing.

## Local model
- [ ] Open the model manager.
- [ ] Download the model over Wi-Fi.
- [ ] Confirm SHA-256 validation passes.
- [ ] Open chat and ask a short Uzbek question.
- [ ] Confirm the answer is readable and not a Java/Kotlin object string.
- [ ] Record first-load time, response time, approximate RAM use and phone temperature.
- [ ] Repeat with airplane mode enabled to verify inference is local after download.

## Memory and chat
- [ ] Save one test memory.
- [ ] Close and reopen AIRA; confirm it remains.
- [ ] Confirm a relevant question uses that memory.
- [ ] Delete the test memory.
- [ ] Clear chat history from Settings and confirm the chat is empty.

## Voice
- [ ] Tap microphone and grant permission.
- [ ] Say a short Uzbek Latin sentence.
- [ ] Confirm recognized text appears in the composer and is not sent automatically.
- [ ] Try optional TTS output; record whether Uzbek voice is installed.
- [ ] Leave chat while listening and confirm the microphone stops.

## Vision OCR
- [ ] Select a clear Uzbek Latin image.
- [ ] Review OCR output and compare it to the source.
- [ ] Send the extracted text to chat.
- [ ] Try a Cyrillic image and note that Latin-only OCR is not guaranteed to read it.

## Floating avatar
- [ ] Grant overlay permission.
- [ ] Start the companion.
- [ ] Drag it to several screen positions.
- [ ] Tap to open chat.
- [ ] Verify listening/thinking/speaking states.
- [ ] Hide and restore the avatar.
- [ ] Stop the service and confirm the notification disappears.

## Battery and reliability
- [ ] Leave the overlay running for 15 minutes.
- [ ] Note battery drain and heat.
- [ ] Test app rotation/relaunch and phone screen lock/unlock.
- [ ] Record crashes or unexpected behavior.

## Result
- Device: HONOR X9b
- Android version:
- App version:
- APK build/run:
- Model checksum result:
- Model first-load time:
- Typical response time:
- Peak heat / battery observations:
- Issues found:
