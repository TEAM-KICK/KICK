package com.example.kick.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R

class AlarmAdapter(
    private var alarms: MutableList<Alarm>,
    private val context: Context
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmTime: TextView = itemView.findViewById(R.id.alarmTimeText)
        val alarmSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        // 알람 시간 표시
        holder.alarmTime.text = String.format("%02d:%02d", alarm.hour, alarm.minute)

        // 스위치 상태 변경 리스너 중복 방지
        holder.alarmSwitch.setOnCheckedChangeListener(null)
        holder.alarmSwitch.isChecked = alarm.isActive

        holder.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm.isActive = isChecked
            // 스위치 상태 변경 시 SharedPreferences에 저장하는 로직 추가 가능
        }
    }

    override fun getItemCount(): Int = alarms.size

    // 알람 목록 업데이트
    fun updateAlarms(newAlarms: MutableList<Alarm>) {
        alarms.clear()
        alarms.addAll(newAlarms)
        notifyDataSetChanged()  // 데이터 변경 알림
    }
}
