package com.ramesh.jarvis
import android.content.*
class BootReceiver: BroadcastReceiver(){ override fun onReceive(c:Context,i:Intent){ if(i.action==Intent.ACTION_BOOT_COMPLETED){ val s=Intent(c,JarvisService::class.java); if(android.os.Build.VERSION.SDK_INT>=26)c.startForegroundService(s) else c.startService(s) } } }
