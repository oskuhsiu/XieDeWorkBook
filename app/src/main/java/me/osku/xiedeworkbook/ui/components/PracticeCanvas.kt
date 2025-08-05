package me.osku.xiedeworkbook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.osku.xiedeworkbook.data.GridStyle
import me.osku.xiedeworkbook.data.PracticeSettings
import me.osku.xiedeworkbook.ref.DrawingState
import me.osku.xiedeworkbook.ref.rememberDrawingState

/**
 * 佈局資訊數據類
 */
data class LayoutInfo(
    val maxCharsPerRow: Int,
    val maxRows: Int,
    val charsPerPage: Int
)

/**
 * 練習畫布組件 - 支援單字和多字模式
 */
@Composable
fun PracticeCanvas(
    modifier: Modifier = Modifier,
    characters: List<String>,
    currentPage: Int,
    settings: PracticeSettings,
    drawingState: DrawingState = rememberDrawingState(),
    onDrawingInteraction: () -> Unit = {},
    onLayoutInfoChanged: ((LayoutInfo) -> Unit)? = null
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // 根據設定決定格子大小
    val gridSize = if (settings.isSingleCharMode) {
        settings.singleCharGridSize.dp
    } else {
        settings.multiCharGridSize.dp
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            val id = change.id.value
                            when {
                                change.pressed && !change.previousPressed -> {
                                    drawingState.startPath(id.toInt(), change.position)
                                    onDrawingInteraction()
                                }
                                change.pressed && change.previousPressed -> {
                                    drawingState.appendToPath(id.toInt(), change.position)
                                }
                                !change.pressed && change.previousPressed -> {
                                    drawingState.endPath(id.toInt())
                                }
                            }
                            change.consume()
                        }
                    }
                }
            }
    ) {
        // 計算多字模式的佈局（在 Canvas 內部可以獲取 size）
        val currentLayoutInfo = if (!settings.isSingleCharMode) {
            val gridSizePx = gridSize.toPx()
            val spacing = 20.dp.toPx()
            val topMargin = 50.dp.toPx()
            val bottomMargin = 100.dp.toPx()
            val sideMargin = 16.dp.toPx()

            // 計算實際可用空間
            val availableWidth = size.width - (sideMargin * 2)
            val availableHeight = size.height - topMargin - bottomMargin

            // 計算能容納的行列數（考慮格子間距）
            val maxCharsPerRow = if (availableWidth > 0) {
                ((availableWidth + spacing) / (gridSizePx + spacing)).toInt().coerceAtLeast(1)
            } else 1

            val maxRows = if (availableHeight > 0) {
                ((availableHeight + spacing) / (gridSizePx + spacing)).toInt().coerceAtLeast(1)
            } else 1

            val charsPerPage = maxCharsPerRow * maxRows

            LayoutInfo(maxCharsPerRow, maxRows, charsPerPage)
        } else {
            LayoutInfo(1, 1, 1) // 單字模式
        }

        // 通知佈局資訊變更
        onLayoutInfoChanged?.invoke(currentLayoutInfo)

        // 計算當前頁面要顯示的字符
        val displayCharacters = if (settings.isSingleCharMode) {
            if (currentPage < characters.size) listOf(characters[currentPage]) else listOf("")
        } else {
            val startIndex = currentPage * currentLayoutInfo.charsPerPage
            val endIndex = minOf(startIndex + currentLayoutInfo.charsPerPage, characters.size)
            if (startIndex < characters.size) {
                characters.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

        // 繪製練習格和字符
        if (settings.isSingleCharMode) {
            drawSingleCharacterGrid(
                character = displayCharacters.firstOrNull() ?: "",
                gridSize = gridSize.toPx(),
                settings = settings,
                textMeasurer = textMeasurer,
                density = density
            )
        } else {
            drawMultiCharacterGrid(
                characters = displayCharacters,
                gridSize = gridSize.toPx(),
                settings = settings,
                textMeasurer = textMeasurer,
                maxCharsPerRow = currentLayoutInfo.maxCharsPerRow,
                density = density
            )
        }

        // 繪製用戶筆跡
        val stroke = Stroke(
            width = settings.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // 繪製已完成的路徑
        drawingState.paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = stroke
            )
        }

        // 繪製正在畫的路徑
        drawingState.getActivePaths().forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = stroke
            )
        }
    }
}

/**
 * 繪製單字模式的格子
 */
