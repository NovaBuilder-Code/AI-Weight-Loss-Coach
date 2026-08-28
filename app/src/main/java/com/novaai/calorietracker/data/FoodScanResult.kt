package com.novaai.calorietracker.data

/**
 * One detected food from the Nova food-scan backend.
 * All values are estimates from the AI model.
 */
data class FoodItem(
    val name: String,
    val estimatedPortion: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

/** Full result of a photo scan, in the strict schema the Worker returns. */
data class FoodScanResult(
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val confidence: String,
    val disclaimer: String
)

/**
 * In-progress edit of one [FoodItem] before it is logged. Fields stay strings
 * so the user can clear a box while typing; [FoodScanEdit.parseDraft] turns
 * a complete, valid draft into a [FoodItem].
 */
data class FoodEditDraft(
    val name: String,
    val estimatedPortion: String,
    val calories: String,
    val proteinG: String,
    val carbsG: String,
    val fatG: String
) {
    companion object {
        fun from(item: FoodItem) = FoodEditDraft(
            name = item.name,
            estimatedPortion = item.estimatedPortion,
            calories = item.calories.toString(),
            proteinG = formatNumber(item.proteinG),
            carbsG = formatNumber(item.carbsG),
            fatG = formatNumber(item.fatG)
        )

        private fun formatNumber(v: Double): String =
            if (v == Math.floor(v) && !v.isInfinite()) v.toInt().toString()
            else v.toString().trimEnd('0').trimEnd('.')
    }
}

/**
 * Validation and totals for the editable food-scan result. Pure Kotlin so it
 * is unit-testable on the JVM with no Compose/Android dependency.
 */
object FoodScanEdit {

    const val DISCLAIMER = "AI estimates may be inaccurate. Adjust portions or values if needed."

    fun parseDraft(draft: FoodEditDraft): FoodItem? {
        val name = draft.name.trim()
        if (name.isEmpty()) return null
        val calories = draft.calories.trim().toIntOrNull() ?: return null
        if (calories < 0) return null
        val protein = parseNonNegDouble(draft.proteinG) ?: return null
        val carbs = parseNonNegDouble(draft.carbsG) ?: return null
        val fat = parseNonNegDouble(draft.fatG) ?: return null
        return FoodItem(
            name = name,
            estimatedPortion = draft.estimatedPortion.trim(),
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat
        )
    }

    fun parseAll(drafts: List<FoodEditDraft>): List<FoodItem>? {
        val items = ArrayList<FoodItem>(drafts.size)
        for (draft in drafts) {
            items.add(parseDraft(draft) ?: return null)
        }
        return items
    }

    /**
     * Rebuilds a scan result from edited drafts. Totals are always the sum of
     * the edited foods — the original AI [FoodScanResult.totalCalories] is not
     * reused. Returns null when any draft is invalid.
     */
    fun resultFromDrafts(original: FoodScanResult, drafts: List<FoodEditDraft>): FoodScanResult? {
        val foods = parseAll(drafts) ?: return null
        return original.copy(
            foods = foods,
            totalCalories = foods.sumOf { it.calories },
            disclaimer = DISCLAIMER
        )
    }

    private fun parseNonNegDouble(text: String): Double? {
        val v = text.trim().toDoubleOrNull() ?: return null
        return if (v >= 0.0 && !v.isNaN() && !v.isInfinite()) v else null
    }
}

/**
 * Tolerant parser for the Nova food-scan JSON returned by the Cloudflare
 * Worker. Pure Kotlin with no Android/JSON-library dependencies so it is
 * directly unit-testable on the JVM. Unknown/missing fields fall back to
 * safe defaults; anything that is not a JSON object returns null.
 */
object FoodScanJson {

    const val DEFAULT_DISCLAIMER = FoodScanEdit.DISCLAIMER

    /** Parse any JSON text into Map/List/String/Double/Boolean/null values. */
    fun parse(text: String): Any? = try {
        JsonParser(text).parseValue()
    } catch (e: JsonParser.ParseException) {
        null
    }

    /** Parse the scan schema. Returns null for malformed/non-object input. */
    fun parseScanResult(text: String): FoodScanResult? {
        val root = parse(text) as? Map<*, *> ?: return null

        val foods = (root["foods"] as? List<*>)
            ?.mapNotNull { item -> (item as? Map<*, *>)?.let { foodFrom(it) } }
            ?.take(MAX_FOODS)
            ?: emptyList()

        val foodSum = foods.sumOf { it.calories.toLong() }.toInt()
        val total = (root["totalCalories"] as? Number)
            ?.toDouble()
            ?.let { if (it >= 0) it.toInt() else foodSum }
            ?: foodSum
        val confidence = (root["confidence"] as? String)
            ?.lowercase()
            ?.takeIf { it in CONFIDENCE_LEVELS }
            ?: "medium"
        val disclaimer = (root["disclaimer"] as? String)
            ?.trim()
            ?.take(200)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_DISCLAIMER

        return FoodScanResult(foods, total, confidence, disclaimer)
    }

    private fun foodFrom(map: Map<*, *>): FoodItem = FoodItem(
        name = (map["name"] as? String)?.trim()?.take(80)?.takeIf { it.isNotEmpty() } ?: "food",
        estimatedPortion = (map["estimatedPortion"] as? String)?.trim()?.take(40) ?: "",
        calories = (map["calories"] as? Number)?.toDouble()?.let { if (it >= 0) it.toInt() else 0 } ?: 0,
        proteinG = nonNegative(map["proteinG"]),
        carbsG = nonNegative(map["carbsG"]),
        fatG = nonNegative(map["fatG"])
    )

    private fun nonNegative(value: Any?): Double =
        (value as? Number)?.toDouble()?.coerceAtLeast(0.0) ?: 0.0

    private const val MAX_FOODS = 10
    private val CONFIDENCE_LEVELS = setOf("high", "medium", "low")

    /** Minimal recursive-descent JSON parser (objects, arrays, strings,
     *  numbers, booleans, null). Throws on malformed input. */
    private class JsonParser(private val s: String) {

        class ParseException : Exception()

        private var i = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (i >= s.length) throw ParseException()
            return when (val c = s[i]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't' -> { expect("true"); true }
                'f' -> { expect("false"); false }
                'n' -> { expect("null"); null }
                else -> if (c == '-' || c.isDigit()) parseNumber() else throw ParseException()
            }
        }

        private fun parseObject(): Any {
            i++ // '{'
            skipWhitespace()
            val map = LinkedHashMap<String, Any?>()
            if (peek('}')) { i++; return map }
            while (true) {
                skipWhitespace()
                val key = parseString() as? String ?: throw ParseException()
                skipWhitespace()
                if (!peek(':')) throw ParseException()
                i++
                map[key] = parseValue()
                skipWhitespace()
                when {
                    peek('}') -> { i++; return map }
                    peek(',') -> i++
                    else -> throw ParseException()
                }
            }
        }

        private fun parseArray(): Any {
            i++ // '['
            skipWhitespace()
            val list = ArrayList<Any?>()
            if (peek(']')) { i++; return list }
            while (true) {
                skipWhitespace()
                list.add(parseValue())
                skipWhitespace()
                when {
                    peek(']') -> { i++; return list }
                    peek(',') -> i++
                    else -> throw ParseException()
                }
            }
        }

        private fun parseString(): Any? {
            i++ // '"'
            val sb = StringBuilder()
            while (true) {
                if (i >= s.length) throw ParseException()
                val c = s[i]
                when {
                    c == '"' -> { i++; return sb.toString() }
                    c == '\\' -> {
                        i++
                        if (i >= s.length) throw ParseException()
                        when (val e = s[i]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (i + 4 >= s.length) throw ParseException()
                                val hex = s.substring(i + 1, i + 5)
                                val code = hex.toIntOrNull(16) ?: throw ParseException()
                                sb.append(code.toChar())
                                i += 4
                            }
                            else -> throw ParseException()
                        }
                        i++
                    }
                    else -> { sb.append(c); i++ }
                }
            }
        }

        private fun parseNumber(): Any {
            val start = i
            if (s[i] == '-') i++
            while (i < s.length && (s[i].isDigit() || s[i] in ".eE+-")) i++
            val text = s.substring(start, i)
            return text.toDoubleOrNull() ?: throw ParseException()
        }

        private fun expect(word: String) {
            if (!s.startsWith(word, i)) throw ParseException()
            i += word.length
        }

        private fun peek(c: Char): Boolean = i < s.length && s[i] == c

        private fun skipWhitespace() {
            while (i < s.length && s[i].isWhitespace()) i++
        }
    }
}
