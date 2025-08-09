package me.osku.xiedeworkbook.utils

/**
 * 文本預處理工具類，實現自動斷句功能
 */
object TextProcessor {

    // 斷句型標點符號
    private val sentenceEndPunctuations = setOf('。', '，', '、')

    // 所有標點符號（用於移除）
    private val allPunctuations = setOf(
        '。', '，', '、', '？', '！', '：', '；', '「', '」', '『', '』',
        '"', '"', '\'', '\'', '(', ')', '（', '）', '[', ']', '【', '】',
        '…', '—', '–', '·', '〈', '〉', '《', '》'
    )

    /**
     * 第一階段：輸入內容預處理
     * 流程：原始輸入 -> 去除換行 -> 依指定標點斷句 -> 去除句內所有標點 -> 得到純文字的句組列表
     */
    fun preprocessText(rawInput: String): List<String> {
        if (rawInput.isBlank()) return emptyList()

        // 步驟1：移除換行符號
        val noNewlines = rawInput.replace(Regex("[\r\n]+"), "")

        // 步驟2：依標點符號進行斷句
        val sentences = splitBySentenceEndPunctuations(noNewlines)

        // 步驟3：移除句組內所有標點符號
        return sentences.map { sentence ->
            removePunctuations(sentence).trim()
        }.filter { it.isNotEmpty() }
    }

    /**
     * 依斷句型標點符號分割文本
     */
    private fun splitBySentenceEndPunctuations(text: String): List<String> {
        val result = mutableListOf<String>()
        val currentSentence = StringBuilder()

        for (char in text) {
            currentSentence.append(char)
            if (char in sentenceEndPunctuations) {
                result.add(currentSentence.toString())
                currentSentence.clear()
            }
        }

        // 處理最後一段沒有標點結尾的文字
        if (currentSentence.isNotEmpty()) {
            result.add(currentSentence.toString())
        }

        return result
    }

    /**
     * 移除所有標點符號
     */
    private fun removePunctuations(text: String): String {
        return text.filter { it !in allPunctuations }
    }

    /**
     * 判斷文本是否適合自動斷句模式
     * 條件：包含斷句型標點符號
     */
    fun canUseAutoSentenceMode(text: String): Boolean {
        return text.any { it in sentenceEndPunctuations }
    }

    /**
     * 計算句組的排版信息
     */
    fun calculateSentenceLayout(sentence: String): SentenceLayout {
        val length = sentence.length
        return when {
            length <= 0 -> SentenceLayout(0, emptyList())
            length <= 3 -> SentenceLayout(1, listOf(length)) // 3字句：1欄
            length <= 5 -> SentenceLayout(1, listOf(length)) // 5字句：1欄
            length == 7 -> SentenceLayout(2, listOf(4, 3)) // 7字句：右欄4字，左欄3字
            length <= 15 -> {
                // 長句：自動換欄，每欄最多5字
                val columns = mutableListOf<Int>()
                var remaining = length
                while (remaining > 0) {
                    val columnSize = minOf(5, remaining)
                    columns.add(columnSize)
                    remaining -= columnSize
                }
                SentenceLayout(columns.size, columns)
            }
            else -> {
                // 超過15字：只顯示前15字，3欄各5字
                SentenceLayout(3, listOf(5, 5, 5))
            }
        }
    }
}

/**
 * 句組排版信息
 * @param columnCount 列數
 * @param columnSizes 每列的字數
 */
data class SentenceLayout(
    val columnCount: Int,
    val columnSizes: List<Int>
) {
    val maxCharsPerColumn: Int = if (columnSizes.isEmpty()) 0 else columnSizes.maxOf { it }
    val totalChars: Int = columnSizes.sum()
}
