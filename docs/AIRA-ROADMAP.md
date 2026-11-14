# AIRA — Product Roadmap

## Vision
An Android virtual companion with a calm, intelligent personality; Uzbek-first conversation; local AI and user-controlled memory; voice interaction; image assistance; and a movable animated avatar.

## Product constraints
- Target device: HONOR X9b, Android 15, 12 GB RAM.
- Phone-first development through GitHub and Termux.
- No paid API is required for the baseline local features.
- No silent collection of contacts, messages, location, or microphone data.
- The user controls saved memories, permissions, image selection, and sharing.
- Be explicit when a feature depends on Google Play Services, network, or device language packs.

## Implemented milestones

### M1 — Android companion shell
- AIRA identity and dark violet UI.
- Floating draggable 2D Canvas avatar.
- Foreground service and overlay permission flow.
- Tap avatar to open chat.

### M2 — Local AI foundation
- Provider-neutral AI interface and LiteRT-LM Android runtime.
- Separate model downloader with size and SHA-256 validation.
- Qwen3-0.6B model file stored outside the APK.
- Offline demo fallback clearly labelled.

### M3 — User-controlled memory
- App-private SQLite memory store.
- Explicit add, list, delete-one and clear-all controls.
- User-approved memories included in local AI prompt context.

### M4 — Voice interaction
- Microphone permission requested on demand.
- Uzbek SpeechRecognizer language preference.
- Optional Android TextToSpeech output.
- Recognition stopped when chat leaves the foreground.

### M5 — Vision OCR
- User-selected image input.
- On-device ML Kit Latin text recognition.
- Editable OCR output can be sent into AIRA chat.
- This is OCR, not general visual understanding.

### M6 — Avatar expression
- Idle, listening, thinking and speaking states.
- State-specific aura, blink and mouth expressions.
- Chat and voice actions update the floating avatar state.

### M7 — Overlay controls
- Hide avatar without stopping service.
- Restore avatar while overlay permission is available.
- Stop companion service and remove foreground notification.

### M8 — Settings and privacy
- Persist voice-output preference.
- Share or clear chat history.
- Separate chat-history and memory management.
- Privacy information and direct links to model/memory screens.

## Remaining milestones

### M9 — Real device validation
- Install latest APK on HONOR X9b.
- Download and verify Qwen3-0.6B.
- Measure model load time, response latency, RAM, temperature and battery.
- Test Uzbek speech input/output and OCR with real samples.
- Fix crashes and device-specific issues before production claims.

### M10 — 3D character
- Import a redistributable GLB/GLTF model with correct rig and animations.
- Evaluate Filament/SceneView or another maintained Android renderer.
- Add idle, blink, talk, listen, happy, sad and thinking animations.
- Keep a low-power 2D fallback for battery and compatibility.

### M11 — Genuine multimodal vision
- Evaluate an on-device vision-language model compatible with the phone's RAM.
- Keep the existing OCR route as a lightweight fallback.
- Do not send user images to a server without explicit opt-in.

### M12 — Character behavior and quality
- User-configurable routines and reminders.
- Safe, permission-gated actions only.
- Automated tests for memory, settings, model lifecycle and overlay controls.
- Accessibility, localization, crash and battery testing.

## Release-ready definition
- Clean GitHub Actions build.
- Installs and runs on HONOR X9b.
- Local model downloaded, hash-verified and successfully answers prompts.
- Chat history and memories can be inspected and deleted.
- Voice controls clearly indicate microphone state.
- OCR works on representative Uzbek Latin images.
- Overlay can be moved, hidden, restored and stopped.
- 3D and vision capabilities are only advertised after device testing.
- Release APK is properly signed and documented.