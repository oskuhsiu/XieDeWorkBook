package me.osku.xiedeworkbook.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import me.osku.xiedeworkbook.utils.TextProcessor
import java.util.UUID

/**
 * 練習簿數據模型
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class PracticeBook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String, // 練習內容的字串
    // 修復：characters 應該是過濾掉標點符號後的字符列表
    val characters: List<String> = run {
        // 如果文本支持自動斷句模式，使用預處理後的純文字
        if (TextProcessor.canUseAutoSentenceMode(content)) {
            TextProcessor.preprocessText(content).joinToString("").toCharArray().map { it.toString() }
        } else {
            // 一般文本，直接轉換為字符列表
            content.toCharArray().map { it.toString() }
        }
    },
    val isBuiltIn: Boolean = false,
    val canRandomize: Boolean = false, // 是否支持随机选项
    val createdAt: Long = System.currentTimeMillis(),
    // 自動斷句相關屬性
    val sentences: List<String> = TextProcessor.preprocessText(content), // 句組列表
    val supportsAutoSentence: Boolean = TextProcessor.canUseAutoSentenceMode(content) // 是否支持自動斷句模式
)

/**
 * 練習模式設定
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class PracticeSettings(
    val isFingerMode: Boolean = true, // true: 手指模式, false: 觸控筆模式
    val isSingleCharMode: Boolean = true, // true: 單字模式, false: 多字模式
    val strokeWidth: Float = 5f, // 筆跡寬度
    val singleCharGridSize: Float = 200f, // 單字模式格子大小
    val multiCharGridSize: Float = 120f, // 多字模式格子大小
    val fontType: String = "KaiTi", // 字型類型
    val gridStyle: GridStyle = GridStyle.RICE_GRID, // 格線樣式
    val showZhuyin: Boolean = false, // 是否顯示注音
    val isAutoSentenceMode: Boolean = false // 是否啟用自動斷句顯示模式
)

/**
 * 格線樣式
 */
@Serializable
enum class GridStyle(val displayName: String) {
    RICE_GRID("米字格"),
    NINE_GRID("九宮格")
}
