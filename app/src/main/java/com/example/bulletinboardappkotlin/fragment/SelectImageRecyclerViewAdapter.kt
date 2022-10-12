package com.example.bulletinboardappkotlin.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.example.bulletinboardappkotlin.utils.ItemTouchMoveCallback

class SelectImageRecyclerViewAdapter :
    RecyclerView.Adapter<SelectImageRecyclerViewAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.select_image_fragment_item, parent, false)
        return ImageHolder(view, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPosition: Int, targetPosition: Int) {
        val targetItem = mainArray[targetPosition]
        mainArray[targetPosition] = mainArray[startPosition]
        mainArray[startPosition] = targetItem
        notifyItemMoved(startPosition, targetPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(itemView: View,
                      val context: Context,
                      val adapter: SelectImageRecyclerViewAdapter)
        : RecyclerView.ViewHolder(itemView) {
        private lateinit var tvFragmentItemTitle: TextView
        lateinit var ivFragmentItemImage: ImageView
        lateinit var imbEditImage: ImageButton
        lateinit var imbDeleteImage: ImageButton

        fun setData(item: String) {
            tvFragmentItemTitle = itemView.findViewById(R.id.tvFragmentItemTitle)
            ivFragmentItemImage = itemView.findViewById(R.id.ivFragmentItemImage)
            imbEditImage = itemView.findViewById(R.id.imbEditImage)
            imbDeleteImage = itemView.findViewById(R.id.imbDeleteImage)

            imbEditImage.setOnClickListener {
                ImagePicker.getImages(context as EditAdsActivity, 1,
                    ImagePicker.REQUEST_CODE_GET_SINGLE_IMAGE)
                context.editImagePosition = adapterPosition
            }

            imbDeleteImage.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) {
                    adapter.notifyItemChanged(n)
                }
            }

            tvFragmentItemTitle.text =
                context.resources.getStringArray(R.array.title_image_array)[adapterPosition]
            ivFragmentItemImage.setImageURI(Uri.parse(item))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<String>, needClear: Boolean) {
        if (needClear) {
            mainArray.clear()
        }
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}