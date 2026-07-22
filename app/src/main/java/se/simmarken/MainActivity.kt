package se.simmarken

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
        setContent {
            SimmarkenTheme {
                SimmarkenNavHost()
            }
        }
    }
}
