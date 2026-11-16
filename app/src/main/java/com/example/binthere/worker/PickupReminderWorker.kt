package com.example.binthere.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class PickupReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val zone = inputData.getString("zone") ?: "Nairobi"
        val point = inputData.getString("collectionPoint") ?: "Designated Skip"
        val time = inputData.getString("pickupTime") ?: "Scheduled Time"

        showNotification(
            title = "BinThere Pickup Reminder ($zone)",
            message = "Community/County waste pickup at $point starts at $time. Help keep drains clear!"
        )
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "binthere_pickup_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pickup Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}