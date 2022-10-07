package com.example.bulletinboardappkotlin.activities

import android.R.attr
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.databinding.ActivityEditAdsBinding
import com.example.bulletinboardappkotlin.dialogspinnerhelper.DialogSpinnerHelper
import com.example.bulletinboardappkotlin.utils.CountryHelper
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil

private const val TAG = "EditAdsActivityLog"

class EditAdsActivity : AppCompatActivity() {
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()

    private lateinit var btSelectCountry: Button
    private lateinit var btSelectCity: Button
    private lateinit var ibtPickImages: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.getImages(this)
                } else {
                    toastMessage(
                        this, "Approve permissions to open Pix ImagePicker")
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                Log.d(TAG, "Image(s): ${returnValue?.get(0)}")
                Log.d(TAG, "Image(s): ${returnValue?.get(1)}")
                Log.d(TAG, "Image(s): ${returnValue?.get(2)}")
            }
        }
    }

    private fun init() {
        btSelectCountry = rootElement.btSelectCountry
        btSelectCity = rootElement.btSelectCity
        ibtPickImages = rootElement.ibtPickImages

        btSelectCountry.setOnClickListener {
            val listCountries = CountryHelper.getAllCountries(this)
            dialog.showSpinnerDialog(this, listCountries, rootElement.btSelectCountry)
            if (rootElement.btSelectCity.text.toString() != getString(R.string.select_city)) {
                rootElement.btSelectCity.text = getString(R.string.select_city)
            }
        }

        btSelectCity.setOnClickListener {
            val selectedCountry = rootElement.btSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCities = CountryHelper.getAllCities(selectedCountry, this)
                dialog.showSpinnerDialog(this, listCities, rootElement.btSelectCity)
            } else {
                toastMessage(this, "Страна не выбрана!")
            }
        }

        ibtPickImages.setOnClickListener {
            ImagePicker.getImages(this)
        }
    }

}