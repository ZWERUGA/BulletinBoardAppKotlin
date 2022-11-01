package com.example.bulletinboardappkotlin.utils

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3

    private fun getOptions(imageCount: Int): Options {
        return Options.init()
            .setCount(imageCount)
            .setFrontfacing(false)
            .setMode(Options.Mode.Picture)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/pix/images")
    }

    fun launcher(
        editActivity: EditAdsActivity, launcher: ActivityResultLauncher<Intent>?, imageCount: Int) {
        PermUtil.checkForCamaraWritePermissions(editActivity) {
            val intent = Intent(editActivity, Pix::class.java).apply {
                putExtra("options", getOptions(imageCount))
            }
            launcher?.launch(intent)
        }

    }

    fun getLauncherForMultiSelectImages(editActivity: EditAdsActivity): ActivityResultLauncher<Intent> {
        return editActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
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
            }
        }
    }

    fun getLauncherForSingleImage(editActivity: EditAdsActivity): ActivityResultLauncher<Intent> {
        return editActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val returnValue = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    editActivity.chooseImageFragment
                        ?.setSingleImage(returnValue?.get(0)!!, editActivity.editImagePosition)
                }
            }
        }
    }
}