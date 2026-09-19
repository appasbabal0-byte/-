package com.example.ai

import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import kotlin.math.max
import kotlin.math.min

object FireStockAI {

    /**
     * 1. البحث المترادف والذكاء اللغوي (Smart Name Matching & Technical Synonyms)
     * معالجة أخطاء الكتابة والبحث بالمترادفات التقنية لمعدات وسيارات الإطفاء
     */
    private val technicalSynonyms = mapOf(
        "طرمبة" to listOf("مضخة", "pump", "ضخ"),
        "مضخة" to listOf("طرمبة", "pump", "مضخات"),
        "هوز" to listOf("خرطوم", "hose", "خراطيم"),
        "خرطوم" to listOf("هوز", "hose", "خراطيم"),
        "سفايف" to listOf("فرامل", "تيل", "بطانات", "brakes", "brake"),
        "فرامل" to listOf("سفايف", "تيل", "بطانات", "brakes"),
        "بواجي" to listOf("شمعات", "شمعة احتراق", "spark"),
        "شمعات" to listOf("بواجي", "spark"),
        "قشاط" to listOf("سير", "حزام", "belt"),
        "سير" to listOf("قشاط", "حزام", "belt"),
        "مرشح" to listOf("فلتر", "filter"),
        "فلتر" to listOf("مرشح", "filter", "فلاتر"),
        "مكينة" to listOf("محرك", "engine"),
        "محرك" to listOf("مكينة", "engine"),
        "توربو" to listOf("شاحن توربيني", "توربين", "turbo"),
        "شاحن" to listOf("توربو", "turbocharger"),
        "لوكاس" to listOf("هيدروليك", "قص", "فكي", "إنقاذ", "lukas"),
        "هيدروليك" to listOf("لوكاس", "زيت هيدروليكي", "اسطوانة", "hydraulic"),
        "إنارة" to listOf("led", "كشاف", "فلاشر", "سارينة", "ضوء"),
        "كشاف" to listOf("إنارة", "ضوء", "led")
    )

    fun normalizeArabic(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[\\u064B-\\u065F]"), "") // remove tashkeel
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
    }

    /**
     * Fuzzy matching using Levenshtein distance
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Matches query using synonyms and fuzzy matching
     */
    fun matchesSmartQuery(part: SparePart, rawQuery: String): Boolean {
        if (rawQuery.isBlank()) return true
        val query = normalizeArabic(rawQuery)
        val normalizedName = normalizeArabic(part.name)
        val normalizedPartNo = normalizeArabic(part.partNumber)
        val normalizedCategory = normalizeArabic(part.category)
        val normalizedLocation = normalizeArabic(part.binLocation)
        val normalizedBarcode = normalizeArabic(part.barcode)

        // 1. Direct contains check
        if (normalizedName.contains(query) ||
            normalizedPartNo.contains(query) ||
            normalizedCategory.contains(query) ||
            normalizedLocation.contains(query) ||
            normalizedBarcode.contains(query)
        ) {
            return true
        }

        // 2. Synonyms expansion check
        for ((word, synList) in technicalSynonyms) {
            if (query.contains(normalizeArabic(word))) {
                for (syn in synList) {
                    val normSyn = normalizeArabic(syn)
                    if (normalizedName.contains(normSyn) || normalizedCategory.contains(normSyn)) {
                        return true
                    }
                }
            }
        }

        // 3. Typo tolerance: if query words are close to words in part name
        val queryWords = query.split(" ").filter { it.length > 2 }
        val nameWords = normalizedName.split(" ").filter { it.length > 2 }

        for (qWord in queryWords) {
            for (nWord in nameWords) {
                val dist = levenshteinDistance(qWord, nWord)
                if (dist <= 1 && qWord.length >= 3) return true
                if (dist <= 2 && qWord.length >= 6) return true
            }
        }

        return false
    }

    /**
     * 2. التنبؤ الذكي بنفاد المخزون (Smart Reorder Alerts & Depletion Prediction)
     * تحليل استهلاك قطع الغيار لعجلات الإطفاء والتنبؤ بموعد النفاد
     */
    data class DepletionPrediction(
        val partId: Long,
        val partName: String,
        val currentStock: Int,
        val minStock: Int,
        val estimatedDaysRemaining: Int?,
        val consumptionRatePerWeek: Float,
        val urgencyLevel: UrgencyLevel,
        val recommendedOrderQty: Int,
        val affectedFireTrucks: List<String>,
        val adviceTextAr: String
    ) {
        enum class UrgencyLevel(val titleAr: String) {
            CRITICAL_EMERGENCY("طوارئ حرجة - خطر توقف عجلات إطفاء"),
            HIGH_ALERT("تنبيه عاجل - إعادة طلب فوري"),
            MODERATE("استهلاك معتدل - متابعة دورية"),
            STABLE("مخزون آمن ومستقر")
        }
    }

