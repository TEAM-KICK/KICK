package com.example.kick.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlarmListActivity : AppCompatActivity() {

    private lateinit var alarmListView: RecyclerView
    private lateinit var addAlarmButton: FloatingActionButton
    private lateinit var alarmListAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)

        alarmListView = findViewById(R.id.recyclerViewAlarms)
        addAlarmButton = findViewById(R.id.btnAddAlarm)

        // 알람 목록 로드 후 어댑터 설정
        alarmListAdapter = AlarmAdapter(loadAlarms(), this)
        alarmListView.layoutManager = LinearLayoutManager(this)
        alarmListView.adapter = alarmListAdapter

        // 알람 추가 버튼 클릭 리스너
        addAlarmButton.setOnClickListener {
            startActivity(Intent(this, AddAlarmActivity::class.java))
        }
    }

    // 여러 알람을 불러오는 함수
    private fun loadAlarms(): MutableList<Alarm> {  // 반환 타입을 MutableList로 변경
        val sharedPref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val alarmJsonSet = sharedPref.getStringSet("alarms", null) ?: return mutableListOf()  // 빈 MutableList 반환

        val alarms = mutableListOf<Alarm>()
        for (alarmJson in alarmJsonSet) {
            alarms.add(Alarm.fromJson(alarmJson))
        }

        return alarms  // MutableList 반환
    }
}