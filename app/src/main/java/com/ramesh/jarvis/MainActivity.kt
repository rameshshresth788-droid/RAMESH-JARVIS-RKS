package com.ramesh.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var store: JarvisConfigStore

    private val mic =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = JarvisConfigStore(this)

        setContent {
            App()
        }
    }

    private fun startRks() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            mic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        val intent = Intent(this, JarvisService::class.java)

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        if (
            store.load().floatingLogo &&
            Settings.canDrawOverlays(this)
        ) {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    private fun overlaySettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    @Composable
    fun App() {
        var cfg by remember {
            mutableStateOf(store.load())
        }

        var settings by remember {
            mutableStateOf(false)
        }

        var active by remember {
            mutableStateOf(false)
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFFFD45A),
                background = Color.Black,
                surface = Color(0xFF0B0A07)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF3A2A0A),
                                Color(0xFF0A0906),
                                Color.Black
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "RKS JARVIS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE7A1)
                    )

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )

                    Core(
                        active = active,
                        onClick = {
                            active = true
                            startRks()
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = if (active) {
                            "RKS ACTIVE • ALWAYS READY"
                        } else {
                            "TAP TO ACTIVATE"
                        },
                        color = Color(0xFFFFF2C2),
                        letterSpacing = 3.sp
                    )

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )

                    Button(
                        onClick = {
                            settings = true
                        }
                    ) {
                        Text("SETTINGS")
                    }
                }

                if (settings) {
                    SettingsDialog(
                        initial = cfg,
                        onDismiss = {
                            settings = false
                        },
                        onSave = {
                            cfg = it
                            store.save(it)
                            settings = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun Core(
        active: Boolean,
        onClick: () -> Unit
    ) {
        val transition =
            rememberInfiniteTransition(label = "rks")

        val pulse by transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(if (active) pulse else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFF6D0).copy(alpha = 0.5f),
                            Color(0xFFFFC928).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFFFD45A),
                    shape = CircleShape
                )
                .clickable {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◉",
                fontSize = 70.sp,
                color = Color.White
            )
        }
    }

    @Composable
    fun SettingsDialog(
        initial: JarvisConfig,
        onDismiss: () -> Unit,
        onSave: (JarvisConfig) -> Unit
    ) {
        var provider by remember {
            mutableStateOf(initial.provider)
        }

        var key by remember {
            mutableStateOf(initial.apiKey)
        }

        var prompt by remember {
            mutableStateOf(initial.systemPrompt)
        }

        var wake by remember {
            mutableStateOf(initial.wakeWord)
        }

        var always by remember {
            mutableStateOf(initial.alwaysReady)
        }

        var floating by remember {
            mutableStateOf(initial.floatingLogo)
        }

        val selectedProvider =
            runCatching {
                AiProvider.valueOf(provider)
            }.getOrDefault(AiProvider.GEMINI)

        AlertDialog(
            onDismissRequest = onDismiss,

            title = {
                Text("RKS SETTINGS")
            },

            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "AI PROVIDER",
                        color = Color(0xFFFFD45A)
                    )

                    AiProvider.values().forEach { item ->
                        RadioButtonRow(
                            text = item.title,
                            checked = item.name == provider,
                            onClick = {
                                provider = item.name
                            }
                        )
                    }

                    OutlinedTextField(
                        value = key,
                        onValueChange = {
                            key = it
                        },
                        label = {
                            Text("API Key")
                        },
                        singleLine = true
                    )

                    Text(
                        text = "Endpoint: ${selectedProvider.endpoint}",
                        fontSize = 10.sp
                    )

                    Text(
                        text = "Model: ${selectedProvider.model}",
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = wake,
                        onValueChange = {
                            wake = it
                        },
                        label = {
                            Text("Wake word")
                        },
                        singleLine = true
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = always,
                            onCheckedChange = {
                                always = it
                            }
                        )

                        Text("Always-ready voice service")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = floating,
                            onCheckedChange = {
                                floating = it
                            }
                        )

                        Text("Floating RKS logo")
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = {
                            prompt = it
                        },
                        label = {
                            Text("System prompt")
                        },
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            startPermissionScreen()
                        }
                    ) {
                        Text("SYSTEM PERMISSIONS")
                    }

                    Button(
                        onClick = {
                            overlaySettings()
                        }
                    ) {
                        Text("ALLOW FLOATING WINDOW")
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(
                            JarvisConfig(
                                provider = provider,
                                endpoint = selectedProvider.endpoint,
                                apiKey = key,
                                model = selectedProvider.model,
                                systemPrompt = prompt,
                                wakeWord = wake,
                                alwaysReady = always,
                                floatingLogo = floating
                            )
                        )
                    }
                ) {
                    Text("SAVE")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    @Composable
    fun RadioButtonRow(
        text: String,
        checked: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = checked,
                onClick = onClick
            )

            Text(text)
        }
    }

    private fun startPermissionScreen() {
        if (Build.VERSION.SDK_INT >= 23) {
            startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            )
        }
    }
}
