package com.androidcomplete.detail.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidcomplete.detail.R

/**
 * Created by mouleshs on 25,February,2021
 */

class VitalHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sequenceTxt = itemView.findViewById<TextView>(R.id.sequenceTxt)
    val sessionTime = itemView.findViewById<TextView>(R.id.timeText)
    val batteryInfo = itemView.findViewById<TextView>(R.id.batteryTxt)
    val skinTemp = itemView.findViewById<TextView>(R.id.skinTemp)
    val ambTemp = itemView.findViewById<TextView>(R.id.ambientTemp)
    val heartRate = itemView.findViewById<TextView>(R.id.heartRateTxt)
    val motion = itemView.findViewById<TextView>(R.id.motionTxt)


    fun bind(vital: Packet.Vital) {
        sequenceTxt.text = "Sequence: " + vital.sequence
        sessionTime.text = "Session Time: " + vital.epoch
        batteryInfo.text = "Battery: " + vital.battery
        skinTemp.text = "Skin temp: " + vital.skin_temp
        ambTemp.text = "Amb temp: " + vital.ambient_temp
        heartRate.text = "Heart rate: " + vital.heart_rate
        motion.text = "Motion: " + vital.motion
    }
}