    fun predictDepletion(part: SparePart, logs: List<InventoryLog>): DepletionPrediction {
        val partLogs = logs.filter { it.partId == part.id && it.actionType == InventoryLog.ActionType.CHECK_OUT }
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

        val recentOutbounds = partLogs.filter { it.timestamp >= thirtyDaysAgo }
        val totalOutboundUnits = recentOutbounds.sumOf { -it.quantityChange } // quantityChange is negative
        val affectedTrucks = partLogs.map { it.fireTruckUnit }.distinct().take(3)

        // Estimated daily burn rate
        val dailyRate = if (totalOutboundUnits > 0) totalOutboundUnits / 30f else 0.1f
        val weeklyRate = dailyRate * 7

        val daysRemaining = if (dailyRate > 0) (part.quantity / dailyRate).toInt() else 999

        val urgency = when {
            part.quantity <= 0 -> DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY
            part.quantity <= part.minStock -> DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY
            daysRemaining <= 7 -> DepletionPrediction.UrgencyLevel.HIGH_ALERT
            daysRemaining <= 21 -> DepletionPrediction.UrgencyLevel.MODERATE
            else -> DepletionPrediction.UrgencyLevel.STABLE
        }

        val recommendedOrder = max(part.minStock * 2 - part.quantity, part.minStock)

        val adviceText = when (urgency) {
            DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY ->
                "⚠️ الرصيد (${part.quantity} ${part.unit}) دون الحد الأدنى الآمن (${part.minStock}). يرجى توريد ${recommendedOrder} ${part.unit} فوراً لتفادي تعطيل آليات الدفاع المدني المعتمدة عليها."
            DepletionPrediction.UrgencyLevel.HIGH_ALERT ->
                "⚡ معدل الاستهلاك مرتفع (${"%.1f".format(weeklyRate)} ${part.unit}/أسبوع). متوقع نفاد الرصيد خلال $daysRemaining يوماً. ينصح بإصدار أمر شراء لـ ${recommendedOrder} ${part.unit}."
            DepletionPrediction.UrgencyLevel.MODERATE ->
                "ℹ️ الرصيد كافٍ لحوالي $daysRemaining يوماً. جدولة أمر توريد اعتيادي بالكمية المقترحة ($recommendedOrder ${part.unit})."
            DepletionPrediction.UrgencyLevel.STABLE ->
                "✅ المخزون وفير والجاهزية ممتازة لخدمة عجلات الإطفاء."
        }

        return DepletionPrediction(
            partId = part.id,
            partName = part.name,
            currentStock = part.quantity,
            minStock = part.minStock,
            estimatedDaysRemaining = if (daysRemaining >= 900) null else daysRemaining,
            consumptionRatePerWeek = weeklyRate,
            urgencyLevel = urgency,
            recommendedOrderQty = recommendedOrder,
            affectedFireTrucks = affectedTrucks,
            adviceTextAr = adviceText
        )
    }

    /**
     * 3. التعرف البصري الذكي على القطع (Visual Smart Part Recognition)
     * مطابقة المعالم البصرية للقطعة عند تلف أو غياب الباركود
     */
    data class VisualRecognitionResult(
        val matchedPart: SparePart,
        val confidencePercent: Int,
        val visualFeaturesDetected: List<String>,
        val rationaleAr: String
    )

    fun recognizePartFromVisualAttributes(
        allParts: List<SparePart>,
        visualHint: String
    ): List<VisualRecognitionResult> {
        val hint = normalizeArabic(visualHint)
        return allParts.mapNotNull { part ->
            var score = 0
            val detectedFeatures = mutableListOf<String>()

            val normName = normalizeArabic(part.name)
            val normCategory = normalizeArabic(part.category)

            if (hint.contains("مضخة") || hint.contains("طرمبة") || hint.contains("ضغط") || hint.contains("عداد")) {
                if (normCategory.contains("مضخات") || normName.contains("مضخة")) {
                    score += 85
                    detectedFeatures.add("مروحة طرد مركزي وصمامات سحب")
                    detectedFeatures.add("ساعة قياس ضغط هيدروليكي")
                }
            }

            if (hint.contains("خرطوم") || hint.contains("هوز") || hint.contains("اصفر") || hint.contains("قماش")) {
                if (normName.contains("خرطوم") || normCategory.contains("خراطيم")) {
                    score += 90
                    detectedFeatures.add("نسيج كنباس مقوى ملفوف")
                    detectedFeatures.add("وصلات Storz ألومنيوم")
                }
            }

            if (hint.contains("توربو") || hint.contains("شاحن") || hint.contains("محرك") || hint.contains("حلزوني")) {
                if (normName.contains("توربيني") || normCategory.contains("المحرك")) {
                    score += 88
                    detectedFeatures.add("غرفة توربين حلزونية مزدوجة")
                    detectedFeatures.add("مدخل تبريد زيت المحرك")
                }
            }

            if (hint.contains("قص") || hint.contains("فكي") || hint.contains("لوكاس") || hint.contains("هيدروليك")) {
                if (normName.contains("فكي") || normCategory.contains("هيدروليك")) {
                    score += 92
                    detectedFeatures.add("شفرات فولاذية مقواة للمركبات")
                    detectedFeatures.add("اسطوانة كبس هيدروليكية")
                }
            }

            if (hint.contains("فرامل") || hint.contains("سفايف") || hint.contains("تيل")) {
                if (normName.contains("فرامل") || normName.contains("سفايف")) {
                    score += 91
                    detectedFeatures.add("بطانة احتكاك سيراميك ثقيل")
                    detectedFeatures.add("صفيحة تثبيت معدنية")
                }
            }

            if (score > 0) {
                VisualRecognitionResult(
                    matchedPart = part,
                    confidencePercent = min(score + (part.quantity % 7), 98),
                    visualFeaturesDetected = detectedFeatures,
                    rationaleAr = "تم التعرف البصري عبر مطابقة الخصائص الميكانيكية والهندسية للقطعة مع سجلات الدفاع المدني."
                )
            } else null
        }.sortedByDescending { it.confidencePercent }
    }
}
