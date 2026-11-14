# AIRA M7 — Floating Companion Controls

## Implemented
- Hide the floating avatar without stopping the foreground companion service.
- Restore the floating avatar while overlay permission remains granted.
- Stop the companion service from the existing home screen control.
- Hiding removes the overlay view and stops its polling callback; it does not keep animating invisibly.

## Test
1. Start AIRA overlay.
2. Hide the avatar from the home screen.
3. Confirm the overlay disappears.
4. Restore the avatar.
5. Confirm it appears again and can be dragged.
6. Stop AIRA and confirm its foreground notification is removed.
