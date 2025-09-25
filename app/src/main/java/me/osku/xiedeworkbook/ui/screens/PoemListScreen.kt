package me.osku.xiedeworkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.osku.xiedeworkbook.data.Poem
import me.osku.xiedeworkbook.data.PoetryBook
import me.osku.xiedeworkbook.ui.MainViewModel

/**
 * B. 篇目列表畫面 (Poem List View)
 * 顯示選定書籍中的所有詩篇
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoemListScreen(
    viewModel: MainViewModel,
    book: PoetryBook,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // 根據搜索查詢過濾詩篇
    val filteredPoems = remember(book.poemList, searchQuery) {
        if (searchQuery.isBlank()) {
            book.poemList
        } else {
            val query = searchQuery.lowercase()
            book.poemList.filter { poem ->
                poem.title.lowercase().contains(query) ||
                poem.author.lowercase().contains(query)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 頂部欄
        TopAppBar(
            title = {
                Text(
                    text = book.bookTitle,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回書庫"
                    )
                }
            },
            actions = {
                // 隨機一首按鈕
                IconButton(
                    onClick = { viewModel.showRandomPoem(book) },
                    enabled = book.poemList.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "隨機一首"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜尋欄
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索詩詞標題或作者...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除搜索"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 隨機按鈕（大號）
            Button(
                onClick = { viewModel.showRandomPoem(book) },
                enabled = book.poemList.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "隨機一首",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 詩篇列表
            if (filteredPoems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isBlank()) Icons.Default.DateRange else Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "此書暫無內容" else "找不到符合條件的詩詞",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPoems) { poem ->
                        PoemCard(
                            poem = poem,
                            onClick = { viewModel.selectPoem(poem) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 詩詞卡片組件
 */
@Composable
private fun PoemCard(
    poem: Poem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = poem.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = poem.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 內容預覽（前30個字符）
            val preview = poem.content.replace("\n", " ").take(30)
            Text(
                text = if (poem.content.length > 30) "$preview..." else preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
