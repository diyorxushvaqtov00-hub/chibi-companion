package uz.diyor.companion.ai

/**
 * Temporary deterministic provider used until a real local model is installed.
 * Its replies are explicitly marked as fallback/demo output.
 */
class OfflineDemoProvider : AiProvider {
    override val id: String = "offline-demo"

    override fun generate(request: AiRequest, callback: AiCallback) {
        val message = request.userMessage.trim()
        if (message.isEmpty()) {
            callback.onFailure(AiFailure.InvalidRequest("Xabar bo‘sh."))
            return
        }

        val lower = message.lowercase()
        val reply = when {
            listOf("salom", "assalom", "hello", "hi").any(lower::contains) ->
                "Salom. Seni eshitib turibman. Nima gap?"
            listOf("isming", "kimsan", "kim san").any(lower::contains) ->
                "Men AIRA — virtual hamrohingman. Sovuqqon fikrlayman, ammo yaqinlarimga befarq emasman."
            listOf("rahmat", "raxmat").any(lower::contains) ->
                "Arzimaydi. Kerak bo‘lsa, yana yordam beraman."
            listOf("xafa", "yomonman", "zerik", "tushkun").any(lower::contains) ->
                "Tushundim. Maslahat berishga shoshilmayman. Nima bo‘lganini aytib berishing mumkin. Men tinglayman."
            listOf("anime", "manga", "manhwa").any(lower::contains) ->
                "Qiziqarli mavzu. Qahramonning kuchidan ko‘ra, uning qarorlari ko‘proq narsani ochib beradi."
            listOf("yordam", "reja", "qanday").any(lower::contains) ->
                "Avval muammoni aniq belgilaymiz. Keyin eng sodda va samarali yechimni tanlaymiz."
            lower.contains("?") ->
                "Savolingni tushundim. Hozir men offline demo rejimidaman, shuning uchun haqiqiy AI kabi erkin fikrlay olmayman."
            else ->
                "Tushunarli. Davom et — gaplaringni diqqat bilan kuzatyapman."
        }

        callback.onSuccess(
            AiReply(
                text = reply,
                providerId = id,
                isFallback = true
            )
        )
    }
}
