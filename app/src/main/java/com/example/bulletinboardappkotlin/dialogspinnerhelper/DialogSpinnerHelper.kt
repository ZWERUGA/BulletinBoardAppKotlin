package com.example.bulletinboardappkotlin.dialogspinnerhelper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.utils.CountryHelper

class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>) {
        val builder = AlertDialog.Builder(context)
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner, null)
        val adapter = DialogRecyclerViewSpinner()
        val rvSpinnerView = rootView.findViewById<RecyclerView>(R.id.rvSpinnerView)
        val svCountrySearch = rootView.findViewById<SearchView>(R.id.svSpinner)
        rvSpinnerView.layoutManager = LinearLayoutManager(context)
        rvSpinnerView.adapter = adapter
        builder.setView(rootView)
        adapter.updateAdapter(list)
        setSearchView(adapter, list, svCountrySearch)
        builder.show()
    }

    private fun setSearchView(adapter: DialogRecyclerViewSpinner,
                              list: ArrayList<String>,
                              svCountrySearch: SearchView?) {
        svCountrySearch?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CountryHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }
}
