package com.example.kick.alarm

import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.kick.R
//import com.example.kick.alarm.model.Alarm

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)

        timePicker = findViewById(R.id.timePicker)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val alarm = Alarm(0, hour, minute, true)

            // 알람 저장 로직 추가
            finish()
        }
    }
}