private fun DrawScope.drawSingleCharacterGrid(
    character: String,
    gridSize: Float,
    settings: PracticeSettings,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val gridLeft = centerX - gridSize / 2
    val gridTop = centerY - gridSize / 2

    // 繪製外框
    drawRect(
        color = Color.Black,
        topLeft = Offset(gridLeft, gridTop),
        size = Size(gridSize, gridSize),
        style = Stroke(width = 2.dp.toPx())
    )

    // 繪製格線
    drawGridLines(
        left = gridLeft,
        top = gridTop,
        size = gridSize,
        style = settings.gridStyle
    )

    // 繪製底字（淡灰色）
    if (character.isNotEmpty()) {
        val textStyle = TextStyle(
            fontSize = (gridSize * 0.6f / density.density).sp,
            fontFamily = FontFamily.Serif,
            color = Color.Gray.copy(alpha = 0.3f)
        )

        val textLayoutResult = textMeasurer.measure(character, textStyle)
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        drawText(
            textMeasurer = textMeasurer,
            text = character,
            style = textStyle,
            topLeft = Offset(
                centerX - textWidth / 2,
                centerY - textHeight / 2
            )
        )
    }
}

/**
 * 繪製多字模式的格子
 */
private fun DrawScope.drawMultiCharacterGrid(
    characters: List<String>,
    gridSize: Float,
    settings: PracticeSettings,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    maxCharsPerRow: Int,
    density: androidx.compose.ui.unit.Density
) {
    val spacing = 20.dp.toPx()
    val totalWidth = maxCharsPerRow * gridSize + (maxCharsPerRow - 1) * spacing
    val startX = (size.width - totalWidth) / 2
    val startY = 50.dp.toPx()

    characters.forEachIndexed { index, character ->
        val row = index / maxCharsPerRow
        val col = index % maxCharsPerRow

        val left = startX + col * (gridSize + spacing)
        val top = startY + row * (gridSize + spacing)

        // 繪製外框
        drawRect(
            color = Color.Black,
            topLeft = Offset(left, top),
            size = Size(gridSize, gridSize),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 繪製格線
        drawGridLines(
            left = left,
            top = top,
            size = gridSize,
            style = settings.gridStyle
        )

        // 繪製底字
        if (character.isNotEmpty()) {
            val textStyle = TextStyle(
                fontSize = (gridSize * 0.5f / density.density).sp,
                fontFamily = FontFamily.Serif,
                color = Color.Gray.copy(alpha = 0.25f)
            )

            val textLayoutResult = textMeasurer.measure(character, textStyle)
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            drawText(
                textMeasurer = textMeasurer,
                text = character,
                style = textStyle,
                topLeft = Offset(
                    left + gridSize / 2 - textWidth / 2,
                    top + gridSize / 2 - textHeight / 2
                )
            )
        }
    }
}

/**
 * 繪製格線（米字格或九宮格）
 */
private fun DrawScope.drawGridLines(
    left: Float,
    top: Float,
    size: Float,
    style: GridStyle
) {
    val strokeStyle = Stroke(width = 0.5.dp.toPx())
    val lineColor = Color.Gray.copy(alpha = 0.4f)

    when (style) {
        GridStyle.RICE_GRID -> {
            // 米字格：十字線 + 對角線
            // 垂直中線
            drawLine(
                color = lineColor,
                start = Offset(left + size / 2, top),
                end = Offset(left + size / 2, top + size),
                strokeWidth = strokeStyle.width
            )
            // 水平中線
            drawLine(
                color = lineColor,
                start = Offset(left, top + size / 2),
                end = Offset(left + size, top + size / 2),
                strokeWidth = strokeStyle.width
            )
            // 左上到右下對角線
            drawLine(
                color = lineColor,
                start = Offset(left, top),
                end = Offset(left + size, top + size),
                strokeWidth = strokeStyle.width
            )
            // 右上到左下對角線
            drawLine(
                color = lineColor,
                start = Offset(left + size, top),
                end = Offset(left, top + size),
                strokeWidth = strokeStyle.width
            )
        }
        GridStyle.NINE_GRID -> {
            // 九宮格：3x3網格
            // 垂直線
            drawLine(
                color = lineColor,
                start = Offset(left + size / 3, top),
                end = Offset(left + size / 3, top + size),
                strokeWidth = strokeStyle.width
            )
            drawLine(
                color = lineColor,
                start = Offset(left + size * 2 / 3, top),
                end = Offset(left + size * 2 / 3, top + size),
                strokeWidth = strokeStyle.width
            )
            // 水平線
            drawLine(
                color = lineColor,
                start = Offset(left, top + size / 3),
                end = Offset(left + size, top + size / 3),
                strokeWidth = strokeStyle.width
            )
            drawLine(
                color = lineColor,
                start = Offset(left, top + size * 2 / 3),
                end = Offset(left + size, top + size * 2 / 3),
                strokeWidth = strokeStyle.width
            )
        }
    }
}
