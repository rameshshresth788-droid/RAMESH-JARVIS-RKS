package com.ramesh.jarvis

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisService : Service(), TextToSpeech.OnInitListener {
    private lateinit var speech: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var store: JarvisConfigStore
    private var listening = false
    private var active = false
    private var lastWake = 0L

    override fun onCreate() {
        super.onCreate(); store = JarvisConfigStore(this)
        tts = TextToSpeech(this, this)
        createChannel()
        startForeground(44, Notification.Builder(this, "rks_voice").setContentTitle("RKS JARVIS active").setContentText("Voice assistant is ready").setSmallIcon(android.R.drawable.ic_btn_speak_now).build())
        if (SpeechRecognizer.isRecognitionAvailable(this)) speech = SpeechRecognizer.createSpeechRecognizer(this)
        startListening()
    }
    private fun createChannel() { if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("rks_voice","RKS Voice Agent",NotificationManager.IMPORTANCE_LOW)) }
    private fun startListening() {
        if (!::speech.isInitialized || listening) return
        listening = true
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false) }
        speech.setRecognitionListener(object: RecognitionListener {
            override fun onResults(b: Bundle?) { listening=false; val text=b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); handle(text); Handler(Looper.getMainLooper()).postDelayed({ startListening() },500) }
            override fun onError(e:Int){ listening=false; Handler(Looper.getMainLooper()).postDelayed({ startListening() },800) }
            override fun onReadyForSpeech(p:Bundle?){}; override fun onBeginningOfSpeech(){}; override fun onRmsChanged(v:Float){}; override fun onBufferReceived(b:ByteArray?){}; override fun onEndOfSpeech(){}; override fun onPartialResults(b:Bundle?){}; override fun onEvent(t:Int,b:Bundle?){ }
        }); speech.startListening(i)
    }
    private fun handle(raw:String) {
        val c=store.load(); val text=raw.trim(); if(text.isBlank()) return
        val wake=c.wakeWord.lowercase(Locale.getDefault()); val lower=text.lowercase(Locale.getDefault())
        if(!active && !lower.contains(wake)) return
        if(!active){ active=true; say("Yes Master, I’m active."); val command = text.drop(wake.length).trim(); if(command.isNotBlank()) execute(command); return }
        execute(text)
    }
    private fun execute(command:String) {
        val l=command.lowercase(Locale.getDefault())
        if(l.startsWith("open ") || l.startsWith("launch ")) { val name=command.substringAfter(' ').trim(); val pm=packageManager; val pkgs=pm.getInstalledApplications(PackageManager.GET_META_DATA); val hit=pkgs.firstOrNull{pm.getApplicationLabel(it).toString().equals(name,true)} ?: pkgs.firstOrNull{pm.getApplicationLabel(it).toString().contains(name,true)}; if(hit!=null){startActivity(pm.getLaunchIntentForPackage(hit.packageName)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); say("Opening $name");return} }
        if(l=="stop"||l=="go to sleep"||l=="standby"){active=false;say("Going to standby, Master.");return}
        // AI call can be wired through the same client without blocking the listener.
        Thread { val answer=AIClient.ask(store.load(), listOf("user" to command)); Handler(Looper.getMainLooper()).post{say(answer)} }.start()
    }
    private fun say(s:String){ if(::tts.isInitialized) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"rks") }
    override fun onInit(status:Int){ if(status==TextToSpeech.SUCCESS) tts.language=Locale.getDefault() }
    override fun onStartCommand(i:Intent?,f:Int,id:Int)=START_STICKY
    override fun onDestroy(){ if(::speech.isInitialized)speech.destroy(); if(::tts.isInitialized){tts.stop();tts.shutdown()};super.onDestroy() }
    override fun onBind(i:Intent?)=null
}
