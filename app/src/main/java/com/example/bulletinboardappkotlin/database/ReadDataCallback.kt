package com.example.bulletinboardappkotlin.database

import com.example.bulletinboardappkotlin.data.Advertisement

interface ReadDataCallback {
    fun readData(list: List<Advertisement>)
}