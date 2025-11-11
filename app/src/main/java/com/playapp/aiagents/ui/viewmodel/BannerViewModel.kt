package com.playapp.aiagents.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playapp.aiagents.data.model.Banner
import com.playapp.aiagents.data.repository.BannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BannerViewModel(application: android.app.Application, private val repository: BannerRepository = BannerRepository(null)) : AndroidViewModel(application) {

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners

    init {
        loadBanners()
    }

    private fun loadBanners() {
        viewModelScope.launch {
            try {
                repository.getBanners(getApplication()).collect { bannerList ->
                    _banners.value = bannerList
                }
            } catch (e: Exception) {
                android.util.Log.e("BannerViewModel", "Failed to load banners: ${e.message}")
                // The repository should still emit local banners even if Firebase fails
                _banners.value = emptyList()
            }
        }
    }
}