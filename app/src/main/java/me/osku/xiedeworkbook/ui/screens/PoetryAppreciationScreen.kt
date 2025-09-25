package me.osku.xiedeworkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.osku.xiedeworkbook.data.Poem
import me.osku.xiedeworkbook.data.PoetryMode
import me.osku.xiedeworkbook.ui.MainViewModel

/**
 * D-1. 賞析模式 (Appreciation Mode)
 * 智慧排版顯示詩詞內容，支援分頁瀏覽
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoetryAppreciationScreen(
    viewModel: MainViewModel,
    poem: Poem,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    // 計算分頁內容
    val pageContents = remember(poem) {
        calculateAppreciationPages(poem.sentences)
    }

    val totalPages = pageContents.size.coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 頂部欄
        TopAppBar(
            title = {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                // 切換到練習模式
                IconButton(
                    onClick = { viewModel.startPoetryMode(poem, PoetryMode.PRACTICE) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "練習書寫"
                    )
                }
            }
        )

        // 主要內容區域
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {
            // 詩詞內容顯示
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (pageContents.isNotEmpty()) {
                        // 作者信息（僅第一頁顯示）
                        if (currentPage == 0) {
                            Text(
                                text = poem.author,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }

                        // 當前頁內容 - 移除捲動功能
                        Text(
                            text = pageContents[currentPage],
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp,  // 稍微減小字體以確保內容適合螢幕
                                lineHeight = 36.sp, // 相應調整行高
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                            // 完全移除 verticalScroll - 賞析模式不應該捲動
                        )
                    }
                }
            }
        }

        // 底部控制欄
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一頁按鈕
                IconButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上一頁"
                    )
                }

                // 頁面指示器與練習按鈕
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 頁面指示器
                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 練習書寫按鈕
                    FilledTonalButton(
                        onClick = { viewModel.startPoetryMode(poem, PoetryMode.PRACTICE) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("練習書寫")
                    }
                }

                // 下一頁按鈕
                IconButton(
                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "下一頁"
                    )
                }
            }
        }
    }
}

/**
 * 計算賞析模式的分頁內容
 * 根據句組長度智慧分頁，確保每頁內容適合螢幕顯示，無需捲動
 */
private fun calculateAppreciationPages(sentences: List<String>): List<String> {
    if (sentences.isEmpty()) return emptyList()

    val pages = mutableListOf<String>()
    val maxCharsPerPage = 40 // 減少每頁字數限制，確保不需要捲動
    val currentPage = StringBuilder()

    for (sentence in sentences) {
        val sentenceWithBreak = if (currentPage.isNotEmpty()) "\n\n$sentence" else sentence

        // 如果加上這句會超過限制，且當前頁不為空，則開始新頁
        if (currentPage.isNotEmpty() &&
            (currentPage.length + sentenceWithBreak.length) > maxCharsPerPage) {
            pages.add(currentPage.toString())
            currentPage.clear()
            currentPage.append(sentence)
        } else {
            currentPage.append(sentenceWithBreak)
        }

        // 如果單句就超過限制，強制分頁
        if (currentPage.length > maxCharsPerPage) {
            pages.add(currentPage.toString())
            currentPage.clear()
        }
    }

    // 添加最後一頁
    if (currentPage.isNotEmpty()) {
        pages.add(currentPage.toString())
    }

    return pages.ifEmpty { listOf("") } // 確保至少有一頁
}
