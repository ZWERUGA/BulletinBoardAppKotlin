package com.example.bulletinboardappkotlin.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DatabaseManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAdvertisement(advertisement: Advertisement, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) {
            db.child(advertisement.key ?: "empty")
                .child(auth.uid!!)
                .child(ADVERTISEMENT_NODE)
                .setValue(advertisement).addOnCompleteListener {
                    finishWorkListener.onFinish()
                }
        }
    }

    fun deleteAdvertisement(advertisement: Advertisement, listener: FinishWorkListener) {
        if (advertisement.key == null || advertisement.uid == null) return
        db.child(advertisement.key).child(advertisement.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }

    fun advertisementViewed(advertisement: Advertisement) {
        var counter = advertisement.viewsCounter.toInt()
        counter += 1
        if (auth.uid != null) {
            db.child(advertisement.key ?: "empty")
                .child(INFORMATION_NODE)
                .setValue(
                    ItemInformation(
                        counter.toString(),
                        advertisement.emailsCounter,
                        advertisement.callsCounter
                    )
                )
        }
    }

    private fun readDataFromDatabase(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertisementArray = ArrayList<Advertisement>()
                for (item in snapshot.children) {
                    var advertisement: Advertisement? = null
                    item.children.forEach {
                        if (advertisement == null) {
                            advertisement =
                                it.child(ADVERTISEMENT_NODE).getValue(Advertisement::class.java)
                        }
                    }

                    val itemInfo =
                        item.child(INFORMATION_NODE).getValue(ItemInformation::class.java)
                    val favouritesCounter = item.child(FAVOURITE_NODE).childrenCount
                    advertisement?.viewsCounter = itemInfo?.viewsCounter ?: "0"
                    advertisement?.emailsCounter = itemInfo?.emailsCounter ?: "0"
                    advertisement?.callsCounter = itemInfo?.callsCounter ?: "0"
                    advertisement?.favouritesCounter = favouritesCounter.toString()
                    val isFavourite = auth.uid?.let {
                        item.child(FAVOURITE_NODE).child(it).getValue(String::class.java)
                    }
                    advertisement?.isFavourite = isFavourite != null

                    if (advertisement != null) {
                        advertisementArray.add(advertisement!!)
                    }
                }
                readDataCallback?.readData(advertisementArray)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Advertisement>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/advertisement/uid").equalTo(auth.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    fun getAllAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/advertisement/price")
        readDataFromDatabase(query, readDataCallback)
    }

    fun onFavouriteClick(advertisement: Advertisement, finishWorkListener: FinishWorkListener) {
        if (advertisement.isFavourite) {
            removeFromFavourites(advertisement, finishWorkListener)
        } else {
            addToFavourites(advertisement, finishWorkListener)
        }
    }

    fun getMyFavourites(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/${FAVOURITE_NODE}/${auth.uid}").equalTo(auth.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    private fun addToFavourites(advertisement: Advertisement, listener: FinishWorkListener) {
        advertisement.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVOURITE_NODE).child(uid).setValue(uid).addOnCompleteListener {
                    if (it.isSuccessful) {
                        listener.onFinish()
                    }
                }
            }
        }
    }

    private fun removeFromFavourites(advertisement: Advertisement, listener: FinishWorkListener) {
        advertisement.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVOURITE_NODE).child(uid).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        listener.onFinish()
                    }
                }
            }
        }
    }

    companion object {
        const val ADVERTISEMENT_NODE = "advertisement"
        const val INFORMATION_NODE = "information"
        const val FAVOURITE_NODE = "favourite"
        const val MAIN_NODE = "main"
    }
}