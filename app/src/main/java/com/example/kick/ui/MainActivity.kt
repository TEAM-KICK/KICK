package com.example.kick.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kick.R
import com.example.kick.alarm.AddAlarmActivity
import com.example.kick.alarm.Alarm
import com.example.kick.alarm.AlarmAdapter
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : AppCompatActivity() {

    private lateinit var alarmListView: RecyclerView
    private lateinit var alarmListAdapter: AlarmAdapter

    private val REQUEST_CODE_NOTIFICATIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_home)  // alarm_home.xml을 사용하여 뷰 설정

        checkNotificationPermission()

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
            startActivity(intent)}

        // RecyclerView 설정
        alarmListView = findViewById(R.id.recyclerViewAlarms)
        alarmListView.layoutManager = LinearLayoutManager(this)

        // 알람 목록을 어댑터에 연결
        alarmListAdapter = AlarmAdapter(loadAlarms().toMutableList(), this)
        alarmListView.adapter = alarmListAdapter

        // ItemTouchHelper 콜백 생성
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // 드래그 기능은 사용하지 않음
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 스와이프된 아이템의 포지션 가져오기
                val position = viewHolder.adapterPosition
                alarmListAdapter.deleteAlarm(position)
                Log.d("MainActivity", "Alarm deleted at position: $position")
            }
        }

        // ItemTouchHelper를 RecyclerView에 연결
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(alarmListView)
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

        val alarms = alarmJsonSet.map { Alarm.fromJson(it) }.toMutableList()

        // 알람을 시간순으로 정렬
        alarms.sortWith(compareBy({ it.hour }, { it.minute }))

        return alarms
    }



    // 알림 권한 체크 및 요청 메서드
    private fun checkNotificationPermission() {
        // Android 13 (API 33) 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            } else {
                // 권한이 이미 부여된 경우
                sendNotification()  // 알림 표시하는 함수 호출
            }
        } else {
            // Android 13 미만 버전에서는 권한 필요 없음
            sendNotification()  // 알림 표시하는 함수 호출
        }
    }

    // 권한 요청 결과 처리 메서드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 알림 권한이 부여된 경우
                sendNotification()
            } else {
                // 알림 권한이 거부된 경우
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 알림을 표시하는 함수 (간단한 알림 구현 예시)
    private fun sendNotification() {
        // 알림을 생성하고 표시하는 로직을 여기에 구현합니다
        Log.d("MainActivity", "sendNotification 호출됨: 알림이 표시되어야 합니다.")
        // 예시로는 실제 알림 생성 코드가 필요합니다. 알림 표시 관련 코드를 구현해야 합니다.
    }
}
