# AIRA M5 — Vision OCR

## Implemented
- User explicitly chooses an image using Android's document picker.
- ML Kit bundled Latin text recognition extracts text on-device.
- Selected image is not uploaded by this feature.
- Extracted text is editable before sending to AIRA chat.
- No broad storage permission is requested.

## Scope boundary
This milestone provides OCR (reading visible text), not general image understanding, object recognition, face recognition, or scene description. It does not claim that the Qwen text-only model can accept image pixels.

## Supported text
The bundled Latin script recognizer is suitable for many Latin-script texts, including Uzbek Latin, but OCR quality depends on image focus, contrast, resolution, and layout. Cyrillic and other scripts are not guaranteed by this Latin model.

## Test
1. Open "Rasmni AIRA bilan o‘qish".
2. Select a clear image containing Uzbek Latin text.
3. Review and edit the extracted text.
4. Send it to chat and ask AIRA to summarize or explain it.
5. Test an image with no text and verify a clear empty-result message.

## Source
Google ML Kit Text Recognition v2 Android documentation:
https://developers.google.com/ml-kit/vision/text-recognition/v2/android
