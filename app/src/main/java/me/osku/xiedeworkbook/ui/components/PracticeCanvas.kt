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
                        // 多點觸控：每個觸控點都處理
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
            val bottomMargin = 50.dp.toPx()
            val sideMargin = 16.dp.toPx()

            // 計算實際可用空間
            val availableWidth = size.width - (sideMargin * 2)
            val availableHeight = size.height - topMargin - bottomMargin

            // 計算每個格子的實際寬度（包含注音格子）
            val zhuyinWidth = if (settings.showZhuyin) gridSizePx * 0.25f else 0f
            val gridWithZhuyinWidth = gridSizePx + zhuyinWidth

            // 對於中文直式排列：先計算列數（垂直方向），再計算行數（水平方向）
            val maxRows = if (availableHeight > 0) {
                ((availableHeight + spacing) / (gridSizePx + spacing)).toInt().coerceAtLeast(1)
            } else 1

            val maxCols = if (availableWidth > 0) {
                ((availableWidth + spacing) / (gridWithZhuyinWidth + spacing)).toInt().coerceAtLeast(1)
            } else 1

            val charsPerPage = maxRows * maxCols

            // 注意：這裡的 maxCharsPerRow 實際上是 maxCharsPerCol（每列最大字數）
            LayoutInfo(maxRows, maxCols, charsPerPage)
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

    // 計算注音格子的寬度（主格子的25-30%）
    val zhuyinWidth = if (settings.showZhuyin) gridSize * 0.28f else 0f
    val totalWidth = gridSize + zhuyinWidth

    // 檢查總寬度是否超出螢幕，如果超出則縮小主格子
    val maxAvailableWidth = size.width * 0.9f // 預留10%邊距
    val actualGridSize = if (totalWidth > maxAvailableWidth && settings.showZhuyin) {
        // 重新計算主格子大小，確保總寬度不超出螢幕
        val adjustedMainGridSize = maxAvailableWidth / 1.28f // 1.28 = 1 + 0.28 (注音格比例)
        adjustedMainGridSize
    } else {
        gridSize
    }

    // 重新計算注音格子寬度
    val actualZhuyinWidth = if (settings.showZhuyin) actualGridSize * 0.28f else 0f
    val actualTotalWidth = actualGridSize + actualZhuyinWidth

    // 調整主格子的位置，讓整體居中
    val mainGridLeft = centerX - actualTotalWidth / 2
    val mainGridTop = centerY - actualGridSize / 2
    val zhuyinGridLeft = mainGridLeft + actualGridSize

    // 繪製主格子外框
    drawRect(
        color = Color.Black,
        topLeft = Offset(mainGridLeft, mainGridTop),
        size = Size(actualGridSize, actualGridSize),
        style = Stroke(width = 2.dp.toPx())
    )

    // 繪製主格子格線
    drawGridLines(
        left = mainGridLeft,
        top = mainGridTop,
        size = actualGridSize,
        style = settings.gridStyle
    )

    // 繪製注音格子（如果啟用且寬度有效）
    if (settings.showZhuyin && character.isNotEmpty() && actualZhuyinWidth > 0) {
        // 繪製注音格子外框
        drawRect(
            color = Color.Black,
            topLeft = Offset(zhuyinGridLeft, mainGridTop),
            size = Size(actualZhuyinWidth, actualGridSize),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 繪製注音
        drawZhuyinInGrid(
            character = character.firstOrNull() ?: ' ',
            left = zhuyinGridLeft,
            top = mainGridTop,
            width = actualZhuyinWidth,
            height = actualGridSize,
            textMeasurer = textMeasurer,
            density = density
        )
    }

    // 繪製底字（淡灰色）
    if (character.isNotEmpty()) {
        val textStyle = TextStyle(
            fontSize = (actualGridSize * 0.7f / density.density).sp,
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
                mainGridLeft + actualGridSize / 2 - textWidth / 2,
                mainGridTop + actualGridSize / 2 - textHeight / 2
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
    maxCharsPerRow: Int, // 這裡實際上是 maxRows（每列最大字數）
    density: androidx.compose.ui.unit.Density
) {
    val spacing = 20.dp.toPx()
    // 根據新的佈局邏輯重新計算
    val maxRows = maxCharsPerRow // 每列最大字數
    val maxCols = if (characters.isNotEmpty()) {
        (characters.size + maxRows - 1) / maxRows // 需要的列數
    } else 1

    // 計算注音格子寬度（如果啟用注音）
    val zhuyinWidth = if (settings.showZhuyin) gridSize * 0.25f else 0f
    val gridWithZhuyinWidth = gridSize + zhuyinWidth

    val totalWidth = maxCols * gridWithZhuyinWidth + (maxCols - 1) * spacing
    val startX = (size.width - totalWidth) / 2
    val startY = 50.dp.toPx()

    characters.forEachIndexed { index, character ->
        // 中文直式排列：先填滿第一列，再填第二列
        val col = index / maxRows  // 第幾列
        val row = index % maxRows  // 列內第幾個

        val left = startX + col * (gridWithZhuyinWidth + spacing)
        val top = startY + row * (gridSize + spacing)

        // 繪製主格子外框
        drawRect(
            color = Color.Black,
            topLeft = Offset(left, top),
            size = Size(gridSize, gridSize),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 繪製主格子格線
        drawGridLines(
            left = left,
            top = top,
            size = gridSize,
            style = settings.gridStyle
        )

        // 繪製注音格子（如果啟用且字符不為空）
        if (settings.showZhuyin && character.isNotEmpty() && zhuyinWidth > 0) {
            val zhuyinLeft = left + gridSize

            // 繪製注音格子外框
            drawRect(
                color = Color.Black,
                topLeft = Offset(zhuyinLeft, top),
                size = Size(zhuyinWidth, gridSize),
                style = Stroke(width = 1.dp.toPx())
            )

            // 繪製注音
            drawZhuyinInGrid(
                character = character.firstOrNull() ?: ' ',
                left = zhuyinLeft,
                top = top,
                width = zhuyinWidth,
                height = gridSize,
                textMeasurer = textMeasurer,
                density = density
            )
        }

        // 繪製底字
        if (character.isNotEmpty()) {
            val textStyle = TextStyle(
                fontSize = (gridSize * 0.7f / density.density).sp,
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

/**
 * 在注音格子中繪製注音符號
 */
private fun DrawScope.drawZhuyinInGrid(
    character: Char,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    // 檢查格子尺寸是否有效
    if (width <= 0 || height <= 0) return

    // 獲取注音
    val zhuyin = me.osku.xiedeworkbook.utils.ZhuyinDict.getZhuyin(character)
    if (zhuyin.isEmpty()) return

    // 解析注音組件
    val zhuyinComponents = me.osku.xiedeworkbook.utils.parseZhuyinWithTones(zhuyin)
    if (zhuyinComponents.isEmpty()) return

    // 計算字體大小，根據格子尺寸調整
    val baseFontSize = minOf(width, height) * .8f // 使用較小的尺寸作為基準
    val fontSize = maxOf(baseFontSize, 8f) // 最小字體大小為8

    // 計算注音的垂直佈局
    val maxY = zhuyinComponents.maxOfOrNull { it.y } ?: 0f
    val minY = zhuyinComponents.minOfOrNull { it.y } ?: 0f
    val totalHeight = (maxY - minY + 1) * fontSize * 1.2f
    val startY = top + (height - totalHeight) / 2 + fontSize

    // 計算水平居中位置
    val centerX = left + width / 2

    // 繪製每個注音組件
    zhuyinComponents.forEach { component ->
        val xPosition = centerX + component.x * fontSize * 0.3f // 減小水平偏移
        val yPosition = startY + (component.y - minY) * fontSize * 1.2f

        // 確保位置在格子範圍內
        if (xPosition < left || xPosition > left + width ||
            yPosition < top || yPosition > top + height
        ) {
            return@forEach
        }

        val actualFontSize = /*if (component.isTone) fontSize * 0.8f else*/ fontSize
        val textStyle = TextStyle(
            fontSize = (actualFontSize / density.density).sp,
            fontFamily = FontFamily.Default,
            color = Color.Black.copy(alpha = 0.8f)
        )

        // 測量文字尺寸，確保不會超出格子範圍
        val textLayoutResult = textMeasurer.measure(component.char.toString(), textStyle)
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        // 檢查文字尺寸是否有效
        if (textWidth <= 0 || textHeight <= 0) return@forEach

        // 計算最終繪製位置，確保文字完全在格子內
        val maxX = maxOf(left, left + width - textWidth)
        val maxY = maxOf(top, top + height - textHeight)

        val finalX = (xPosition - textWidth / 2).coerceIn(left, maxX)
        val finalY = (yPosition - textHeight / 2).coerceIn(top, maxY)

        // 再次檢查最終位置是否有效
        if (finalX < left || finalY < top ||
            finalX + textWidth > left + width ||
            finalY + textHeight > top + height
        ) {
            return@forEach
        }

        drawText(
            textMeasurer = textMeasurer,
            text = component.char.toString(),
            style = textStyle,
            topLeft = Offset(finalX, finalY)
        )
    }
}
