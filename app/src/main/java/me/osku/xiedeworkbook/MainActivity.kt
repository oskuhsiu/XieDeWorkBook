package me.osku.xiedeworkbook

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import me.osku.xiedeworkbook.ui.MainViewModel
import me.osku.xiedeworkbook.ui.Screen
import me.osku.xiedeworkbook.ui.screens.*
import me.osku.xiedeworkbook.ui.theme.XieDeWorkBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        enableEdgeToEdge()
        setContent {
            XieDeWorkBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    XieDeWorkBookApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setFullScreen()
    }

    private fun setFullScreen() {
        // 設定全螢幕，隱藏狀態列與導覽列
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        } catch (e: Exception) {
            // 如果全螢幕設定失敗，記錄錯誤但不影響 App 正常運行
            e.printStackTrace()
        }
    }
}

@Composable
fun XieDeWorkBookApp() {
    val viewModel: MainViewModel = viewModel()

    // 根據當前螢幕顯示相應的UI
    when (viewModel.currentScreen) {
        Screen.HOME -> {
            HomeScreen(viewModel = viewModel)
        }
        Screen.CONTENT_SELECTION -> {
            ContentSelectionScreen(viewModel = viewModel)
        }
        Screen.PRACTICE -> {
            PracticeScreen(viewModel = viewModel)
        }
        Screen.MY_BOOKS -> {
            ContentSelectionScreen(viewModel = viewModel, selectedTab = 1)
        }
    }

    // 設定畫面覆蓋層
    if (viewModel.isSettingsVisible) {
        SettingsScreen(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSettings() }
        )
    }
}