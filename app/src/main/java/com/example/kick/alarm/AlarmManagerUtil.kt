package com.example.kick.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class AlarmManagerUtil(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 특정 시간에 알람을 예약
    fun scheduleAlarm(alarm: Alarm) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_hour", alarm.hour)
            putExtra("alarm_minute", alarm.minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,  // 각 알람에 대해 고유한 requestCode
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 특정 시간에 알람을 트리거하도록 설정
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }

        // 만약 알람 시간이 오늘을 넘긴 경우, 내일로 예약
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
        )

        Log.d("AlarmManagerUtil", "알람 예약됨: $alarm at ${calendar.time}")
    }

    // 알람 ID로 알람 취소
    fun cancelAlarm(alarm: Alarm) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmManagerUtil", "알람 취소됨: ${alarm.id}")
    }
}