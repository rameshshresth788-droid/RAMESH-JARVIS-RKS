package com.ramesh.jarvis

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import java.util.Locale

class MainActivity: ComponentActivity(){
 private lateinit var store:JarvisConfigStore
 private val mic=registerForActivityResult(ActivityResultContracts.RequestPermission()){ }
 override fun onCreate(b:Bundle?){super.onCreate(b);store=JarvisConfigStore(this);setContent{App()}}
 private fun startRks(){ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){mic.launch(Manifest.permission.RECORD_AUDIO);return}; val i=Intent(this,JarvisService::class.java);if(Build.VERSION.SDK_INT>=26)startForegroundService(i) else startService(i); if(store.load().floatingLogo && Settings.canDrawOverlays(this)) startService(Intent(this,OverlayService::class.java)) }
 private fun overlaySettings(){startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))}
 @Composable fun App(){var cfg by remember{mutableStateOf(store.load())};var settings by remember{mutableStateOf(false)};var active by remember{mutableStateOf(false)}
  MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFFFFD45A),background=Color.Black,surface=Color(0xFF0B0A07))){Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF3A2A0A),Color(0xFF0A0906),Color.Black))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("RKS JARVIS",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color(0xFFFFE7A1));Spacer(Modifier.height(30.dp));Core(active){active=true;startRks()};Spacer(Modifier.height(24.dp));Text(if(active)"RKS ACTIVE • ALWAYS READY" else "TAP TO ACTIVATE",color=Color(0xFFFFF2C2),letterSpacing=3.sp);Spacer(Modifier.height(30.dp));Button({settings=true}){Text("SETTINGS")}}
   if(settings)SettingsDialog(cfg,{settings=false}){cfg=it;store.save(it);settings=false}
  }} }
 @Composable fun Core(active:Boolean,onClick:()->Unit){val inf=rememberInfiniteTransition(label="rks");val p by inf.animateFloat(.92f,1.08f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="p");Box(Modifier.size(190.dp).scale(if(active)p else 1f).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFFFF6D0).copy(.5f),Color(0xFFFFC928).copy(.15f),Color.Transparent))).border(2.dp,Color(0xFFFFD45A),CircleShape).clickable{onClick()},contentAlignment=Alignment.Center){Text("◉",fontSize=70.sp,color=Color.White)}}
 @Composable fun SettingsDialog(initial:JarvisConfig,onDismiss:()->Unit,onSave:(JarvisConfig)->Unit){var provider by remember{mutableStateOf(initial.provider)};var key by remember{mutableStateOf(initial.apiKey)};var prompt by remember{mutableStateOf(initial.systemPrompt)};var wake by remember{mutableStateOf(initial.wakeWord)};var always by remember{mutableStateOf(initial.alwaysReady)};var float by remember{mutableStateOf(initial.floatingLogo)};val p=runCatching{AiProvider.valueOf(provider)}.getOrDefault(AiProvider.GEMINI)
  AlertDialog(onDismissRequest=onDismiss,title={Text("RKS SETTINGS")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("AI PROVIDER",color=Color(0xFFFFD45A));AiProvider.values().forEach{RadioButtonRow(it.title,it.name==provider){provider=it.name}};OutlinedTextField(key,{key=it},label={Text("API Key")},singleLine=true);Text("Endpoint: ${p.endpoint}",fontSize=10.sp);Text("Model: ${p.model}",fontSize=10.sp);OutlinedTextField(wake,{wake=it},label={Text("Wake word")},singleLine=true);Row(verticalAlignment=Alignment.CenterVertically){Checkbox(always,{always=it});Text("Always-ready voice service")};Row(verticalAlignment=Alignment.CenterVertically){Checkbox(float,{float=it});Text("Floating RKS logo")};OutlinedTextField(prompt,{prompt=it},label={Text("System prompt")},minLines=3);Button({startPermissionScreen()}){Text("SYSTEM PERMISSIONS")};Button({overlaySettings()}){Text("ALLOW FLOATING WINDOW")}},confirmButton={TextButton({onSave(JarvisConfig(provider,p.endpoint,key,p.model,prompt,wake,always,float))}){Text("SAVE")}},dismissButton={TextButton(onDismiss){Text("CANCEL")}})}
 @Composable fun RadioButtonRow(s:String,checked:Boolean,click:()->Unit){Row(Modifier.fillMaxWidth().clickable{click()},verticalAlignment=Alignment.CenterVertically){RadioButton(checked,click);Text(s)}}
 private fun startPermissionScreen(){if(Build.VERSION.SDK_INT>=23)startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
}
