package uz.diyor.companion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import uz.diyor.companion.ai.AiCallback
import uz.diyor.companion.ai.AiFailure
import uz.diyor.companion.ai.AiMessage
import uz.diyor.companion.ai.AiProvider
import uz.diyor.companion.ai.AiRequest
import uz.diyor.companion.ai.AiRole
import uz.diyor.companion.ai.AiraPersona
import uz.diyor.companion.ai.OfflineDemoProvider
import uz.diyor.companion.memory.MemoryStore

class ChatActivity : Activity() {
    private lateinit var messages: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private lateinit var micButton: Button
    private lateinit var voiceButton: Button
    private lateinit var sendButton: Button
    private lateinit var aiStatus: TextView
    private val mainHandler = Handler(Looper.getMainLooper())
    private var requestInProgress = false
    private var requestId = 0
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var speakReplies = false
    private var listening = false
    private val requestAudioPermission = 420
    private val history = mutableListOf<Pair<String, String>>()
    private lateinit var aiProvider: AiProvider
    private val prefs by lazy { getSharedPreferences("aira_chat", MODE_PRIVATE) }
    private val companionPrefs by lazy { getSharedPreferences("companion_state", MODE_PRIVATE) }

    private fun setAvatarState(state: ChibiView.AvatarState) {
        companionPrefs.edit().putString("avatar_state", state.name).apply()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", true).apply()
    }

    override fun onPause() {
        speechRecognizer?.cancel()
        listening = false
        setAvatarState(ChibiView.AvatarState.IDLE)
        if (::micButton.isInitialized) micButton.text = "🎙"
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", false).apply()
        super.onPause()
    }

