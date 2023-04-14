package com.example.finalprojectrodrigoaugusto.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.finalprojectrodrigoaugusto.R

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val notification: NotificationCompat.Builder = NotificationCompat.Builder(context, "INFNET")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.nova_tarefa_vencendo))
                .setContentText(context.getString(R.string.proxima_tarefa_vencendo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(0, notification.build())
        }
    }
}