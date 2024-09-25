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
    private val alarms: MutableList<Alarm>,  // MutableList로 변경하여 알람 삭제 가능
    private val context: Context,
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // ViewHolder 정의
    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmTime: TextView = itemView.findViewById(R.id.alarmTimeText)  // 알람 시간
        val alarmSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)    // 알람 활성화 스위치
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    // 데이터와 ViewHolder를 연결
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        // 알람 시간 표시
        holder.alarmTime.text = String.format("%02d:%02d", alarm.hour, alarm.minute)

        // 스위치 상태 설정
        holder.alarmSwitch.isChecked = alarm.isActive

        // 스위치 상태 변경 처리
        holder.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm.isActive = isChecked  // 스위치 변경에 따라 알람 활성화 상태 업데이트
            // SharedPreferences나 DB에 변경 사항을 저장하는 로직을 여기에 추가
        }

    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return alarms.size
    }

    // 알람 목록에서 알람 삭제
    private fun removeAlarm(position: Int) {
        alarms.removeAt(position)
        notifyItemRemoved(position)
    }
}
