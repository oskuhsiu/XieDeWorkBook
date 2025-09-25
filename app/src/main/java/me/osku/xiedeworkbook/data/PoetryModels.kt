package me.osku.xiedeworkbook.data

import kotlinx.serialization.Serializable

/**
 * 詩詞書籍模型
 */
@Serializable
data class PoetryBook(
    val bookTitle: String,
    val fileName: String,
    var poemList: List<Poem> = emptyList(), // 懶加載，初始為空
    var isLoaded: Boolean = false
)

/**
 * 單首詩詞模型
 */
@Serializable
data class Poem(
    val title: String,
    val author: String,
    val content: String, // 原始內容，保留換行符
    val sentences: List<String> = emptyList() // 經TextProcessor處理後的句組列表
)

/**
 * 詩詞簿模式枚舉
 */
enum class PoetryMode {
    APPRECIATION, // 賞析模式
    PRACTICE      // 練習書寫模式
}
