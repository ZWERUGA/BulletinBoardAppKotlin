package com.example.bulletinboardappkotlin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.data.Advertisement
import com.example.bulletinboardappkotlin.databinding.AdvertisementListItemBinding

class AdvertisementRecyclerViewAdapter
    : RecyclerView.Adapter<AdvertisementRecyclerViewAdapter.AdvertisementHolder>() {

    val advertisementArray = ArrayList<Advertisement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementHolder {
        val binding = AdvertisementListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdvertisementHolder(binding)
    }

    override fun onBindViewHolder(holder: AdvertisementHolder, position: Int) {
        holder.setData(advertisementArray[position])
    }

    override fun getItemCount(): Int {
        return advertisementArray.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdvertisementAdapter(newList: List<Advertisement>) {
        advertisementArray.clear()
        advertisementArray.addAll(newList)
        notifyDataSetChanged()
    }

    class AdvertisementHolder(val binding: AdvertisementListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun setData(advertisement: Advertisement) {
            binding.apply {
                tvDescription.text = advertisement.description
                tvPrice.text = advertisement.price
                tvTitle.text = advertisement.title
            }
        }
    }
}