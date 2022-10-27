package com.example.bulletinboardappkotlin.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.adapters.ImageAdapter
import com.example.bulletinboardappkotlin.data.Advertisement
import com.example.bulletinboardappkotlin.database.DatabaseManager
import com.example.bulletinboardappkotlin.databinding.ActivityEditAdsBinding
import com.example.bulletinboardappkotlin.dialogspinnerhelper.DialogSpinnerHelper
import com.example.bulletinboardappkotlin.fragment.FragmentCloseInterface
import com.example.bulletinboardappkotlin.fragment.ImagesListFragment
import com.example.bulletinboardappkotlin.utils.CountryHelper
import com.example.bulletinboardappkotlin.utils.ImageManager
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil

private const val TAG = "EditAdsActivityLog"

class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var chooseImageFragment: ImagesListFragment? = null
    var editImagePosition = 0

    private lateinit var btSelectCountry: Button
    private lateinit var btSelectCity: Button
    private lateinit var btSelectCategory: Button
    private lateinit var btPublish: Button
    private lateinit var ibtPickImages: ImageButton

    private val dbManager = DatabaseManager(null)

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
                    ImagePicker
                        .getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
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
        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
    }

    private fun init() {
        btSelectCountry = rootElement.btSelectCountry
        btSelectCity = rootElement.btSelectCity
        btSelectCategory = rootElement.btCategory
        btPublish = rootElement.btPublish
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

        btSelectCategory.setOnClickListener {
            val listCategories = resources.getStringArray(R.array.categories).toMutableList() as ArrayList
                dialog.showSpinnerDialog(this, listCategories, rootElement.btCategory)
        }

        btPublish.setOnClickListener {
            dbManager.publishAdvertisement(fillAdvertisement())
        }

        ibtPickImages.setOnClickListener {
            if (imageAdapter.mainArray.size == 0) {
                ImagePicker
                    .getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
            } else {
                openChooseImageFragment(null)
                chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
            }

        }

        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }

    private fun fillAdvertisement() : Advertisement {
        val ad: Advertisement
        rootElement.apply {
            ad = Advertisement(
                btSelectCountry.text.toString(),
                btSelectCity.text.toString(),
                etTelephone.text.toString(),
                etIndex.text.toString(),
                cbWithSend.isChecked.toString(),
                btCategory.text.toString(),
                etTitle.text.toString(),
                etPrice.text.toString(),
                etDescription.text.toString(),
                dbManager.db.push().key
            )
        }

        return ad
    }

    override fun onFragmentClose(list: ArrayList<Bitmap>) {
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.updateArray(list)
        chooseImageFragment = null
    }

    // Функция - запускает фрагмент
    fun openChooseImageFragment(newList: ArrayList<String>?) {
        chooseImageFragment = ImagesListFragment(this, newList)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFragment!!)
        fm.commit()
    }

}