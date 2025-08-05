package me.osku.xiedeworkbook.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 練習簿數據模型
 */
@Serializable
data class PracticeBook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String, // 練習內容的字串
    val characters: List<String> = content.toCharArray().map { it.toString() },
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 練習模式設定
 */
@Serializable
data class PracticeSettings(
    val isFingerMode: Boolean = true, // true: 手指模式, false: 觸控筆模式
    val isSingleCharMode: Boolean = true, // true: 單字模式, false: 多字模式
    val strokeWidth: Float = 5f, // 筆跡寬度
    val singleCharGridSize: Float = 200f, // 單字模式格子大小
    val multiCharGridSize: Float = 120f, // 多字模式格子大小
    val fontType: String = "KaiTi", // 字型類型
    val gridStyle: GridStyle = GridStyle.RICE_GRID, // 格線樣式
    val showZhuyin: Boolean = false // 是否顯示注音
)

/**
 * 格線樣式
 */
@Serializable
enum class GridStyle(val displayName: String) {
    RICE_GRID("米字格"),
    NINE_GRID("九宮格")
}
