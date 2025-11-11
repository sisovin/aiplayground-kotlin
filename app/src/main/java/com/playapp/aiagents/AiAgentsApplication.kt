package com.playapp.aiagents

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.core.provider.FontRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class AiAgentsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize EmojiCompat
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
            .setReplaceAll(true)
            .registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    println("AiAgentsApplication: EmojiCompat initialized successfully")
                }

                override fun onFailed(throwable: Throwable?) {
                    println("AiAgentsApplication: EmojiCompat initialization failed: ${throwable?.message}")
                }
            })
        EmojiCompat.init(config)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        println("AiAgentsApplication: Firebase initialized successfully")

        // Configure Firebase Database persistence BEFORE any database usage
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            setPersistenceCacheSizeBytes(10 * 1024 * 1024) // 10MB cache
        }
        println("AiAgentsApplication: Firebase Database persistence configured")
    }
}