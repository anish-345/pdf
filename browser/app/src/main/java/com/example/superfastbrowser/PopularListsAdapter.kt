package com.example.superfastbrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PopularListsAdapter(
    private val popularLists: Map<String, String>,
    private val enabledLists: MutableSet<String>
) : RecyclerView.Adapter<PopularListsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.list_name_textview)
        val checkBox: CheckBox = view.findViewById(R.id.list_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.popular_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listName = popularLists.keys.elementAt(position)
        holder.nameTextView.text = listName
        holder.checkBox.isChecked = enabledLists.contains(listName)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enabledLists.add(listName)
            } else {
                enabledLists.remove(listName)
            }
        }
    }

    override fun getItemCount() = popularLists.size
}
