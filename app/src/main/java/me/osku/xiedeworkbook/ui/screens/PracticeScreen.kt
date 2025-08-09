package me.osku.xiedeworkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.osku.xiedeworkbook.ref.rememberDrawingState
import me.osku.xiedeworkbook.ui.MainViewModel
import me.osku.xiedeworkbook.ui.components.PracticeCanvas
import me.osku.xiedeworkbook.ui.components.AutoSentenceCanvas
import me.osku.xiedeworkbook.ui.components.LayoutInfo

/**
 * 練習畫面 - App的核心功能頁面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val drawingState = rememberDrawingState()
    val currentBook = viewModel.currentBook
    val coroutineScope = rememberCoroutineScope()

    // 用於儲存動態計算的佈局資訊
    var currentLayoutInfo by remember { mutableStateOf<LayoutInfo?>(null) }

    // 當頁面或練習簿改變時清除畫布
    LaunchedEffect(viewModel.currentPage, currentBook?.id) {
        drawingState.clear()
    }

    if (currentBook == null) {
        // 錯誤狀態，返回上一頁
        LaunchedEffect(Unit) {
            viewModel.navigateBack()
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 頂部欄
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = currentBook.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // 使用動態計算的頁面資訊
                    val pageInfo = if (viewModel.practiceSettings.isAutoSentenceMode &&
                                     currentBook.supportsAutoSentence &&
                                     !viewModel.practiceSettings.isSingleCharMode) {
                        // 自動斷句模式：每頁顯示一個句組
                        val maxPages = currentBook.sentences.size
                        "${viewModel.currentPage + 1}/$maxPages"
                    } else {
                        // 一般模式的頁面資訊
                        currentLayoutInfo?.let { layoutInfo ->
                            val maxPages = viewModel.calculateMaxPages(layoutInfo.charsPerPage)
                            "${viewModel.currentPage + 1}/$maxPages"
                        } ?: viewModel.currentPageInfo
                    }

                    Text(
                        text = pageInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.showSettings() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "設定"
                    )
                }
            }
        )

        // 頂部部控制列
        TopControlBar(
            onPreviousPage = { viewModel.previousPage() },
            onNextPage = {
                currentLayoutInfo?.let { layoutInfo ->
                    viewModel.nextPage(layoutInfo.charsPerPage)
                } ?: viewModel.nextPage()
            },
            onClear = {
                drawingState.undo()
            },
            onLongPressClear = {
                drawingState.clear()
            },
            onSettings = { viewModel.showSettings() },
            canGoPrevious = viewModel.currentPage > 0,
            canGoNext = run {
                if (viewModel.practiceSettings.isAutoSentenceMode &&
                    currentBook.supportsAutoSentence &&
                    !viewModel.practiceSettings.isSingleCharMode) {
                    // 自動斷句模式：檢查是否還有句組
                    viewModel.currentPage < currentBook.sentences.size - 1
                } else {
                    // 一般模式的頁數計算
                    currentLayoutInfo?.let { layoutInfo ->
                        val maxPages = viewModel.calculateMaxPages(layoutInfo.charsPerPage)
                        viewModel.currentPage < maxPages - 1
                    } ?: run {
                        // 回退到估算邏輯
                        val estimatedCharsPerPage = when {
                            viewModel.practiceSettings.multiCharGridSize <= 80f -> 20
                            viewModel.practiceSettings.multiCharGridSize <= 120f -> 12
                            else -> 6
                        }
                        val maxPages = viewModel.calculateMaxPages(estimatedCharsPerPage)
                        viewModel.currentPage < maxPages - 1
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // 主要練習區域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            // 根據設定選擇顯示模式
            if (viewModel.practiceSettings.isAutoSentenceMode &&
                currentBook.supportsAutoSentence &&
                !viewModel.practiceSettings.isSingleCharMode) {
                // 自動斷句模式：每頁顯示一個句組
                val currentSentence = if (viewModel.currentPage < currentBook.sentences.size) {
                    currentBook.sentences[viewModel.currentPage]
                } else ""

                AutoSentenceCanvas(
                    modifier = Modifier.fillMaxSize(),
                    sentence = currentSentence,
                    settings = viewModel.practiceSettings,
                    drawingState = drawingState
                )
            } else {
                // 一般模式（單字模式或多字模式）
                PracticeCanvas(
                    modifier = Modifier.fillMaxSize(),
                    characters = currentBook.characters,
                    currentPage = viewModel.currentPage,
                    settings = viewModel.practiceSettings,
                    drawingState = drawingState,
                    onLayoutInfoChanged = { layoutInfo ->
                        currentLayoutInfo = layoutInfo
                    }
                )
            }
        }
    }
}

/**
 * 頂部控制列組件
 */
@Composable
private fun TopControlBar(
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onClear: () -> Unit,
    onLongPressClear: () -> Unit,
    onSettings: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isLongPress by remember { mutableStateOf(false) }
    var longPressJob: Job? by remember { mutableStateOf<Job?>(null) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一頁按鈕
        Button(
            onClick = onPreviousPage,
            enabled = canGoPrevious,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "上一頁",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        // 智慧清除按鈕（短按復原/長按全清）
        Button(
            onClick = onClear,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    isLongPress = false
                                    longPressJob = coroutineScope.launch {
                                        delay(500L) // 長按500毫秒觸發
                                        isLongPress = true
                                        onLongPressClear()
                                    }
                                }
                                PointerEventType.Release -> {
                                    longPressJob?.cancel()
                                }
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = "清除",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        // 下一頁按鈕
        Button(
            onClick = onNextPage,
            enabled = canGoNext,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "下一頁",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
