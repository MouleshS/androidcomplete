package com.androidcomplete.detail.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androidcomplete.detail.R

/**
 * Created by mouleshs on 25,February,2021
 */

class VitalsAdapter(var items: MutableList<Packet.Vital>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vital_item_row, parent, false)
        return VitalHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vital = items[position]
        if (holder is VitalHolder) {
            holder.bind(vital)
        }
    }
}