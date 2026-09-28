package com.ramesh.jarvis
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.TextView
import android.graphics.Color
class OverlayService:Service(){private var wm:WindowManager?=null;private var v:View?=null
 override fun onCreate(){super.onCreate();if(!android.provider.Settings.canDrawOverlays(this)){stopSelf();return};wm=getSystemService(WINDOW_SERVICE) as WindowManager;val t=TextView(this);t.text="◉";t.textSize=28f;t.setTextColor(Color.WHITE);t.gravity=17;t.setBackgroundColor(Color.TRANSPARENT);val type=if(android.os.Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE;val lp=WindowManager.LayoutParams(72,72,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP or Gravity.END;lp.x=18;lp.y=120;v=t;wm!!.addView(t,lp)}
 override fun onDestroy(){v?.let{wm?.removeView(it)};super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null}
