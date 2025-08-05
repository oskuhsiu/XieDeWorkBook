package me.osku.xiedeworkbook.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.osku.xiedeworkbook.data.*
import me.osku.xiedeworkbook.R
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private fun initializeBuiltInBooks() {
        viewModelScope.launch {
            val existingBooks = settingsRepository.practiceBooks.firstOrNull() ?: emptyList()
            if (existingBooks.none { it.isBuiltIn }) {
                val builtInBooks = listOf(
                    PracticeBook(
                        name = "常用字",
                        content = "的一是在不了有和這中大為上個國我以要他時來用們生到作地於出就分對成會可主發年動同工也能下過子說產種面而方後多定行學法所民本者立向世路級進長少年光從把先然如此心件於前表文問現代小民水理什天手日平想電話內數量達走向使風雨",
                        isBuiltIn = true,
                    ),
                    PracticeBook(
                        name = "常用字1000",
                        content = "的不一我是人有了大國來生在子們中上他時小地出以學可自這會成家到為天心年然要得說過個著能下動發臺麼車那行經去好開現就作後多方如事公看也長面力起裡高用業你因而分市於道外沒無同法前水電民對兒日之文當教新意情所實工全定美理頭本明氣進樣都主間老想重體山物知手相回性果政只此代和活媽親化加影什身己灣機部常見其正世北女花合場海者表問立西還最感告度光點色種少風資她期利保友樂關品書金師產但觀平太名空變安聲路從爸口很真許些又母門孩展像目信今應特十東入話原內把病白星員提總愛畫走片眼比統由界受使朋通球別已覺廣計結接選打先才認候望神何處放做題向位收運三建數解流形量再兩清馬童式直傳難管院快造記必視設次引音報滿術命社月演交司住育聽商反幾邊父至舉醫系治給任轉導辦喜園土遠價科怎四非區歡完往二南容服近調字指象校林未程改求始帶失制精務更每紅陸笑輕吃節強單石古深房整專論等投考斯陽功遊推語念英研樹張創環型便興速義落達戰質示思濟營讀縣費客賽持華火青線格河熱識共故離決王呢腦連團步卻取布消男黃言類死包亞際士眾黨照且早飛及參究寫易備基景算半各角件府雖夫響爭細或軍叫足議苦食第香德技元史城紀它條境排與藝標該斷集器養企態切助夜準雨根聯終除權乎領錢案驗勢奇黑並百規顯具萬存底將圖晚歷詩低拉座春約希假預官似獨增即坐木鄉首找刻爾需草久五戲室局絕確續率效油留克例溫衣里班洲證嚴組況裝值極亮味令須廠弟港護願哥腳料則銀查習製雄健訴注痛血狀登越族龍省級葉跟劇蘭異趣魚職請居源隨優李配舞險破壓防股歌列農般福密嗎緊謝充皮玩策維麗旅傷吸禮印超被試談背支甚銷館息千財雙靜曾漸買否送升站係微修巴寶致汽急素驚野供良適積牛招八阿懷洋課答獎屋唱模羅藥跑綠盡怕待夠突初康差減燈票復臉施較益獲講讓冷負善簡止害構錯酒另慢輪拿操岸稱層靈筆隊雲富據倒智飯樓竟依雜仍委散紙庭坡毒顧停評聞賣檢毛款責店擔幫船歐牙限臨婦擎迷板換沙歲典群午彩採段免競吧六田錄察衛永周隻舊句乾追戶夏執波穿堂礙忙幼織威缺革恐盤卡沉忽妹劃討繼休染逐鐵怪貨悲號牠婚寒志江激擊竹勞遺夢圍窗降街幕呼熟鏡睡忘村九派殺尋菜拍附鳥稅置島烈退移療徵跳宋勝曲探訪植伯朝協訊畢浪普餐剛洗短補啊控迎耳左牌警尼略塊掉圓材掌右帝搖京陳測肉售雪秋救賞亂潮皇湖守射雞貴煙露避藏宜額七抗享鼓屬購頂疑宮睛冰透障困慶宣旁洞巨趕姐若律症陰順階擇承幸歸豐鐘遇端擁默橋盛編廳託州訓敗翻危婆抱骨僅範壞茶麻紛棒奶誰私鬆娘堅博淡嘴季納尤肯隆介玉倫惡游彈練蟲陣揮",
                        isBuiltIn = true,
                        canRandomize = true
                    ),
                    PracticeBook(
                        name = "三字經",
                        content = "人之初性本善性相近習相遠苟不教性乃遷教之道貴以專昔孟母擇鄰處子不學斷機杼竇燕山有義方教五子名俱揚養不教父之過教不嚴師之惰子不學非所宜幼不學老何為玉不琢不成器人不學不知義為人子方少時親師友習禮儀香九齡能溫席孝於親所當執融四歲能讓梨弟於長宜先知首孝悌次見聞知某數識某文一而十十而百百而千千而萬三才者天地人三光者日月星三綱者君臣義父子親夫婦順曰春夏曰秋冬此四時運不窮曰南北曰西東此四方應乎中曰水火木金土此五行本乎數",
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

    private fun initializeZhuyinDict() {
        viewModelScope.launch {
            me.osku.xiedeworkbook.utils.ZhuyinDict.load(getApplication())
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

    /**
     * 從word4k.tsv隨機選取50個字
     */
    fun generateRandomCharacters() {
        viewModelScope.launch {
            try {
                val randomChars = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().resources.openRawResource(R.raw.word4k)
                    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    val allCharacters = mutableSetOf<String>()

                    reader.forEachLine { line ->
                        val parts = line.trim().split('\t')
                        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                            allCharacters.add(parts[0])
                        }
                    }
                    reader.close()

                    // 隨機選取50個字，如果不足50個就取全部
                    val shuffled = allCharacters.toList().shuffled()
                    shuffled.take(50).joinToString("")
                }

                manualInputText = randomChars
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果出錯，使用預設的隨機字符
                manualInputText = "春眠不覺曉處處聞啼鳥夜來風雨聲花落知多少靜夜思床前明月光疑是地上霜舉頭望明月低頭思故鄉"
            }
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
