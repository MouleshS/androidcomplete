package com.androidcomplete.detail.ui

import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidcomplete.detail.R

/**
 * Created by mouleshs on 26,January,2021
 */

class BleDeviceViewHolder(itemView: View, val adapter: BleAdapter) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {
    val deviceTxt = itemView.findViewById<TextView>(R.id.deviceName)

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(device: BluetoothDevice) {
        deviceTxt.text = device.name
    }

    override fun onClick(p0: View?) {
        adapter.itemClicked(adapterPosition)
    }
}