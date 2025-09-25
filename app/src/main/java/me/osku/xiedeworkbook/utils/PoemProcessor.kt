package me.osku.xiedeworkbook.utils

import android.content.Context
import me.osku.xiedeworkbook.data.Poem
import me.osku.xiedeworkbook.data.PoetryBook
import java.io.IOException

/**
 * 詩詞處理器 - 負責掃描、加載和解析詩詞書籍
 */
object PoemProcessor {

    private const val POETRY_BOOKS_DIR = "poetry_books"
    private const val POEM_SEPARATOR = "\n\n" // 詩篇分隔符

    /**
     * 掃描並初始化所有詩詞書籍（懶加載模式）
     */
    fun loadPoetryBooksList(context: Context): List<PoetryBook> {
        val books = mutableListOf<PoetryBook>()

        try {
            val assetManager = context.assets
            val files = assetManager.list(POETRY_BOOKS_DIR)

            files?.forEach { fileName ->
                if (fileName.endsWith(".txt")) {
                    val bookTitle = fileName.removeSuffix(".txt")
                    books.add(
                        PoetryBook(
                            bookTitle = bookTitle,
                            fileName = fileName,
                            poemList = emptyList(), // 懶加載：初始為空
                            isLoaded = false
                        )
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return books
    }

    /**
     * 加載指定書籍的所有詩篇內容
     */
    fun loadBookContent(context: Context, book: PoetryBook): PoetryBook {
        if (book.isLoaded) return book

        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("$POETRY_BOOKS_DIR/${book.fileName}")
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

            val poems = parsePoems(content)

            return book.copy(
                poemList = poems,
                isLoaded = true
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return book
        }
    }

    /**
     * 解析詩詞文件內容，分割成獨立的詩篇
     */
    private fun parsePoems(content: String): List<Poem> {
        val poems = mutableListOf<Poem>()
        val poemBlocks = content.split(POEM_SEPARATOR)

        poemBlocks.forEach { block ->
            val trimmedBlock = block.trim()
            if (trimmedBlock.isNotEmpty()) {
                val poem = parseSinglePoem(trimmedBlock)
                if (poem != null) {
                    poems.add(poem)
                }
            }
        }

        return poems
    }

    /**
     * 解析單首詩詞
     * 格式：第一行為標題，第二行為作者，其餘為內容
     */
    private fun parseSinglePoem(block: String): Poem? {
        val lines = block.split("\n").filter { it.isNotBlank() }

        if (lines.size < 3) return null // 至少需要標題、作者、內容

        val title = lines[0].trim()
        val author = lines[1].trim()
        val content = lines.drop(2).joinToString("\n") // 保留原始換行符

        // 使用TextProcessor處理內容，得到句組列表
        val sentences = TextProcessor.preprocessText(content)

        return Poem(
            title = title,
            author = author,
            content = content,
            sentences = sentences
        )
    }

    /**
     * 從指定書籍中隨機選取一首詩
     */
    fun getRandomPoem(book: PoetryBook): Poem? {
        if (book.poemList.isEmpty()) return null
        return book.poemList.random()
    }

    /**
     * 根據標題或作者搜索詩詞
     */
    fun searchPoems(book: PoetryBook, query: String): List<Poem> {
        if (query.isBlank()) return book.poemList

        val lowerQuery = query.lowercase()
        return book.poemList.filter { poem ->
            poem.title.lowercase().contains(lowerQuery) ||
            poem.author.lowercase().contains(lowerQuery)
        }
    }
}
