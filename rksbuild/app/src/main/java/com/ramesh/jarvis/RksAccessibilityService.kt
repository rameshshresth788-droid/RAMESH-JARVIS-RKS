package com.ramesh.jarvis
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
class RksAccessibilityService: AccessibilityService(){ override fun onAccessibilityEvent(event:AccessibilityEvent?){}; override fun onInterrupt(){} }
