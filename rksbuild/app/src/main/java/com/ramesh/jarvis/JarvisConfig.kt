package com.ramesh.jarvis

import android.content.Context

enum class AiProvider(val title: String, val endpoint: String, val model: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", "gemini-2.5-flash"),
    OPENAI("ChatGPT / OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "openai/gpt-4o-mini")
}

data class JarvisConfig(
    val provider: String,
    val endpoint: String,
    val apiKey: String,
    val model: String,
    val systemPrompt: String,
    val wakeWord: String,
    val alwaysReady: Boolean,
    val floatingLogo: Boolean
)

class JarvisConfigStore(context: Context) {
    private val secure = SecureStore(context)
    fun load() = JarvisConfig(
        secure.get("provider", AiProvider.GEMINI.name),
        secure.get("endpoint", AiProvider.GEMINI.endpoint),
        secure.get("apiKey"),
        secure.get("model", AiProvider.GEMINI.model),
        secure.get("systemPrompt", "You are RAMESH JARVIS, a private personal Android voice assistant. Understand Hindi, English and Hinglish. Be concise and natural. Never claim an action was completed unless it actually was."),
        secure.get("wakeWord", "RKS"),
        secure.get("alwaysReady", "true") == "true",
        secure.get("floatingLogo", "true") == "true"
    )
    fun save(c: JarvisConfig) {
        secure.put("provider", c.provider); secure.put("endpoint", c.endpoint); secure.put("apiKey", c.apiKey)
        secure.put("model", c.model); secure.put("systemPrompt", c.systemPrompt); secure.put("wakeWord", c.wakeWord)
        secure.put("alwaysReady", c.alwaysReady.toString()); secure.put("floatingLogo", c.floatingLogo.toString())
    }
}
