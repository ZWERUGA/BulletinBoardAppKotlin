package com.example.bulletinboardappkotlin.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.databinding.ListImagesFragmentBinding
import com.example.bulletinboardappkotlin.dialoghelper.ProgressDialog
import com.example.bulletinboardappkotlin.utils.AdapterCallback
import com.example.bulletinboardappkotlin.utils.ImageManager
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.example.bulletinboardappkotlin.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ImagesListFragmentLog"

class ImagesListFragment(private val fragmentCloseInterface: FragmentCloseInterface,
                         private val newList: ArrayList<String>?) : Fragment(), AdapterCallback {
    lateinit var rootElement: ListImagesFragmentBinding
    val adapter = SelectImageRecyclerViewAdapter(this)
    private val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageButton: MenuItem? = null

    // Начинается отрисовка фрагмента
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootElement = ListImagesFragmentBinding.inflate(inflater)
        return rootElement.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        touchHelper.attachToRecyclerView(rootElement.rcvSelectedImages)
        rootElement.rcvSelectedImages.layoutManager = LinearLayoutManager(activity)
        rootElement.rcvSelectedImages.adapter = adapter
        if (newList != null) {
            resizeSelectedImages(newList, true)
        }
    }

    override fun onItemDelete() {
        addImageButton?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    private fun resizeSelectedImages(newList: ArrayList<String>, needClear: Boolean) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResize(newList)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > ImagePicker.MAX_IMAGE_COUNT - 1) {
                addImageButton?.isVisible = false
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentCloseInterface.onFragmentClose(adapter.mainArray)
        job?.cancel()
    }

    private fun setUpToolbar() {
        rootElement.tbFragment.inflateMenu(R.menu.toolbar_choose_image)
        addImageButton = rootElement.tbFragment.menu.findItem(R.id.add_image)
        val deleteImageButton = rootElement.tbFragment.menu.findItem(R.id.delete_image)

        rootElement.tbFragment.setNavigationOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        addImageButton?.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.getImages(activity as AppCompatActivity,
                imageCount, ImagePicker.REQUEST_CODE_GET_IMAGES)
            true
        }

        deleteImageButton.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            toastMessage(this, "Все изображения удалены!")
            addImageButton?.isVisible = true
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>) {
        resizeSelectedImages(newList, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSingleImage(uri: String, position: Int) {
        val pbEditImage = rootElement.rcvSelectedImages[position]
            .findViewById<ProgressBar>(R.id.pbEditImage)
        job = CoroutineScope(Dispatchers.Main).launch {
            pbEditImage.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(listOf(uri))
            pbEditImage.visibility = View.GONE
            adapter.mainArray[position] = bitmapList[0]
            adapter.notifyItemChanged(position)
        }

    }


}