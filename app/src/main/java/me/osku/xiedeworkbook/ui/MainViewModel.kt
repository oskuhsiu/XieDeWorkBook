package me.osku.xiedeworkbook.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.osku.xiedeworkbook.data.*

/**
 * 主要的ViewModel，管理練習簿App的狀態
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    // 設定狀態
    var practiceSettings by mutableStateOf(PracticeSettings())
        private set

    // 練習簿相關狀態
    var practiceBooks by mutableStateOf<List<PracticeBook>>(emptyList())
        private set

    var currentBook by mutableStateOf<PracticeBook?>(null)
        private set

    var currentPage by mutableStateOf(0)
        private set

    // UI 狀態
    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    var isSettingsVisible by mutableStateOf(false)
        private set

    var manualInputText by mutableStateOf("")
        private set

    init {
        // 載入設定和練習簿
        loadSettings()
        loadPracticeBooks()
        initializeBuiltInBooks()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.practiceSettings.collect { settings ->
                practiceSettings = settings
            }
        }
    }

    private fun loadPracticeBooks() {
        viewModelScope.launch {
            settingsRepository.practiceBooks.collect { books ->
                practiceBooks = books
            }
        }
    }

    private fun initializeBuiltInBooks() {
        viewModelScope.launch {
            val existingBooks = settingsRepository.practiceBooks.firstOrNull() ?: emptyList()
            if (existingBooks.none { it.isBuiltIn }) {
                val builtInBooks = listOf(
                    PracticeBook(
                        name = "常用字1000",
                        content = "的一是在不了有和人這中大為上個國我以要他時來用們生到作地於出就分對成會可主發年動同工也能下過子說產種面而方後多定行學法所民本者立向世路級進長少年光從把先然如此心件於前表文問現代小民水理什天手日平想電話內數量達走向使風雨",
                        isBuiltIn = true
                    ),
                    PracticeBook(
                        name = "三字經",
                        content = "人之初性本善性相近習相遠苟不教性乃遷教之道貴以專昔孟母擇鄰處子不學斷機杼竇燕山有義方教五子名俱揚養不教父之過教不嚴師之惰���不學非所宜幼不學老何為玉不琢不成器人不學不知義為人子方少時親師友習禮儀",
                        isBuiltIn = true
                    ),
                    PracticeBook(
                        name = "小學生字表",
                        content = "一二三四五六七八九十百千萬上下左右前後東西南北大小多少長短高低快慢好壞新舊",
                        isBuiltIn = true
                    )
                )
                val allBooks = existingBooks + builtInBooks
                savePracticeBooks(allBooks)
            }
        }
    }

    // 導航相關方法
    fun navigateToScreen(screen: Screen) {
        currentScreen = screen
    }

    fun navigateBack() {
        when (currentScreen) {
            Screen.CONTENT_SELECTION -> currentScreen = Screen.HOME
            Screen.PRACTICE -> currentScreen = Screen.CONTENT_SELECTION
            Screen.MY_BOOKS -> currentScreen = Screen.CONTENT_SELECTION
            else -> currentScreen = Screen.HOME
        }
    }

    // 練習相關方法
    fun startPractice(book: PracticeBook) {
        currentBook = book
        currentPage = 0
        currentScreen = Screen.PRACTICE
    }

    fun startManualPractice(text: String) {
        if (text.isNotBlank()) {
            val book = PracticeBook(
                name = "手動輸入",
                content = text,
                isBuiltIn = false
            )
            startPractice(book)
        }
    }

    fun nextPage(charsPerPage: Int? = null) {
        currentBook?.let { book ->
            val actualCharsPerPage = charsPerPage ?: run {
                // 回退到估算邏輯
                if (practiceSettings.isSingleCharMode) {
                    1
                } else {
                    when {
                        practiceSettings.multiCharGridSize <= 80f -> 20
                        practiceSettings.multiCharGridSize <= 120f -> 12
                        else -> 6
                    }
                }
            }

            val maxPages = if (practiceSettings.isSingleCharMode) {
                book.characters.size
            } else {
                (book.characters.size + actualCharsPerPage - 1) / actualCharsPerPage
            }

            if (currentPage < maxPages - 1) {
                currentPage++
            }
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
        }
    }

    // 設定相關方法
    fun showSettings() {
        isSettingsVisible = true
    }

    fun hideSettings() {
        isSettingsVisible = false
    }

    fun updateSettings(newSettings: PracticeSettings) {
        practiceSettings = newSettings
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    // 練習簿管理方法
    fun updateManualInputText(text: String) {
        manualInputText = text
    }

    fun saveCustomBook(name: String, content: String) {
        if (name.isNotBlank() && content.isNotBlank()) {
            val newBook = PracticeBook(
                name = name,
                content = content,
                isBuiltIn = false
            )
            val updatedBooks = practiceBooks + newBook
            savePracticeBooks(updatedBooks)
        }
    }

    fun deleteBook(book: PracticeBook) {
        if (!book.isBuiltIn) {
            val updatedBooks = practiceBooks.filter { it.id != book.id }
            savePracticeBooks(updatedBooks)
        }
    }

    private fun savePracticeBooks(books: List<PracticeBook>) {
        viewModelScope.launch {
            settingsRepository.savePracticeBooks(books)
        }
    }

    // 取得當前頁面資訊
    val currentPageInfo: String
        get() {
            return currentBook?.let { book ->
                val maxPages = if (practiceSettings.isSingleCharMode) {
                    book.characters.size
                } else {
                    // 多字模式：需要動態計算每頁字數
                    // 由於無法在 ViewModel 中獲取螢幕尺寸，這裡提供一個估算
                    // 實際的頁數將由 UI 層動態調整
                    val estimatedCharsPerPage = when {
                        practiceSettings.multiCharGridSize <= 80f -> 20 // 小格子可以放更多
                        practiceSettings.multiCharGridSize <= 120f -> 12 // 中等格子
                        else -> 6 // 大格子放較少
                    }
                    (book.characters.size + estimatedCharsPerPage - 1) / estimatedCharsPerPage
                }
                "${currentPage + 1}/$maxPages"
            } ?: "0/0"
        }

    // 動態計算最大頁數的輔助方法
    fun calculateMaxPages(charsPerPage: Int): Int {
        return currentBook?.let { book ->
            if (practiceSettings.isSingleCharMode) {
                book.characters.size
            } else {
                (book.characters.size + charsPerPage - 1) / charsPerPage
            }
        } ?: 0
    }
}

/**
 * 頁面枚舉
 */
enum class Screen {
    HOME,
    CONTENT_SELECTION,
    PRACTICE,
    MY_BOOKS
}
