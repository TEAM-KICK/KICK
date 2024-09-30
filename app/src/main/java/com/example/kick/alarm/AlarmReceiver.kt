package com.example.kick.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val hour = intent.getIntExtra("alarm_hour", -1)
        val minute = intent.getIntExtra("alarm_minute", -1)

        if (alarmId != -1) {
            // 알람이 트리거됨. 여기서 로직 처리
            Log.d("AlarmReceiver", "알람 트리거됨: ID = $alarmId, 시간 = $hour:$minute")

            // 예시: 토스트 메시지 표시
            Toast.makeText(context, "알람이 울립니다! ID: $alarmId", Toast.LENGTH_LONG).show()

            // 알림을 트리거하거나 사운드를 재생하는 Foreground 서비스 시작 가능
            // 예: sendNotification(context, alarmId, hour, minute)
        } else {
            Log.e("AlarmReceiver", "잘못된 알람 데이터 수신.")
        }
    }

    // (선택 사항) 사용자에게 알림을 전송하는 메서드
    private fun sendNotification(context: Context, alarmId: Int, hour: Int, minute: Int) {
        // 알림 전송 로직 구현
        // 예시: NotificationCompat.Builder를 사용해 알림 표시
    }
}
