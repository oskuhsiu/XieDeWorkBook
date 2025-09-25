package me.osku.xiedeworkbook.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.osku.xiedeworkbook.data.*
import me.osku.xiedeworkbook.utils.PoemProcessor
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 主要的ViewModel，管理練習簿App的狀態（修復詩詞模式問題）
 */
class MainViewModel_fixed(application: Application) : AndroidViewModel(application) {

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

    // 詩詞簿相關狀態
    var poetryBooks by mutableStateOf<List<PoetryBook>>(emptyList())
        private set

    var currentPoetryBook by mutableStateOf<PoetryBook?>(null)
        private set

    var currentPoem by mutableStateOf<Poem?>(null)
        private set

    var currentPoetryMode by mutableStateOf<PoetryMode?>(null)
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
        loadPoetryBooks()
        initializeBuiltInBooks()
        // 初始化注音字典
        initializeZhuyinDict()
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

    private fun loadPoetryBooks() {
        viewModelScope.launch {
            try {
                val books = withContext(Dispatchers.IO) {
                    PoemProcessor.loadPoetryBooksList(getApplication())
                }
                poetryBooks = books
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initializeBuiltInBooks() {
        viewModelScope.launch {
            val existingBooks = settingsRepository.practiceBooks.firstOrNull() ?: emptyList()
            if (existingBooks.none { it.isBuiltIn }) {
                val context = getApplication<Application>()
                val builtInBooks = listOf(
                    PracticeBook(
                        name = "三字經",
                        content = "人之初性本善性相近習相遠苟不教性乃遷教之道貴以專昔孟母擇鄰處子不學斷機杼竇燕山有義方教五子名俱揚養不教父之過教不嚴師之惰子不學非所宜幼不學老何為玉不琢不成器人不學不知義為人子方少時親師友習禮儀香九齡能溫席孝於親所當執融四歲能讓梨弟於長宜先知首孝悌次見聞知某數識某文一而十十而百百而千千而萬三才者天地人三光者日月星三綱者君臣義父子親夫婦順曰春夏曰秋冬此四時運不窮曰南北曰西東此四方應乎中曰水火木金土此五行本乎數",
                        isBuiltIn = true
                    ),
                    PracticeBook(
                        name = "常用字1000",
                        content = readAssetsTextFile(context, "basic_words_1000.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "常用字1至6畫",
                        content = readAssetsTextFile(context, "basic_words_01_to_06_strokes.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "常用字6至10畫",
                        content = readAssetsTextFile(context, "basic_words_06_to_10_strokes.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "常用字10至15畫",
                        content = readAssetsTextFile(context, "basic_words_10_to_15_strokes.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "常用字16至20畫",
                        content = readAssetsTextFile(context, "basic_words_16_to_20_strokes.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "常用字21畫以上",
                        content = readAssetsTextFile(context, "basic_words_over_21_strokes.txt"),
                        isBuiltIn = true,
                        canRandomize = true
                    )
                )
                val allBooks = existingBooks + builtInBooks
                savePracticeBooks(allBooks)
            }
        }
    }

    // 修改輔助方法：從assets讀取文字檔
    private fun readAssetsTextFile(context: Application, fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun initializeZhuyinDict() {
        viewModelScope.launch {
            me.osku.xiedeworkbook.utils.ZhuyinDict.load(getApplication())
        }
    }

    // 詩詞簿相關方法
    fun selectPoetryBook(book: PoetryBook) {
        viewModelScope.launch {
            try {
                val loadedBook = if (book.isLoaded) {
                    book
                } else {
                    withContext(Dispatchers.IO) {
                        PoemProcessor.loadBookContent(getApplication(), book)
                    }
                }

                // 更新書庫中的書籍狀態
                poetryBooks = poetryBooks.map {
                    if (it.fileName == book.fileName) loadedBook else it
                }

                currentPoetryBook = loadedBook
                currentScreen = Screen.POEM_LIST
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectPoem(poem: Poem) {
        currentPoem = poem
        currentScreen = Screen.POEM_DETAIL
    }

    fun showRandomPoem(book: PoetryBook) {
        val randomPoem = PoemProcessor.getRandomPoem(book)
        if (randomPoem != null) {
            currentPoem = randomPoem
            currentPoetryMode = PoetryMode.APPRECIATION
            currentScreen = Screen.POETRY_APPRECIATION
        }
    }

    fun startPoetryMode(poem: Poem, mode: PoetryMode) {
        currentPoem = poem
        currentPoetryMode = mode
        when (mode) {
            PoetryMode.APPRECIATION -> {
                currentScreen = Screen.POETRY_APPRECIATION
            }
            PoetryMode.PRACTICE -> {
                // 將詩詞轉換為練習簿格式
                val practiceContent = poem.sentences.joinToString("")
                startManualPractice(practiceContent)
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
            Screen.POETRY_BOOKSHELF -> currentScreen = Screen.HOME
            Screen.POEM_LIST -> currentScreen = Screen.POETRY_BOOKSHELF
            Screen.POEM_DETAIL -> currentScreen = Screen.POEM_LIST
            Screen.POETRY_APPRECIATION -> {
                // 如果是從隨機詩詞進入，返回到詩詞列表
                if (currentPoetryBook != null) {
                    currentScreen = Screen.POEM_LIST
                } else {
                    currentScreen = Screen.POETRY_BOOKSHELF
                }
            }
            else -> currentScreen = Screen.HOME
        }
    }

    // 練習相關方法
    fun startPractice(book: PracticeBook) {
        currentBook = book
        currentPage = 0
        currentScreen = Screen.PRACTICE
    }

    // 修復：確保詩詞練習正確設置自動斷句支持
    fun startManualPractice(text: String) {
        if (text.isNotBlank()) {
            // 預處理文本並檢查是否支持自動斷句
            val processedSentences = me.osku.xiedeworkbook.utils.TextProcessor.preprocessText(text)
            val supportsAutoSentence = me.osku.xiedeworkbook.utils.TextProcessor.canUseAutoSentenceMode(text)

            val book = PracticeBook(
                name = "手動輸入",
                content = text,
                isBuiltIn = false,
                sentences = processedSentences,
                supportsAutoSentence = supportsAutoSentence
            )
            startPractice(book)
        }
    }

    fun nextPage(charsPerPage: Int? = null) {
        currentBook?.let { book ->
            val actualCharsPerPage = charsPerPage ?: run {
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

    fun generateRandomCharacters() {
        viewModelScope.launch {
            try {
                val randomChars = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().assets.open("word4k.tsv")
                    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    val allCharacters = mutableSetOf<String>()

                    reader.forEachLine { line ->
                        val parts = line.trim().split('\t')
                        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                            allCharacters.add(parts[0])
                        }
                    }
                    reader.close()

                    val shuffled = allCharacters.toList().shuffled()
                    shuffled.take(50).joinToString("")
                }

                manualInputText = randomChars
            } catch (e: Exception) {
                e.printStackTrace()
                manualInputText =
                    "春眠不覺曉處處聞啼鳥夜來風雨聲花落知多少靜夜思床前明月光疑是地上霜舉頭望明月低頭思故鄉"
            }
        }
    }

    val currentPageInfo: String
        get() {
            return currentBook?.let { book ->
                val maxPages = if (practiceSettings.isSingleCharMode) {
                    book.characters.size
                } else {
                    val estimatedCharsPerPage = when {
                        practiceSettings.multiCharGridSize <= 80f -> 20
                        practiceSettings.multiCharGridSize <= 120f -> 12
                        else -> 6
                    }
                    (book.characters.size + estimatedCharsPerPage - 1) / estimatedCharsPerPage
                }
                "${currentPage + 1}/$maxPages"
            } ?: "0/0"
        }

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
enum class Screen_fixed {
    HOME,
    CONTENT_SELECTION,
    PRACTICE,
    MY_BOOKS,
    POETRY_BOOKSHELF,    // 詩詞書庫
    POEM_LIST,           // 篇目列表
    POEM_DETAIL,         // 詩詞詳情
    POETRY_APPRECIATION  // 詩詞賞析
}
