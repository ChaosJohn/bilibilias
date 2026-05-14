import com.imcys.bilibilias.common.utils.AsRegexUtil

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.imcys.bilibilias.datastore.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

private const val CLIPBOARD_READ_DELAY_MS = 180L

/**
 * 处理剪贴板自动识别
 */
@Composable
fun ClipboardAutoHandler(
    appSettings: AppSettings,
    shouldHandleClipboard: () -> Boolean = { true },
    onClipboardText: (String) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val enabledState by rememberUpdatedState(appSettings.enabledClipboardAutoHandling)
    val shouldHandleState by rememberUpdatedState(shouldHandleClipboard)
    val onClipboardTextState by rememberUpdatedState(onClipboardText)

    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus && enabledState && shouldHandleState()) {
                coroutineScope.launch {
                    Log.d("TAG", "ClipboardAutoHandler: WINDOW_FOCUS_GAINED")
                    delay(CLIPBOARD_READ_DELAY_MS)
                    val text = context.consumeClipboardText()
                    if (!text.isNullOrEmpty()) {
                        onClipboardTextState(text)
                    }
                }
            }
        }

        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }
}

private fun Context.consumeClipboardText(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    val clip = clipboard.primaryClip ?: return null
    val text = clip.getItemAt(0)
        .coerceToText(this)
        ?.toString()
        ?.trim()
        .takeIf { !it.isNullOrEmpty() }

    if (text.isNullOrBlank() || AsRegexUtil.parse(text) == null) {
        return null
    }
    // 清空，避免重复处理
    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    return text
}