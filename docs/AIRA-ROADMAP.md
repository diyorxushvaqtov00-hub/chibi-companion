# AIRA — Virtual AI Companion
## Product and technical roadmap (v1.0)

AIRA is an Android-first virtual companion designed to run on a phone, support Uzbek conversations, remember user-approved facts locally, speak and listen, and optionally appear as a movable floating character.

## Product principles
- Zero-cost development and free/open-source tools only.
- Android phone-first workflow; no desktop computer required.
- Local-first storage; user data stays on device by default.
- AI provider is replaceable. Never hard-code API keys or promise unlimited free cloud inference.
- Battery, memory, accessibility, and Android background-execution limits are first-class constraints.
- Ask permission before storing personal facts or using sensitive device capabilities.

## Target device and build environment
- Initial target: HONOR X9b, Android 15, 12 GB RAM.
- Development: Termux + GitHub.
- Build: GitHub Actions with JDK 17 and Gradle; APK artifact for installation.
- Existing base: Chibi Companion Android project, application ID `uz.diyor.companion`.

## Architecture
- **app/ui** — Jetpack Compose screens: onboarding, companion home, chat, settings, memory manager.
- **app/companion** — character state, mood, interaction events, animation controller.
- **app/overlay** — WindowManager floating companion and foreground service, enabled only after explicit user permission.
- **app/ai** — provider-neutral interface, prompt/persona assembly, streaming response state, safe fallback.
- **app/memory** — Room database, explicit save/forget controls, conversation summaries and user-approved facts.
- **app/voice** — Android SpeechRecognizer and TextToSpeech adapters, with clear microphone state.
- **app/data** — settings, permissions, repositories and migrations.

Keep platform APIs behind interfaces so features can be tested independently.

## Delivery milestones

### M0 — Foundation
- Establish a reproducible Android build.
- Add product identity, app theme, architecture notes and CI workflow.
- Confirm APK installs and launches on the target phone.

### M1 — Companion shell
- Compose home screen and chat screen.
- Character placeholder with idle/talking/listening states.
- Local settings and first-run permission explanation.

### M2 — Conversation
- Provider-neutral AI interface.
- Working chat transport selected from a genuinely free local or free-tier option.
- Offline fallback that clearly identifies itself as a fallback, not an AI answer.

### M3 — Memory
- Room persistence for chat history and user-approved memories.
- Memory review, edit and delete controls.
- No silent collection of contacts, messages, location, or other sensitive data.

### M4 — Voice
- Speech-to-text and text-to-speech.
- Visible microphone/listening indicator and manual stop control.
- Uzbek language support where available on the device.

### M5 — Floating companion
- Explicit overlay permission flow.
- Draggable compact character, tap-to-open chat, hide/close control.
- Foreground service only while the user has enabled the companion.
- Test touch boundaries so the character does not block unrelated app controls.

### M6 — Character polish
- Replace placeholder with optimized 2D animated character first.
- Add expressions and states; evaluate 3D only after performance and battery tests.
- Keep assets licensed for redistribution or created for this project.

## Definition of done for v1.0
- Debug APK builds in GitHub Actions and installs on HONOR X9b.
- App opens without crashing; chat history persists after restart.
- User can inspect and delete stored memories.
- Voice controls clearly show when listening.
- Floating companion can be moved, hidden, and disabled.
- No paid API or subscription is required for the baseline build.
- Known limitations and any free-tier limits are documented.

## Engineering rules
- Make small commits with one purpose each.
- Do not replace existing files until their current contents are inspected.
- Run the Android build after meaningful code changes.
- Do not commit secrets, tokens, private keys, or personal data.
