package com.example.bulletinboardappkotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.MainActivity
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.adapters.ImageAdapter
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.model.DatabaseManager
import com.example.bulletinboardappkotlin.databinding.ActivityEditAdsBinding
import com.example.bulletinboardappkotlin.dialogspinnerhelper.DialogSpinnerHelper
import com.example.bulletinboardappkotlin.fragment.FragmentCloseInterface
import com.example.bulletinboardappkotlin.fragment.ImagesListFragment
import com.example.bulletinboardappkotlin.utils.CountryHelper
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.fxn.utility.PermUtil

private const val TAG = "EditAdsActivityLog"

class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var chooseImageFragment: ImagesListFragment? = null
    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null

    var editImagePosition = 0
    private var isEditState = false
    private var advertisement: Advertisement? = null

    private lateinit var btSelectCountry: Button
    private lateinit var btSelectCity: Button
    private lateinit var btSelectCategory: Button
    private lateinit var btPublish: Button
    private lateinit var ibtPickImages: ImageButton

    private val dbManager = DatabaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        checkEditState()
    }

    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            advertisement = intent.getSerializableExtra(MainActivity.ADS_DATA) as Advertisement
            if (advertisement != null) {
                fillViews(advertisement!!)
            }
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(advertisement: Advertisement) = with(rootElement) {
        btSelectCountry.text = advertisement.country
        btSelectCity.text = advertisement.city
        etTelephone.setText(advertisement.telephone)
        etIndex.setText(advertisement.index)
        cbWithSend.isChecked = advertisement.withSend.toBoolean()
        btSelectCategory.text = advertisement.category
        etTitle.setText(advertisement.title)
        etPrice.setText(advertisement.price)
        etDescription.setText(advertisement.description)
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
                    ImagePicker.launcher(this, launcherMultiSelectImage, 3)
                } else {
                    toastMessage(
                        this, "Approve permissions to open Pix ImagePicker")
                }
                return
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun init() {
        btSelectCountry = rootElement.btSelectCountry
        btSelectCity = rootElement.btSelectCity
        btSelectCategory = rootElement.btCategory
        btPublish = rootElement.btPublish
        ibtPickImages = rootElement.ibtPickImages

        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this)

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
            val advertisementTemp = fillAdvertisement()
            if (isEditState) {
                dbManager.publishAdvertisement(advertisementTemp.copy(key = advertisement?.key), onPublishFinish())
            } else {
                dbManager.publishAdvertisement(advertisementTemp, onPublishFinish())
            }
        }

        ibtPickImages.setOnClickListener {
            if (imageAdapter.mainArray.size == 0) {
                ImagePicker.launcher(this, launcherMultiSelectImage, 3)
            } else {
                openChooseImageFragment(null)
                chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
            }

        }

        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }

    private fun onPublishFinish(): DatabaseManager.FinishWorkListener {
        return object: DatabaseManager.FinishWorkListener {
            override fun onFinish() {
                finish()
            }
        }
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
                dbManager.db.push().key,
                dbManager.auth.uid,
                "0"
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