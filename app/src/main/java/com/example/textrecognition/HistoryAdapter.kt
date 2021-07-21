package com.example.textrecognition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val translatedTextList: ArrayList<String>, private val sourceTextList: ArrayList<String>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        holder.itemSource.text = sourceTextList[position]
        holder.itemDestination.text = translatedTextList[position]
    }

    override fun getItemCount(): Int {
        return translatedTextList.size
    }

    inner class ViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        var itemSource : TextView = itemView.findViewById(R.id.sourceTextView)
        var itemDestination : TextView = itemView.findViewById(R.id.translatedTextView)
    }
}