package com.example.kick.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
import android.util.Log
import android.widget.Button
import com.google.android.material.switchmaterial.SwitchMaterial

class AlarmAdapter(
    private var alarms: MutableList<Alarm>,
    private val context: Context
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmTime: TextView = itemView.findViewById(R.id.alarmTimeText)
//        val alarmSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)
        val alarmSwitch: SwitchMaterial = itemView.findViewById(R.id.alarmSwitch)
//        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
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

            val alarmManagerUtil = AlarmManagerUtil(context)
            if (isChecked) {
                // 스위치가 켜진 경우 알람 예약
                alarmManagerUtil.scheduleAlarm(alarm)
                Log.d("AlarmAdapter", "Alarm scheduled: ${alarm.id}")
            } else {
                // 스위치가 꺼진 경우 알람 취소
                alarmManagerUtil.cancelAlarm(alarm)
                Log.d("AlarmAdapter", "Alarm canceled: ${alarm.id}")
            }

            // (옵션) 스위치 상태 변경 시 SharedPreferences에 저장하는 로직 추가 가능
        }

        // 삭제 버튼 클릭 리스너 설정
//        holder.btnDelete.setOnClickListener {
//            deleteAlarm(position)
//            Log.d("AlarmAdapter", "Alarm deleted: ${alarm.id}")
//        }
    }

    override fun getItemCount(): Int = alarms.size

    // 알람 목록 업데이트
    fun updateAlarms(newAlarms: MutableList<Alarm>) {
        alarms.clear()
        alarms.addAll(newAlarms)
        notifyDataSetChanged()  // 데이터 변경 알림
    }

    fun deleteAlarm(position: Int) {
        if (position >= 0 && position < alarms.size) {
            val alarm = alarms[position]

            // 알람을 목록에서 제거
            alarms.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, alarms.size)

            // SharedPreferences에서 알람 제거 로직
            val sharedPref = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            val alarmJsonSet = sharedPref.getStringSet("alarms", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            alarmJsonSet.remove(alarm.toJson())

            with(sharedPref.edit()) {
                putStringSet("alarms", alarmJsonSet)
                apply()
            }

            // 알람 취소
            AlarmManagerUtil(context).cancelAlarm(alarm)
        } else {
            Log.e("AlarmAdapter", "Invalid position: $position for list size: ${alarms.size}")
        }
    }
}
