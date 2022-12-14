package com.example.bulletinboardappkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.model.DatabaseManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DatabaseManager()
    val liveAdsData = MutableLiveData<ArrayList<Advertisement>>()

    fun loadAllAds() {
        dbManager.getAllAds(object : DatabaseManager.ReadDataCallback {
            override fun readData(list: ArrayList<Advertisement>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAds() {
        dbManager.getMyAds(object : DatabaseManager.ReadDataCallback {
            override fun readData(list: ArrayList<Advertisement>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(advertisement: Advertisement) {
        dbManager.deleteAdvertisement(advertisement, object : DatabaseManager.FinishWorkListener {
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(advertisement)
                liveAdsData.postValue(updatedList)
            }
        })
    }

    fun loadMyFavourites() {
        dbManager.getMyFavourites(object : DatabaseManager.ReadDataCallback {
            override fun readData(list: ArrayList<Advertisement>) {
                liveAdsData.value = list
            }
        })
    }

    fun advertisementViewed(advertisement: Advertisement) {
        dbManager.advertisementViewed(advertisement)
    }

    fun onFavouriteClick(advertisement: Advertisement) {
        dbManager.onFavouriteClick(advertisement, object : DatabaseManager.FinishWorkListener {
            override fun onFinish() {
                val updatedList = liveAdsData.value
                val position = updatedList?.indexOf(advertisement)
                if (position != -1) {
                    position?.let {
                        val favouriteCounter =
                            if (advertisement.isFavourite) advertisement.favouritesCounter.toInt() - 1
                            else advertisement.favouritesCounter.toInt() + 1
                        updatedList[position] =
                            updatedList[position].copy(
                                isFavourite = !advertisement.isFavourite,
                                favouritesCounter = favouriteCounter.toString()
                            )
                    }
                }
                liveAdsData.postValue(updatedList)
            }

        })
    }
}