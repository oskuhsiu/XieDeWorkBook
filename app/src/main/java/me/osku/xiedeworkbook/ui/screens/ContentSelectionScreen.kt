package me.osku.xiedeworkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.osku.xiedeworkbook.data.PracticeBook
import me.osku.xiedeworkbook.ui.MainViewModel

/**
 * 內容選擇頁面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSelectionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("手動輸入", "我的練習簿", "內建練習簿")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 頂部欄
        TopAppBar(
            title = {
                Text(
                    text = "選擇練習內容",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )

        // Tab 選擇器
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        // 內容區域
        when (selectedTab) {
            0 -> ManualInputTab(viewModel)
            1 -> MyBooksTab(viewModel)
            2 -> BuiltInBooksTab(viewModel)
        }
    }
}

/**
 * 手動輸入標籤頁
 */
@Composable
private fun ManualInputTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "輸入或貼上要練習的文字",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = viewModel.manualInputText,
            onValueChange = viewModel::updateManualInputText,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = {
                Text("例如：春眠不覺曉，處處聞啼鳥...")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (viewModel.manualInputText.isNotBlank()) {
                    viewModel.startManualPractice(viewModel.manualInputText)
                }
            },
            enabled = viewModel.manualInputText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "開始練習",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // 字數統計
        if (viewModel.manualInputText.isNotBlank()) {
            Text(
                text = "共 ${viewModel.manualInputText.length} 個字",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 我的練習簿標籤頁
 */
@Composable
private fun MyBooksTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userBooks = viewModel.practiceBooks.filter { !it.isBuiltIn }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (userBooks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "尚未建立自訂練習簿",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "可在手動輸入頁面儲存常用的練習內容",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userBooks) { book ->
                    PracticeBookCard(
                        book = book,
                        onStartPractice = { viewModel.startPractice(book) },
                        onDelete = { viewModel.deleteBook(book) },
                        showDeleteButton = true
                    )
                }
            }
        }
    }
}

/**
 * 內建練習簿標籤頁
 */
@Composable
private fun BuiltInBooksTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val builtInBooks = viewModel.practiceBooks.filter { it.isBuiltIn }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(builtInBooks) { book ->
            PracticeBookCard(
                book = book,
                onStartPractice = { viewModel.startPractice(book) },
                onDelete = null,
                showDeleteButton = false
            )
        }
    }
}

/**
 * 練習簿卡片組件
 */
@Composable
private fun PracticeBookCard(
    book: PracticeBook,
    onStartPractice: () -> Unit,
    onDelete: (() -> Unit)?,
    showDeleteButton: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "共 ${book.characters.size} 個字",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showDeleteButton && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("刪除")
                    }
                }
            }

            // 預覽前幾個字
            Text(
                text = book.content.take(20) + if (book.content.length > 20) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onStartPractice,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("開始練習")
            }
        }
    }
}
