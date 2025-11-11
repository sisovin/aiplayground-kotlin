package com.playapp.aiagents.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.playapp.aiagents.data.model.Banner
import com.playapp.aiagents.data.service.FirebaseService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration.Companion.seconds

class BannerRepository(private val firebaseService: FirebaseService? = null) {

    private val firebaseServiceInstance: FirebaseService? by lazy {
        try {
            firebaseService ?: FirebaseService()
        } catch (e: Exception) {
            android.util.Log.e("BannerRepository", "Failed to create FirebaseService: ${e.message}")
            null
        }
    }

    @OptIn(FlowPreview::class)
    fun getBanners(context: Context): Flow<List<Banner>> = flow {
        // Emit local data immediately to avoid hanging
        val localBanners = loadBannersFromAssets(context)
        println("BannerRepository: Emitting ${localBanners.size} local banners")
        emit(localBanners)

        // Then try to fetch from Firebase and emit if available
        try {
            println("BannerRepository: Attempting to fetch from Firebase")
            firebaseServiceInstance?.let { service ->
                val firebaseBanners = service.getBanners()
                firebaseBanners.timeout(5.seconds).catch { e ->
                    println("BannerRepository: Firebase failed with timeout: ${e.message}")
                    e.printStackTrace()
                }.collect { banners ->
                    println("BannerRepository: Received ${banners.size} banners from Firebase")
                    if (banners.isNotEmpty()) {
                        println("BannerRepository: Emitting Firebase banners")
                        emit(banners)
                    } else {
                        println("BannerRepository: Firebase returned empty list")
                    }
                }
            }
        } catch (e: Exception) {
            println("BannerRepository: Firebase failed with exception: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadBannersFromAssets(context: Context): List<Banner> {
        return try {
            val json = context.assets.open("database.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<Banner>>>() {}.type
            val data: Map<String, List<Banner>> = gson.fromJson(json, type)
            data["Banner"] ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            // Return dummy data if parsing fails
            listOf(
                Banner("https://res.cloudinary.com/dwhren8z6/image/upload/v1762565996/banner_1_giqevw.png")
            )
        }
    }
}