    override fun onDestroy() {
        if (aiProvider is AutoCloseable) {
            try { (aiProvider as AutoCloseable).close() } catch (_: Exception) { }
        }
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloader = uz.diyor.companion.ai.AiraModelDownloader(this)
        val modelFile = downloader.modelFileIfPresent()
        aiProvider = if (modelFile != null) {
            uz.diyor.companion.ai.LocalLiteRtProvider(this, modelFile)
        } else {
            OfflineDemoProvider()
        }
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  A I R A"
            textSize = 22f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(205, 185, 255))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "Sovuqqon aql • Yaqinlarga mehr"
            textSize = 11f
            letterSpacing = 0.08f
            setTextColor(Color.rgb(150, 140, 175))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        })

        aiStatus = TextView(this).apply {
            text = modelFile?.let { "Model aniqlanmoqda: ${downloader.profileFor(it)?.displayName ?: it.name}" } ?: "Demo rejimi — model topilmadi."
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(175, 155, 215))
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(aiStatus)
        scroll = ScrollView(this).apply { isFillViewport = true }
        messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(8))
        }
        scroll.addView(messages)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        input = EditText(this).apply {
            hint = "AIRA ga yoz..."
            setHintTextColor(Color.rgb(130, 120, 150))
            setTextColor(Color.WHITE)
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEND
            setPadding(dp(12), 0, dp(12), 0)
            background = rounded(Color.rgb(35, 30, 49), dp(14).toFloat())
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(50), 1f))
        micButton = Button(this).apply {
            text = "🎙"
            isAllCaps = false
            setOnClickListener { requestAudioAndListen() }
        }
        row.addView(micButton, LinearLayout.LayoutParams(dp(54), dp(50)))
        voiceButton = Button(this).apply {
            text = "Ovoz: OFF"
            isAllCaps = false
            setOnClickListener { toggleVoiceOutput() }
        }
        row.addView(voiceButton, LinearLayout.LayoutParams(dp(92), dp(50)))
        sendButton = Button(this).apply {
            text = "Yuborish"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(111, 75, 190))
            setOnClickListener { sendMessage() }
        }
        row.addView(sendButton)
        root.addView(row)
        setContentView(root)
        speakReplies = getSharedPreferences("aira_voice", MODE_PRIVATE).getBoolean("speak_replies", false)
        initializeTextToSpeech()

        restoreHistory()
        intent.getStringExtra(VisionActivity.EXTRA_OCR_TEXT)?.takeIf { it.isNotBlank() }?.let { extracted ->
            input.setText("Rasmdan ajratilgan matn:\n$extracted\n\nShuni tahlil qilib ber.")
            input.setSelection(input.text.length)
        }
        if (history.isEmpty()) {
            appendMessage("AIRA", "Salom. Men AIRA. Odatda ortiqcha gapirmayman. Lekin seni tinglashga vaqtim bor.")
        }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        if (requestInProgress) return
        requestInProgress = true
        val activeRequestId = ++requestId
        sendButton.isEnabled = false
        sendButton.text = "Kut..." 
        aiStatus.text = "AIRA javob tayyorlamoqda..."
        setAvatarState(ChibiView.AvatarState.THINKING)
        appendMessage("Sen", text)
        input.setText("")

        val contextMessages = history.dropLast(1).takeLast(24).map { (who, content) ->
            AiMessage(
                role = if (who == "Sen") AiRole.USER else AiRole.ASSISTANT,
                content = content
            )
        }
        val savedMemories = MemoryStore(applicationContext).use { it.promptContext() }
        val memoryInstruction = if (savedMemories.isBlank()) "" else "\n\nFoydalanuvchi tomonidan ochiq saqlangan xotiralar:\n$savedMemories\nUlarni faqat tegishli bo‘lsa ishlat; ularga yangi fakt qo‘shib to‘qima."
        val request = AiRequest(
            messages = contextMessages,
            userMessage = text,
            systemInstruction = AiraPersona.systemInstruction + memoryInstruction
        )

        val timeout = Runnable {
            if (requestInProgress && requestId == activeRequestId) {
                requestInProgress = false
                sendButton.isEnabled = true
                sendButton.text = "Yuborish"
                aiStatus.text = "Javob vaqti tugadi. Model yuklanmay qolgan yoki juda sekin ishlayotgan bo‘lishi mumkin."
                appendMessage("AIRA · diagnostika", "90 soniya ichida javob kelmadi. Modelni qayta yuklashdan oldin xatolikni aniqlash kerak.")
                setAvatarState(ChibiView.AvatarState.IDLE)
            }
        }
        mainHandler.postDelayed(timeout, 90000L)
        aiProvider.generate(request, object : AiCallback {
            override fun onSuccess(reply: uz.diyor.companion.ai.AiReply) {
                runOnUiThread {
                    if (requestId != activeRequestId || !requestInProgress) return@runOnUiThread
                    mainHandler.removeCallbacks(timeout)
                    requestInProgress = false
                    sendButton.isEnabled = true
                    sendButton.text = "Yuborish"
                    aiStatus.text = if (reply.isFallback) "Demo rejimi — haqiqiy model ishlamayapti." else "Mahalliy AI faol."
                    val label = if (reply.isFallback) "AIRA · demo" else "AIRA"
                    appendMessage(label, reply.text)
                    if (speakReplies) {
                        setAvatarState(ChibiView.AvatarState.SPEAKING)
                        speak(reply.text)
                    } else {
                        setAvatarState(ChibiView.AvatarState.IDLE)
                    }
                }
            }

            override fun onFailure(failure: AiFailure) {
                runOnUiThread {
                    if (requestId != activeRequestId || !requestInProgress) return@runOnUiThread
                    mainHandler.removeCallbacks(timeout)
                    requestInProgress = false
                    sendButton.isEnabled = true
                    sendButton.text = "Yuborish"
                    aiStatus.text = "AI xatolik bilan to‘xtadi."
                    val reason = when (failure) {
                        is AiFailure.Unavailable -> failure.reason
                        is AiFailure.InvalidRequest -> failure.reason
                        is AiFailure.Internal -> failure.reason
                    }
                    appendMessage("AIRA", "Hozir javob bera olmadim: $reason")
                    setAvatarState(ChibiView.AvatarState.IDLE)
                }
            }
        })
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            runOnUiThread {
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale("uz", "UZ"))
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit
                        override fun onDone(utteranceId: String?) {
                            runOnUiThread { setAvatarState(ChibiView.AvatarState.IDLE) }
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            runOnUiThread { setAvatarState(ChibiView.AvatarState.IDLE) }
                        }
                    })
                    ttsReady = result != null &&
                        result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    voiceButton.isEnabled = ttsReady
                    if (ttsReady) voiceButton.text = if (speakReplies) "Ovoz: ON" else "Ovoz: OFF"
                    if (!ttsReady) {
                        voiceButton.text = "Ovoz yo‘q"
                    }
                } else {
                    ttsReady = false
                    voiceButton.isEnabled = false
                    voiceButton.text = "Ovoz yo‘q"
                }
            }
        }
    }

    private fun toggleVoiceOutput() {
        if (!ttsReady) {
            Toast.makeText(this, "Telefonda o‘zbekcha TTS ovozi mavjud emas.", Toast.LENGTH_LONG).show()
            return
        }
        speakReplies = !speakReplies
        getSharedPreferences("aira_voice", MODE_PRIVATE).edit().putBoolean("speak_replies", speakReplies).apply()
        voiceButton.text = if (speakReplies) "Ovoz: ON" else "Ovoz: OFF"
        if (!speakReplies) {
            textToSpeech?.stop()
            setAvatarState(ChibiView.AvatarState.IDLE)
        }
    }

    private fun speak(text: String) {
        if (ttsReady && speakReplies) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aira-reply")
        }
    }

    private fun requestAudioAndListen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Bu telefonda ovoz tanish xizmati mavjud emas.", Toast.LENGTH_LONG).show()
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudioPermission)
            return
        }
        startListening()
    }

    private fun startListening() {
        if (listening) {
            speechRecognizer?.stopListening()
            return
        }
        val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(this).also { created ->
            created.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    listening = true
                    setAvatarState(ChibiView.AvatarState.LISTENING)
                    micButton.text = "Tingla..."
                }

                override fun onBeginningOfSpeech() {
                    listening = true
                }

                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    listening = false
                    setAvatarState(ChibiView.AvatarState.IDLE)
                    micButton.text = "🎙"
                }

                override fun onError(error: Int) {
                    listening = false
                    setAvatarState(ChibiView.AvatarState.IDLE)
                    micButton.text = "🎙"
                    if (error != SpeechRecognizer.ERROR_CLIENT) {
                        Toast.makeText(this@ChatActivity, "Ovoz aniqlanmadi. Yana urinib ko‘r.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(results: Bundle?) {
                    listening = false
                    setAvatarState(ChibiView.AvatarState.IDLE)
                    micButton.text = "🎙"
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!spoken.isNullOrBlank()) {
                        input.setText(spoken)
                        input.setSelection(input.text.length)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
        speechRecognizer = recognizer
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "uz-UZ")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "uz-UZ")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            recognizer.startListening(intent)
            listening = true
            setAvatarState(ChibiView.AvatarState.LISTENING)
            micButton.text = "Tingla..."
        } catch (error: Exception) {
            listening = false
            setAvatarState(ChibiView.AvatarState.IDLE)
            micButton.text = "🎙"
            Toast.makeText(this, error.message ?: "Mikrofonni ishga tushirib bo‘lmadi.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudioPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(this, "Ovozli yozish uchun mikrofon ruxsati kerak.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun appendMessage(who: String, text: String) {
        history.add(who to text)
        if (history.size > 80) history.removeAt(0)
        renderMessage(who, text)
        saveHistory()
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun renderMessage(who: String, text: String) {
        val bubble = TextView(this).apply {
            this.text = "$who\n$text"
            textSize = 15f
            setTextColor(if (who.startsWith("AIRA")) Color.rgb(225, 213, 255) else Color.WHITE)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(
                if (who.startsWith("AIRA")) Color.rgb(38, 29, 62) else Color.rgb(42, 40, 50),
                dp(15).toFloat()
            )
        }
        val params = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(4), 0, dp(4))
        }
        messages.addView(bubble, params)
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun saveHistory() {
        val array = JSONArray()
        history.forEach { (who, text) ->
            array.put(JSONObject().put("who", who).put("text", text))
        }
        prefs.edit().putString("messages", array.toString()).apply()
    }

    private fun restoreHistory() {
        val raw = prefs.getString("messages", null) ?: return
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val who = item.optString("who")
                val text = item.optString("text")
                if (who.isNotBlank() && text.isNotBlank()) {
                    history.add(who to text)
                    renderMessage(who, text)
                }
            }
        } catch (_: Exception) {
            prefs.edit().remove("messages").apply()
            history.clear()
            messages.removeAllViews()
        }
    }
}
