package me.osku.xiedeworkbook.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * 設定儲存管理類
 */
class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("practice_settings", Context.MODE_PRIVATE)

    private val _practiceSettings = MutableStateFlow(loadSettings())
    val practiceSettings: Flow<PracticeSettings> = _practiceSettings.asStateFlow()

    private val _practiceBooks = MutableStateFlow(loadPracticeBooks())
    val practiceBooks: Flow<List<PracticeBook>> = _practiceBooks.asStateFlow()

    private fun loadSettings(): PracticeSettings {
        return PracticeSettings(
            isFingerMode = prefs.getBoolean("is_finger_mode", true),
            isSingleCharMode = prefs.getBoolean("is_single_char_mode", true),
            strokeWidth = prefs.getFloat("stroke_width", if (prefs.getBoolean("is_finger_mode", true)) 8f else 3f),
            singleCharGridSize = prefs.getFloat("single_char_grid_size", if (prefs.getBoolean("is_finger_mode", true)) 300f else 200f),
            multiCharGridSize = prefs.getFloat("multi_char_grid_size", if (prefs.getBoolean("is_finger_mode", true)) 150f else 100f),
            fontType = prefs.getString("font_type", "KaiTi") ?: "KaiTi",
            gridStyle = try {
                GridStyle.valueOf(prefs.getString("grid_style", GridStyle.RICE_GRID.name) ?: GridStyle.RICE_GRID.name)
            } catch (e: Exception) {
                GridStyle.RICE_GRID
            }
        )
    }

    suspend fun updateSettings(settings: PracticeSettings) {
        prefs.edit().apply {
            putBoolean("is_finger_mode", settings.isFingerMode)
            putBoolean("is_single_char_mode", settings.isSingleCharMode)
            putFloat("stroke_width", settings.strokeWidth)
            putFloat("single_char_grid_size", settings.singleCharGridSize)
            putFloat("multi_char_grid_size", settings.multiCharGridSize)
            putString("font_type", settings.fontType)
            putString("grid_style", settings.gridStyle.name)
            apply()
        }
        _practiceSettings.value = settings
    }

    private fun loadPracticeBooks(): List<PracticeBook> {
        val json = prefs.getString("practice_books", "[]") ?: "[]"
        return try {
            Json.decodeFromString<List<PracticeBook>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun savePracticeBooks(books: List<PracticeBook>) {
        val json = Json.encodeToString(books)
        prefs.edit().putString("practice_books", json).apply()
        _practiceBooks.value = books
    }
}
