package com.example.kick.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R

class AlarmAdapter(private val alarmList: MutableList<Alarm>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.alarmTimeText)
        val activeSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.timeTextView.text = String.format("%02d:%02d %s",
            if (alarm.hour < 12) alarm.hour else alarm.hour - 12,
            alarm.minute,
            if (alarm.hour < 12) "AM" else "PM"
        )
        holder.activeSwitch.isChecked = alarm.isActive

        holder.activeSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm.isActive = isChecked
        }
    }

    override fun getItemCount(): Int = alarmList.size

    // 알람 추가
    fun addAlarm(alarm: Alarm) {
        alarmList.add(alarm)
        notifyItemInserted(alarmList.size - 1)
    }
}

