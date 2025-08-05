package me.osku.xiedeworkbook.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.osku.xiedeworkbook.data.GridStyle
import me.osku.xiedeworkbook.data.PracticeSettings
import me.osku.xiedeworkbook.ui.MainViewModel
import kotlin.math.roundToInt

/**
 * 設定畫面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSettings by remember { mutableStateOf(viewModel.practiceSettings) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 頂部欄
        TopAppBar(
            title = {
                Text(
                    text = "設定",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "關閉"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.updateSettings(tempSettings)
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("完成")
                }
            }
        )

        // 設定內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 練習模式設定
            SettingsSection(title = "練習模式") {
                // 輸入方式選擇
                SettingItem(title = "輸入方式") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tempSettings.isFingerMode,
                            onClick = {
                                tempSettings = tempSettings.copy(
                                    isFingerMode = true,
                                    strokeWidth = if (tempSettings.isFingerMode) tempSettings.strokeWidth else 8f,
                                    singleCharGridSize = if (tempSettings.isFingerMode) tempSettings.singleCharGridSize else 300f,
                                    multiCharGridSize = if (tempSettings.isFingerMode) tempSettings.multiCharGridSize else 150f
                                )
                            },
                            label = { Text("手指模式") }
                        )
                        FilterChip(
                            selected = !tempSettings.isFingerMode,
                            onClick = {
                                tempSettings = tempSettings.copy(
                                    isFingerMode = false,
                                    strokeWidth = if (!tempSettings.isFingerMode) tempSettings.strokeWidth else 3f,
                                    singleCharGridSize = if (!tempSettings.isFingerMode) tempSettings.singleCharGridSize else 200f,
                                    multiCharGridSize = if (!tempSettings.isFingerMode) tempSettings.multiCharGridSize else 100f
                                )
                            },
                            label = { Text("觸控筆模式") }
                        )
                    }
                }

                // 顯示模式選擇
                SettingItem(title = "顯示模式") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tempSettings.isSingleCharMode,
                            onClick = { tempSettings = tempSettings.copy(isSingleCharMode = true) },
                            label = { Text("單字模式") }
                        )
                        FilterChip(
                            selected = !tempSettings.isSingleCharMode,
                            onClick = { tempSettings = tempSettings.copy(isSingleCharMode = false) },
                            label = { Text("多字模式") }
                        )
                    }
                }
            }

            // 外觀設定
            SettingsSection(title = "外觀設定") {
                // 筆跡寬度
                SettingItem(title = "筆跡寬度") {
                    SliderWithPreview(
                        value = tempSettings.strokeWidth,
                        onValueChange = { tempSettings = tempSettings.copy(strokeWidth = it) },
                        valueRange = 1f..15f,
                        steps = 13,
                        previewType = PreviewType.STROKE
                    )
                }

                // 單字模式格子大小
                SettingItem(title = "單字模式格子大小") {
                    SliderWithPreview(
                        value = tempSettings.singleCharGridSize,
                        onValueChange = { tempSettings = tempSettings.copy(singleCharGridSize = it) },
                        valueRange = 150f..400f,
                        steps = 24,
                        previewType = PreviewType.SINGLE_GRID,
                        gridStyle = tempSettings.gridStyle
                    )
                }

                // 多字模式格子大小
                SettingItem(title = "多字模式格子大小") {
                    SliderWithPreview(
                        value = tempSettings.multiCharGridSize,
                        onValueChange = { tempSettings = tempSettings.copy(multiCharGridSize = it) },
                        valueRange = 80f..200f,
                        steps = 23,
                        previewType = PreviewType.MULTI_GRID,
                        gridStyle = tempSettings.gridStyle
                    )
                }

                // 格線樣式
                SettingItem(title = "格線樣式") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tempSettings.gridStyle == GridStyle.RICE_GRID,
                            onClick = { tempSettings = tempSettings.copy(gridStyle = GridStyle.RICE_GRID) },
                            label = { Text("米字格") }
                        )
                        FilterChip(
                            selected = tempSettings.gridStyle == GridStyle.NINE_GRID,
                            onClick = { tempSettings = tempSettings.copy(gridStyle = GridStyle.NINE_GRID) },
                            label = { Text("九宮格") }
                        )
                    }
                }

                // 字型選擇
                SettingItem(title = "預設字型") {
                    var expanded by remember { mutableStateOf(false) }
                    val fontOptions = listOf("KaiTi", "SimSun", "FangSong", "LiSu")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = tempSettings.fontType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            fontOptions.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font) },
                                    onClick = {
                                        tempSettings = tempSettings.copy(fontType = font)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 設定分組組件
 */
@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

/**
 * 設定項目組件
 */
@Composable
private fun SettingItem(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
        content()
    }
}

/**
 * 帶預覽的滑桿組件
 */
@Composable
private fun SliderWithPreview(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    previewType: PreviewType,
    gridStyle: GridStyle = GridStyle.RICE_GRID,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 滑桿
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps
            )
            Text(
                text = when (previewType) {
                    PreviewType.STROKE -> "${value.roundToInt()}px"
                    PreviewType.SINGLE_GRID, PreviewType.MULTI_GRID -> "${value.roundToInt()}dp"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 預覽
        PreviewBox(
            value = value,
            type = previewType,
            gridStyle = gridStyle,
            modifier = Modifier.size(60.dp)
        )
    }
}

/**
 * 預覽框組件
 */
@Composable
private fun PreviewBox(
    value: Float,
    type: PreviewType,
    gridStyle: GridStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            when (type) {
                PreviewType.STROKE -> {
                    // 預覽筆觸寬度
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.7f)
                        quadraticBezierTo(
                            size.width * 0.5f, size.height * 0.3f,
                            size.width * 0.8f, size.height * 0.7f
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(
                            width = value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
                PreviewType.SINGLE_GRID, PreviewType.MULTI_GRID -> {
                    // 預覽格子大小
                    val gridSizeInPx = with(density) { value.dp.toPx() }
                    val scaleFactor = minOf(size.width, size.height) / gridSizeInPx * 0.8f
                    val actualSize = gridSizeInPx * scaleFactor
                    val left = (size.width - actualSize) / 2
                    val top = (size.height - actualSize) / 2

                    // 繪製外框
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(actualSize, actualSize),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 繪製格線
                    drawGridLines(left, top, actualSize, gridStyle)
                }
            }
        }
    }
}

/**
 * 繪製格線輔助函數
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridLines(
    left: Float,
    top: Float,
    size: Float,
    style: GridStyle
) {
    val strokeStyle = Stroke(width = 0.5.dp.toPx())
    val lineColor = Color.Gray.copy(alpha = 0.4f)

    when (style) {
        GridStyle.RICE_GRID -> {
            // 米字格
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
            // 對角線
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
            // 九宮格
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
 * 預覽類型枚舉
 */
private enum class PreviewType {
    STROKE,
    SINGLE_GRID,
    MULTI_GRID
}
