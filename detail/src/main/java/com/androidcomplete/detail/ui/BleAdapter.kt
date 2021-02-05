package com.androidcomplete.detail.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androidcomplete.detail.R

/**
 * Created by mouleshs on 26,January,2021
 */

class BleAdapter(var items: List<BluetoothDevice>, val clickContract: ItemClickContract) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item_row, parent, false)
        return BleDeviceViewHolder(view, this)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = items[position]
        if (holder is BleDeviceViewHolder) {
            holder.bind(device)
        }
    }

    fun publish(items: List<BluetoothDevice>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun itemClicked(position: Int) {
        clickContract.itemClicked(items[position])
    }

    interface ItemClickContract {
        fun itemClicked(device: BluetoothDevice)
    }
}