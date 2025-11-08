package com.playapp.aiagents

import android.app.Application
import com.google.firebase.FirebaseApp

class AiAgentsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        println("AiAgentsApplication: Firebase initialized successfully")
    }
}