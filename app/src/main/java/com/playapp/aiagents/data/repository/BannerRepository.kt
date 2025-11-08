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

class BannerRepository(private val firebaseService: FirebaseService = FirebaseService()) {

    @OptIn(FlowPreview::class)
    fun getBanners(context: Context): Flow<List<Banner>> = flow {
        // Emit local data immediately to avoid hanging
        emit(loadBannersFromAssets(context))

        // Then try to fetch from Firebase and emit if available
        try {
            val firebaseBanners = firebaseService.getBanners()
            firebaseBanners.timeout(5.seconds).catch { e ->
                // Firebase failed, but local data already emitted
                e.printStackTrace()
            }.collect { banners ->
                if (banners.isNotEmpty()) {
                    emit(banners)
                }
            }
        } catch (e: Exception) {
            // Firebase failed, but local data already emitted
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