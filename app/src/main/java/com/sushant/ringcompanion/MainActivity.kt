package com.sushant.ringcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sushant.ringcompanion.ui.RingCompanionApp
import com.sushant.ringcompanion.ui.theme.RingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RingTheme {
                RingCompanionApp()
            }
        }
    }
}
