package com.FMDAP.pulsepoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.FMDAP.pulsepoint.ui.navigation.NavGraph
import com.FMDAP.pulsepoint.ui.theme.PulsePointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulsePointTheme {
                NavGraph()
            }
        }
    }
}
