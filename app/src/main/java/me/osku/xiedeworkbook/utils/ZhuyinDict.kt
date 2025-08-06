package me.osku.xiedeworkbook.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 注音字典管理類，基於 ZhuyinActivity.kt 的實現
 */
object ZhuyinDict {
    private val dict: MutableMap<String, MutableList<String>> = mutableMapOf()
    private var loaded = false

    suspend fun load(context: Context) {
        if (loaded) return

        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("word4k.tsv")
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                reader.forEachLine { line ->
                    val parts = line.trim().split('\t')
                    if (parts.size >= 2) {
                        val word = parts[0]
                        val zhuyin = parts[1]
                        dict.getOrPut(word) { mutableListOf() }.add(zhuyin)
                    }
                }
                reader.close()
                loaded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getZhuyin(char: Char): String {
        val list = dict[char.toString()]

        //暫時改成反向搜索，常用音似乎都在放面..?
        return list?.lastOrNull() ?: ""
    }

    fun hasZhuyin(char: Char): Boolean {
        return dict.containsKey(char.toString())
    }
}

/**
 * 注音組件數據類，用於定位注音符號
 */
data class ZhuyinComponent(
    val char: Char,
    val x: Float,
    val y: Float,
    val isTone: Boolean = false
)

/**
 * 解析注音符號並計算位置，基於 ZhuyinActivity.kt 的實現
 */
fun parseZhuyinWithTones(zhuyin: String): List<ZhuyinComponent> {
    val components = mutableListOf<ZhuyinComponent>()
    val tones = setOf('ˊ', 'ˇ', 'ˋ', '˙') // 二聲、三聲、四聲、輕聲

    // 分離聲調符號和其他符號
    val nonToneChars = mutableListOf<Char>()
    val toneChars = mutableListOf<Char>()

    zhuyin.forEach { char ->
        if (tones.contains(char)) {
            toneChars.add(char)
        } else {
            nonToneChars.add(char)
        }
    }

    // 先添加非聲調符號
    nonToneChars.forEachIndexed { index, char ->
        components.add(ZhuyinComponent(char, 0f, index.toFloat(), false))
    }

    // 計算聲調符號的位置（中間偏右）
    val middleIndex = nonToneChars.size / 2f
    val toneX = 1.6f // 偏右的距離

    toneChars.forEach { toneChar ->
        when (toneChar) {
            '˙' -> {
                // 輕聲符號放在最上方
                components.add(ZhuyinComponent(toneChar, 0.5f, -0.5f, true))
            }

            else -> {
                // 其他聲調符號放在中間偏右
                components.add(
                    ZhuyinComponent(
                        toneChar,
                        toneX,
                        if (nonToneChars.size > 1) middleIndex else 0f,
                        true
                    )
                )
            }
        }
    }

    return components
}
