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
import me.osku.xiedeworkbook.utils.TextProcessor
import me.osku.xiedeworkbook.utils.SentenceLayout

/**
 * 自動斷句模式專用畫布
 * 根據MD規格實現：每頁只顯示一個句組，依句長自動排版
 */
@Composable
fun AutoSentenceCanvas(
    modifier: Modifier = Modifier,
    sentence: String,
    settings: PracticeSettings,
    drawingState: DrawingState,
    onDrawingInteraction: () -> Unit = {}
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // 計算句組的排版信息
    val sentenceLayout = remember(sentence) {
        TextProcessor.calculateSentenceLayout(sentence)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        // 多點觸控處理
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
        // 繪製自動斷句佈局
        drawAutoSentenceLayout(
            sentence = sentence,
            layout = sentenceLayout,
            settings = settings,
            textMeasurer = textMeasurer,
            density = density
        )

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
 * 繪製自動斷句佈局
 * 實現MD規格：直書排版，依句長自動分欄
 */
private fun DrawScope.drawAutoSentenceLayout(
    sentence: String,
    layout: SentenceLayout,
    settings: PracticeSettings,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    if (sentence.isEmpty() || layout.columnCount == 0) return

    // 計算自適應格子大小
    val availableWidth = size.width * 0.8f // 預留20%邊距
    val availableHeight = size.height * 0.8f // 預留20%邊距

    // 根據句組長度和佈局計算格子大小
    val maxColumnsWidth = layout.columnCount
    val maxRowsHeight = layout.maxCharsPerColumn

    val gridWidth = minOf(availableWidth / maxColumnsWidth, availableHeight / maxRowsHeight)
    val gridSize = maxOf(gridWidth, 80f) // 最小80px

    // 計算注音格寬度
    val zhuyinWidth = if (settings.showZhuyin) gridSize * 0.25f else 0f
    val gridWithZhuyinWidth = gridSize + zhuyinWidth

    // 計算起始位置（居中顯示）
    val totalWidth = layout.columnCount * gridWithZhuyinWidth
    val totalHeight = layout.maxCharsPerColumn * gridSize
    val startX = (size.width - totalWidth) / 2
    val startY = (size.height - totalHeight) / 2

    // 依中文直書習慣：從右到左排列各欄
    var charIndex = 0
    for (colIndex in 0 until layout.columnCount) {
        val columnSize = layout.columnSizes[colIndex]
        val colX = startX + (layout.columnCount - 1 - colIndex) * gridWithZhuyinWidth // 從右到左

        // 繪製該欄的字符格子
        for (rowIndex in 0 until columnSize) {
            if (charIndex < sentence.length) {
                val char = sentence[charIndex].toString()
                val rowY = startY + rowIndex * gridSize

                // 繪製主格子
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(colX, rowY),
                    size = Size(gridSize, gridSize),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 繪製格線
                drawGridLines(colX, rowY, gridSize, settings.gridStyle)

                // 繪製注音格（如果啟用）
                if (settings.showZhuyin && zhuyinWidth > 0) {
                    val zhuyinX = colX + gridSize
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(zhuyinX, rowY),
                        size = Size(zhuyinWidth, gridSize),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 繪製注音符號
                    drawZhuyinInGrid(
                        character = char.firstOrNull() ?: ' ',
                        left = zhuyinX,
                        top = rowY,
                        width = zhuyinWidth,
                        height = gridSize,
                        textMeasurer = textMeasurer,
                        density = density
                    )
                }

                // 繪製底字（淡灰色）
                val textStyle = TextStyle(
                    fontSize = (gridSize * 0.7f / density.density).sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                val textLayoutResult = textMeasurer.measure(char, textStyle)
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                drawText(
                    textMeasurer = textMeasurer,
                    text = char,
                    style = textStyle,
                    topLeft = Offset(
                        colX + gridSize / 2 - textWidth / 2,
                        rowY + gridSize / 2 - textHeight / 2
                    )
                )

                charIndex++
            }
        }
    }
}

/**
 * 繪製格線（復用PracticeCanvas的邏輯）
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
            drawLine(
                color = lineColor,
                start = Offset(left + size / 2, top),
                end = Offset(left + size / 2, top + size),
                strokeWidth = strokeStyle.width
            )
            drawLine(
                color = lineColor,
                start = Offset(left, top + size / 2),
                end = Offset(left + size, top + size / 2),
                strokeWidth = strokeStyle.width
            )
            drawLine(
                color = lineColor,
                start = Offset(left, top),
                end = Offset(left + size, top + size),
                strokeWidth = strokeStyle.width
            )
            drawLine(
                color = lineColor,
                start = Offset(left + size, top),
                end = Offset(left, top + size),
                strokeWidth = strokeStyle.width
            )
        }
        GridStyle.NINE_GRID -> {
            // 九宮格：3x3網格
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
 * 在注音格中繪製注音符號（復用PracticeCanvas的邏輯）
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
    if (width <= 0 || height <= 0) return

    val zhuyin = me.osku.xiedeworkbook.utils.ZhuyinDict.getZhuyin(character)
    if (zhuyin.isEmpty()) return

    val zhuyinComponents = me.osku.xiedeworkbook.utils.parseZhuyinWithTones(zhuyin)
    if (zhuyinComponents.isEmpty()) return

    val baseFontSize = minOf(width, height) * .8f
    val fontSize = maxOf(baseFontSize, 8f)

    val maxY = zhuyinComponents.maxOfOrNull { it.y } ?: 0f
    val minY = zhuyinComponents.minOfOrNull { it.y } ?: 0f
    val totalHeight = (maxY - minY + 1) * fontSize * 1.2f
    val startY = top + (height - totalHeight) / 2 + fontSize

    val centerX = left + width / 2

    zhuyinComponents.forEach { component ->
        val xPosition = centerX + component.x * fontSize * 0.3f
        val yPosition = startY + (component.y - minY) * fontSize * 1.2f

        if (xPosition < left || xPosition > left + width ||
            yPosition < top || yPosition > top + height) {
            return@forEach
        }

        val actualFontSize = fontSize
        val textStyle = TextStyle(
            fontSize = (actualFontSize / density.density).sp,
            fontFamily = FontFamily.Default,
            color = Color.Black.copy(alpha = 0.8f)
        )

        val textLayoutResult = textMeasurer.measure(component.char.toString(), textStyle)
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        if (textWidth <= 0 || textHeight <= 0) return@forEach

        val maxX = maxOf(left, left + width - textWidth)
        val maxY = maxOf(top, top + height - textHeight)

        val finalX = (xPosition - textWidth / 2).coerceIn(left, maxX)
        val finalY = (yPosition - textHeight / 2).coerceIn(top, maxY)

        if (finalX < left || finalY < top ||
            finalX + textWidth > left + width ||
            finalY + textHeight > top + height) {
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
