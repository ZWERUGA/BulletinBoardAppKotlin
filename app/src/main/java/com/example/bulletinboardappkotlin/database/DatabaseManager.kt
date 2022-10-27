package com.example.bulletinboardappkotlin.database

import android.util.Log
import com.example.bulletinboardappkotlin.data.Advertisement
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DatabaseManager(val readDataCallback: ReadDataCallback?) {
    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAdvertisement(advertisement: Advertisement) {
        if (auth.uid != null) {
            db.child(advertisement.key ?: "empty")
                .child(auth.uid!!)
                .child("advertisement")
                .setValue(advertisement)
        }
    }

    fun readDataFromDatabase() {
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertisementArray = ArrayList<Advertisement>()
                for (item in snapshot.children) {
                    val advertisement = item.children.iterator().next()
                        .child("advertisement").getValue(Advertisement::class.java)
                    if (advertisement != null) {
                        advertisementArray.add(advertisement)
                    }
                }
                readDataCallback?.readData(advertisementArray)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }
}