# AIRA M3 — User-Controlled Memory

## Scope
- Store user-entered facts locally in the app-private SQLite database.
- Require an explicit Save action for every memory.
- Show all saved facts in a dedicated memory screen.
- Allow deleting one fact or clearing all facts.
- Include a limited number of saved facts in the local model's system instruction.

## Privacy
- No contacts, SMS, location, microphone, or background device data are collected by this feature.
- Nothing is silently saved from chat.
- Memories remain on the device in app-private storage.
- The user can review and delete memories at any time.
- This initial SQLite store is not separately encrypted.

## Validation
- Add a memory, close and reopen the app, confirm it remains.
- Delete one memory and confirm it disappears from the prompt context.
- Clear all memories and confirm the list is empty.
- Confirm chat works with an empty memory list.
