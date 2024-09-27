package com.example.kick.alarm

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R

class AlarmListActivity : AppCompatActivity() {

    private lateinit var alarmListView: RecyclerView
    private lateinit var alarmListAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)  // Correct layout reference

        alarmListView = findViewById(R.id.recyclerViewAlarms)  // Ensure this ID exists in your layout XML
        alarmListView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with the existing alarms
        alarmListAdapter = AlarmAdapter(loadAlarms().toMutableList(), this)
        alarmListView.adapter = alarmListAdapter
    }

    override fun onResume() {
        super.onResume()
        // Reload alarms when returning to this activity
        val updatedAlarms = loadAlarms().toMutableList()
        alarmListAdapter.updateAlarms(updatedAlarms)
    }

    private fun loadAlarms(): List<Alarm> {
        val sharedPref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val alarmJsonSet = sharedPref.getStringSet("alarms", null) ?: return emptyList()

        val alarms = mutableListOf<Alarm>()
        for (alarmJson in alarmJsonSet) {
            alarms.add(Alarm.fromJson(alarmJson))
        }

        return alarms
    }
}
