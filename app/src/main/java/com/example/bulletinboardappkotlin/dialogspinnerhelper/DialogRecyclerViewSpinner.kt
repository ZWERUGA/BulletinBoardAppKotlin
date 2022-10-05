package com.example.bulletinboardappkotlin.dialogspinnerhelper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.R

class DialogRecyclerViewSpinner :
    RecyclerView.Adapter<DialogRecyclerViewSpinner.SpinnerViewHolder>() {
    private val mainList = ArrayList<String>()

    class SpinnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setSpinnerItemText(text: String) {
            val tvSpinnerItem = itemView.findViewById<TextView>(R.id.tvSpinnerItem)
            tvSpinnerItem.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.spinner_list_item, parent, false)
        return SpinnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpinnerViewHolder, position: Int) {
        holder.setSpinnerItemText(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}