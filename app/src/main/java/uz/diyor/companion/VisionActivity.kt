package uz.diyor.companion

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * User-selected image OCR. Image processing is on-device; no image upload.
 * This milestone extracts text, it does not claim general image understanding.
 */
class VisionActivity : Activity() {
    companion object {
        private const val REQUEST_PICK_IMAGE = 510
        const val EXTRA_OCR_TEXT = "aira_ocr_text"
    }

    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private lateinit var result: EditText
    private lateinit var askButton: Button
    private var selectedImage: Uri? = null
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  AIRA VISION"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(205, 185, 255))
            setPadding(0, 0, 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "Rasm tanla — AIRA undagi matnni telefonda ajratib oladi. Rasm internetga yuborilmaydi."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(12))
        })
        root.addView(Button(this).apply {
            text = "Galereyadan rasm tanlash"
            isAllCaps = false
            setOnClickListener { chooseImage() }
        }, LinearLayout.LayoutParams(-1, -2))
        preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = rounded(Color.rgb(28, 24, 40), dp(14).toFloat())
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, dp(210)).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })
        status = TextView(this).apply {
            text = "Hozircha rasm tanlanmagan."
            textSize = 13f
            setTextColor(Color.rgb(190, 170, 230))
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(8))
        }
        root.addView(status)
        root.addView(TextView(this).apply {
            text = "Ajratilgan matn (xohlasang tahrirla):"
            textSize = 13f
            setTextColor(Color.WHITE)
        })
        result = EditText(this).apply {
            hint = "Rasmdagi matn shu yerda ko‘rinadi..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            gravity = Gravity.TOP
            minLines = 5
            maxLines = 12
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(35, 30, 49), dp(12).toFloat())
        }
        root.addView(result, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(6)
        })
        root.addView(Button(this).apply {
            text = "Matnni AIRA bilan muhokama qilish"
            isAllCaps = false
            setOnClickListener { sendToChat() }
        }, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(10)
        })
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivityForResult(Intent.createChooser(intent, "AIRA uchun rasm tanla"), REQUEST_PICK_IMAGE)
        } catch (_: Exception) {
            Toast.makeText(this, "Rasm tanlash ilovasi topilmadi.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Legacy result API retained to avoid an additional Activity dependency")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_IMAGE || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        selectedImage = uri
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {
            // Temporary URI permission is sufficient for this active screen.
        }
        preview.setImageURI(uri)
        result.setText("")
        status.text = "Rasm o‘qilmoqda..."
        runOcr(uri)
    }

    private fun runOcr(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extracted = visionText.text.trim()
                    result.setText(extracted)
                    status.text = if (extracted.isBlank()) {
                        "Matn topilmadi. Rasm tiniqroq bo‘lishini tekshir."
                    } else {
                        "Matn ajratildi. Natijani tekshirib, kerak bo‘lsa tahrirla."
                    }
                }
                .addOnFailureListener { error ->
                    status.text = "Rasmni o‘qishda xato: ${error.localizedMessage ?: "Noma’lum xato"}"
                }
        } catch (error: Exception) {
            status.text = "Rasm ochilmadi: ${error.localizedMessage ?: "Noma’lum xato"}"
        }
    }

    private fun sendToChat() {
        val text = result.text.toString().trim()
        if (text.isBlank()) {
            Toast.makeText(this, "Avval rasm tanlab matn ajrat.", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, ChatActivity::class.java).putExtra(EXTRA_OCR_TEXT, text))
        finish()
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    override fun onDestroy() {
        recognizer.close()
        super.onDestroy()
    }
}
