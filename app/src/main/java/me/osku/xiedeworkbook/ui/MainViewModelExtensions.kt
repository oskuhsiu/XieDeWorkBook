package me.osku.xiedeworkbook.ui

import me.osku.xiedeworkbook.data.PracticeBook

/**
 * MainViewModel 擴展函數，提供隨機練習功能
 */
fun MainViewModel.startRandomPractice(book: PracticeBook) {
    if (book.canRandomize && book.content.isNotEmpty()) {
        // 隨機打亂字符順序
        val shuffledContent = book.content.toList().shuffled().joinToString("")

        // 創建隨機練習簿
        val randomBook = PracticeBook(
            name = "${book.name}(隨機)",
            content = shuffledContent,
            isBuiltIn = false
        )

        startPractice(randomBook)
    } else {
        // 如果不支持隨機，直接開始正常練習
        startPractice(book)
    }
}
