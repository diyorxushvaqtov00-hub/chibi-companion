# AIRA M1 — Character and Prototype

## Locked character direction
- Name: AIRA
- Personality: calm, cool-headed, intelligent; affectionate and attentive with trusted people.
- Visual direction: realistic anime-inspired 3D target.
- Hair: long silver/white hair.
- Eyes: violet.
- Outfit: black-and-white futuristic outfit with restrained violet light accents.
- Expression: composed, observant, confident; warmth appears subtly around trusted people.

## M1 scope
M1 is the first installable Android prototype, not the finished companion.
1. Rebrand launcher and chat as AIRA.
2. Draw a lightweight silver-haired / violet-eyed avatar using Android Canvas.
3. Keep the floating overlay draggable and tappable.
4. Show a smaller avatar when the main app is not open.
5. Add a small offline persona demo in Uzbek.
6. Build a debug APK through GitHub Actions.

## Explicit limitations
- The Canvas avatar is a 2D placeholder, not a true 3D model.
- Local chat history is stored on-device in app-private preferences; it is not encrypted separately and is not synced.
- M1 chat uses simple keyword-based demo responses; it is not an LLM and does not have persistent conversational memory.
- Voice, real AI inference, 3D rigging/animation, settings, and long-term memory are later milestones.
- No paid API or cloud service is required for this M1 prototype.

## M1 acceptance checklist
- [ ] GitHub Actions debug APK build succeeds.
- [ ] APK installs on HONOR X9b / Android 15.
- [ ] Main screen opens and displays AIRA branding.
- [ ] Overlay permission flow works.
- [ ] AIRA overlay appears, can be dragged, and opens chat on tap.
- [ ] Overlay compacts outside the app and expands inside it.
- [ ] Chat accepts Uzbek text and shows AIRA demo responses.
- [ ] Chat history remains after closing and reopening the app.
- [ ] Stop button removes the overlay and foreground notification.

## Current implementation note
The initial code update has been committed to branch `aira-v1-foundation`. Device testing is still required before calling M1 complete.
