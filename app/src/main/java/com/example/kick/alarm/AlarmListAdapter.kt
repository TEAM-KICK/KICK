package com.example.kick.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
//import com.example.kick.alarm.model.Alarm

class AlarmAdapter(private var alarmList: MutableList<Alarm>) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // ViewHolder 정의
    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAlarmTime: TextView = view.findViewById(R.id.tvAlarmTime)
        val switchAlarm: Switch = view.findViewById(R.id.switchAlarm)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.tvAlarmTime.text = alarm.time
        holder.switchAlarm.isChecked = alarm.isEnabled
    }

    // 리스트의 항목 개수
    override fun getItemCount(): Int {
        return alarmList.size
    }

    // 알람 추가 메서드
    fun addAlarm(alarm: Alarm) {
        alarmList.add(alarm)
        notifyItemInserted(alarmList.size - 1)
    }

    // 알람 삭제 메서드
    fun removeAlarm(position: Int) {
        alarmList.removeAt(position)
        notifyItemRemoved(position)
    }
}
