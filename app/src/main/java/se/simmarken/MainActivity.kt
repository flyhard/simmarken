package se.simmarken

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import se.simmarken.navigation.SimmarkenNavHost
import se.simmarken.ui.theme.SimmarkenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleImportIntent(intent)
        setContent {
            SimmarkenTheme {
                SimmarkenNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent?) {
        val uri = extractImportUri(intent) ?: return
        val app = application as SimmarkenApplication
        app.container.pendingImportUri.value = uri
    }

    private fun extractImportUri(intent: Intent?): Uri? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW, Intent.ACTION_SEND -> {
                when {
                    intent.type?.contains("json") == true -> intent.data
                        ?: intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    intent.type?.startsWith("text/") == true -> intent.data
                        ?: intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    else -> null
                }
            }
            else -> null
        }
    }
}
