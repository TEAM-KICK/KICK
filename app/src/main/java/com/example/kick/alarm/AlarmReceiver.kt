package com.example.kick.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.kick.R
import com.example.kick.ui.MainActivity
import com.example.kick.ui.CameraActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val hour = intent.getIntExtra("alarm_hour", -1)
        val minute = intent.getIntExtra("alarm_minute", -1)

        if (alarmId != -1) {
            // 알람 트리거 로직 처리
            Log.d("AlarmReceiver", "알람 트리거됨: ID = $alarmId, 시간 = $hour:$minute")

            // Toast 메시지 표시 (옵션)
            Toast.makeText(context, "웃을 시간 입니다 ! ID: $alarmId", Toast.LENGTH_LONG).show()

            // 알림(Notification)을 통해 알람 표시
            sendNotification(context, alarmId, hour, minute)
        } else {
            Log.e("AlarmReceiver", "잘못된 알람 데이터 수신.")
        }
    }

    // (선택 사항) 사용자에게 알림을 전송하는 메서드
    // 알림을 전송하는 메서드
    private fun sendNotification(context: Context, alarmId: Int, hour: Int, minute: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"

        Log.d("AlarmReceiver", "Sending notification for alarm: $alarmId at $hour:$minute")
        // Android 8.0 (API 26) 이상에서는 NotificationChannel이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "알람 채널",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "알람을 위한 채널"
                enableLights(true) // 불빛 켜기
                enableVibration(true) // 진동 설정
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 열릴 액티비티 (예: MainActivity)
        val notificationIntent = Intent(context, CameraActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, alarmId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림(Notification) 생성
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_happy)  // 알람 아이콘 설정
            .setContentTitle("알람")
            .setContentText("웃을 시간 입니다 ! $hour:$minute")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // 알림 클릭 시 알림 제거
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // 알림 표시
        notificationManager.notify(alarmId, notification)
    }
}
