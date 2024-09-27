package com.example.kick.ui

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.kick.R
import com.example.kick.alarm.AddAlarmActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)

        // ADD 버튼을 누르면 AddAlarmActivity 실행
        val addAlarmButton: Button = findViewById(R.id.btnAddAlarm)
        addAlarmButton.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java)
            startActivity(intent)
        }

        // Assuming you have a button in your MainActivity layout to start CameraActivity
        val startCameraButton: Button = findViewById(R.id.btntestface)
        startCameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }
}