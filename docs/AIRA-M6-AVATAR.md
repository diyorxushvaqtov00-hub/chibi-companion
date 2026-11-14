# AIRA M6 — Companion Expression States

## Implemented
- Floating avatar has IDLE, LISTENING, THINKING, and SPEAKING expression states.
- Listening changes the aura pulse and blink rhythm.
- Thinking adds a subtle animated visual cue.
- Speaking changes the mouth expression.
- Chat state updates are shared with the floating service through app-private preferences.
- The floating service polls the state while it is running.

## Scope
This milestone improves the existing 2D Canvas avatar. It is not a rigged 3D model and does not include lip-sync to audio phonemes.

## Test
- Open chat while overlay is active.
- Start microphone recognition and confirm listening expression.
- Send a prompt and confirm thinking expression.
- Receive a reply with voice output enabled and confirm speaking expression.
- Leave chat and confirm return to idle.
