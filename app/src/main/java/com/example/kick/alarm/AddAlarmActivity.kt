package com.example.kick.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.kick.R
import java.util.*

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_add)

        timePicker = findViewById(R.id.timePicker)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val alarm = Alarm(0, hour, minute, true) // 새로운 알람 객체 생성

            // 알람 저장
            saveAlarm(alarm)
            Log.d("AddAlarmActivity", "Alarm saved: ${alarm.toJson()}")
            finish()
        }
    }

    private fun saveAlarm(alarm: Alarm) {
        val sharedPref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val alarmJsonSet = sharedPref.getStringSet("alarms", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // 알람 객체를 JSON으로 변환하여 Set에 추가
        alarmJsonSet.add(alarm.toJson())

        // 변경된 Set을 다시 저장
        with(sharedPref.edit()) {
            putStringSet("alarms", alarmJsonSet)
            apply()  // 데이터를 비동기적으로 저장
        }
    }

    // AlarmManager를 사용해 알람을 설정하는 메서드
    private fun setAlarm(alarm: Alarm) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 알람을 받을 수 있도록 AlarmReceiver로 브로드캐스트를 설정
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 현재 시간 기준으로 알람을 설정할 시간을 캘린더로 생성
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 알람 시간이 현재 시간보다 이전이면, 다음날로 설정
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 알람 설정 (정확한 시간에 실행되도록 설정)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

}
