package com.example.kick.alarm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
//import com.example.kick.model.Alarm
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlarmListActivity : AppCompatActivity() {

    private lateinit var alarmListView: RecyclerView
    private lateinit var addAlarmButton: FloatingActionButton
    private lateinit var alarmListAdapter: AlarmListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)

        alarmListView = findViewById(R.id.alarmListView)
        addAlarmButton = findViewById(R.id.addAlarmButton)

        alarmListAdapter = AlarmListAdapter(loadAlarms(), this)
        alarmListView.layoutManager = LinearLayoutManager(this)
        alarmListView.adapter = alarmListAdapter

        // 알람 추가 버튼 클릭 리스너
        addAlarmButton.setOnClickListener {
            startActivity(Intent(this, AddAlarmActivity::class.java))
        }
    }

    private fun loadAlarms(): List<Alarm> {
        // 실제 저장된 알람을 불러오는 로직 (SharedPreferences, SQLite 등)
        return listOf(
                Alarm(1, 9, 0, true),
                Alarm(2, 10, 30, false)
        )
    }
}
