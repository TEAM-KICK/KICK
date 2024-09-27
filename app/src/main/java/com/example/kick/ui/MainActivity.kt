package com.example.kick.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
import com.example.kick.alarm.AddAlarmActivity
import com.example.kick.alarm.AlarmAdapter
import com.example.kick.alarm.Alarm

class MainActivity : AppCompatActivity() {

    private lateinit var alarmListView: RecyclerView
    private lateinit var alarmListAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)  // alarm_home.xml을 사용하여 뷰 설정

        // ADD 버튼을 누르면 AddAlarmActivity 실행
        val addAlarmButton: Button = findViewById(R.id.btnAddAlarm)
        addAlarmButton.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        alarmListView = findViewById(R.id.recyclerViewAlarms)
        alarmListView.layoutManager = LinearLayoutManager(this)

        // 알람 목록을 어댑터에 연결
        alarmListAdapter = AlarmAdapter(loadAlarms().toMutableList(), this)
        alarmListView.adapter = alarmListAdapter
    }

    override fun onResume() {
        super.onResume()

        // 알람 목록을 다시 불러와 업데이트
        val updatedAlarms = loadAlarms().toMutableList()
        Log.d("MainActivity", "Updated Alarms: $updatedAlarms")
        alarmListAdapter.updateAlarms(updatedAlarms)
    }

    // SharedPreferences에서 저장된 알람 불러오기
    private fun loadAlarms(): List<Alarm> {
        val sharedPref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val alarmJsonSet = sharedPref.getStringSet("alarms", null) ?: return emptyList()

        return alarmJsonSet.map { Alarm.fromJson(it) }
    }
}
