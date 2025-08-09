package me.osku.xiedeworkbook.ui

import me.osku.xiedeworkbook.data.PracticeBook
import me.osku.xiedeworkbook.utils.TextProcessor

/**
 * MainViewModel的扩展方法，用于随机练习功能
 */
fun MainViewModel.startRandomPractice(book: PracticeBook, randomSize: Int = 50) {
    if (book.canRandomize && book.content.isNotEmpty()) {
        val randomContent = if (TextProcessor.canUseAutoSentenceMode(book.content)) {
            // 支援斷句模式：從句組中隨機選擇
            val sentences = TextProcessor.preprocessText(book.content)
            if (sentences.isNotEmpty()) {
                // 隨機選擇句組，優先選擇較短的句組以適合練習
                val shuffledSentences = sentences.shuffled()
                val selectedSentences = mutableListOf<String>()
                var totalLength = 0

                for (sentence in shuffledSentences) {
                    if (totalLength + sentence.length <= randomSize) {
                        selectedSentences.add(sentence)
                        totalLength += sentence.length
                    }
                    // 如果已選擇的內容長度達到目標，就停止
                    if (totalLength >= randomSize * 0.8) break // 允許80%的彈性
                }

                // 如果沒有選到任何句組，至少選擇第一個句組
                if (selectedSentences.isEmpty() && shuffledSentences.isNotEmpty()) {
                    selectedSentences.add(shuffledSentences.first())
                }

                selectedSentences.joinToString("")
            } else {
                // 如果預處理失敗，回退到字符隨機模式
                book.content.toCharArray()
                    .distinct()
                    .shuffled()
                    .take(randomSize)
                    .joinToString("")
            }
        } else {
            // 不支援斷句模式：使用原有的字符隨機選擇
            book.content.toCharArray()
                .distinct()
                .shuffled()
                .take(randomSize)
                .joinToString("")
        }

        val randomBook = PracticeBook(
            name = "${book.name}（隨機）",
            content = randomContent,
            isBuiltIn = false,
            canRandomize = false
        )
        startPractice(randomBook)
    } else {
        startPractice(book)
    }
}
