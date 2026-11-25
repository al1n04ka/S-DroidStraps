package com.sdroidstraps.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sdroidstraps.launcher.ui.App
import com.sdroidstraps.launcher.ui.theme.SDroidStrapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SDroidStrapsTheme {
                App()
            }
        }
    }
}
