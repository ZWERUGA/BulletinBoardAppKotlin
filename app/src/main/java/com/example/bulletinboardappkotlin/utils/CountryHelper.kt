package com.example.bulletinboardappkotlin.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

private const val TAG = "CountryHelper"

object CountryHelper {
    fun getAllCountries(context: Context) : ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val countryNames = jsonObject.names()

            if (countryNames != null) {
                for (country in 0 until countryNames.length()) {
                    if (countryNames.getString(country).isNotEmpty()) {
                        tempArray.add(countryNames.getString(country))
                    }
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "IOException: ${e.message}")
        }
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText: String?) : ArrayList<String> {
        val tempList = ArrayList<String>()
        tempList.clear()

        if (searchText == null) {
            tempList.add("Поиск не дал результатов!")
            return tempList
        }

        for (selection : String in list) {
            if (selection.lowercase().startsWith(searchText.lowercase())) {
                tempList.add(selection)
            }
        }

        if (tempList.size == 0) tempList.add("Поиск не дал результатов!")
        return tempList
    }
}