package com.example.bulletinboardappkotlin.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.databinding.ListImagesFragmentBinding
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.example.bulletinboardappkotlin.utils.ItemTouchMoveCallback

private const val TAG = "ImagesListFragmentLog"

class ImagesListFragment(private val fragmentCloseInterface: FragmentCloseInterface,
                         private val newList: ArrayList<String>) : Fragment() {
    lateinit var rootElement: ListImagesFragmentBinding
    val adapter = SelectImageRecyclerViewAdapter()
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)

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
        adapter.updateAdapter(newList, true)
    }

    override fun onDetach() {
        super.onDetach()
        fragmentCloseInterface.onFragmentClose(adapter.mainArray)
    }

    private fun setUpToolbar() {
        rootElement.tbFragment.inflateMenu(R.menu.toolbar_choose_image)
        val addImageButton = rootElement.tbFragment.menu.findItem(R.id.add_image)
        val deleteImageButton = rootElement.tbFragment.menu.findItem(R.id.delete_image)

        rootElement.tbFragment.setNavigationOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        addImageButton.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.getImages(activity as AppCompatActivity,
                imageCount, ImagePicker.REQUEST_CODE_GET_IMAGES)
            true
        }

        deleteImageButton.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            toastMessage(this, "Все изображения удалены!")
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>) {
        adapter.updateAdapter(newList, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSingleImage(uri: String, position: Int) {
        adapter.mainArray[position] = uri
        adapter.notifyDataSetChanged()
    }
}