package me.osku.xiedeworkbook.ui

import me.osku.xiedeworkbook.data.PracticeBook

/**
 * MainViewModel的扩展方法，用于随机练习功能
 */
fun MainViewModel.startRandomPractice(book: PracticeBook, randomSize: Int = 50) {
    if (book.canRandomize && book.content.isNotEmpty()) {
        val shuffledContent = book.content.toCharArray()
            .distinct()
            .shuffled()
            .take(randomSize)
            .joinToString("")

        val randomBook = PracticeBook(
            name = "${book.name}（隨機）",
            content = shuffledContent,
            isBuiltIn = false,
            canRandomize = false
        )
        startPractice(randomBook)
    } else {
        startPractice(book)
    }
}
