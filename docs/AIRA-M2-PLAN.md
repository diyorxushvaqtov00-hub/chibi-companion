# AIRA M2 — AI Core Foundation

## Goal
Replace direct keyword logic in the chat screen with a provider-neutral AI boundary. This milestone prepares the app for a real on-device language model without falsely presenting demo replies as model-generated.

## Added architecture
- `AiModels.kt`: roles, request/reply data and typed failures.
- `AiProvider.kt`: provider interface and cancellation contract.
- `AiraPersona.kt`: the canonical Uzbek-first persona instruction.
- `OfflineDemoProvider.kt`: temporary deterministic fallback, explicitly marked with `isFallback = true`.

## Current limitation
M2 foundation does not yet contain a real LLM. The app remains in offline demo mode until a compatible model runtime and model file are integrated and tested on the target HONOR X9b.

## Local model direction
Evaluate Google's LiteRT-LM Kotlin API and a small CPU/GPU-compatible model such as Qwen3-0.6B in the LiteRT Community format. Model files must be downloaded separately into app storage; do not bundle multi-hundred-megabyte model weights inside the APK.

Official references:
- https://developers.google.com/edge/litert-lm/android
- https://huggingface.co/litert-community

## Privacy and safety requirements
- Never upload chat history without clear user choice.
- Keep local inference available without an account or paid API.
- Make cloud providers optional and disclose when they are used.
- Provide cancel controls for generation.
- Do not claim model-generated output when using the offline demo provider.

## Next implementation steps
1. Route ChatActivity through AiProvider.
2. Add a model download manager with size, checksum and progress validation.
3. Add LiteRT-LM behind a LocalLiteRtProvider.
4. Initialize and own the engine off the main thread; close it exactly once.
5. Run a real-device test for load time, memory use, battery use and Uzbek response quality.
