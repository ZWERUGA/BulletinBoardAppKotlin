package com.example.bulletinboardappkotlin.utils

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    const val MAX_IMAGE_COUNT = 3

    fun getImages(context: AppCompatActivity, imageCount: Int, requestCode: Int) {
        val options: Options = Options.init()
            .setRequestCode(requestCode)
            .setCount(imageCount)
            .setFrontfacing(false)
            .setMode(Options.Mode.Picture)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/pix/images")

        Pix.start(context, options)
    }

    fun showSelectedImages(resultCode: Int, requestCode: Int, data: Intent?, editActivity: EditAdsActivity) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                if (returnValues?.size!! > 1 && editActivity.chooseImageFragment == null) {
                    editActivity.openChooseImageFragment(returnValues)
                } else if (returnValues.size == 1 && editActivity.chooseImageFragment == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        editActivity.rootElement.pbLoadedImages.visibility = View.VISIBLE
                        val bitmapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        editActivity.rootElement.pbLoadedImages.visibility = View.GONE
                        editActivity.imageAdapter.updateArray(bitmapArray)
                    }
                } else if (editActivity.chooseImageFragment != null) {
                    editActivity.chooseImageFragment?.updateAdapter(returnValues)
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK
            && requestCode == REQUEST_CODE_GET_SINGLE_IMAGE) {
            if (data != null) {
                val returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                editActivity.chooseImageFragment
                    ?.setSingleImage(returnValue?.get(0)!!, editActivity.editImagePosition)
            }
        }
    }
}