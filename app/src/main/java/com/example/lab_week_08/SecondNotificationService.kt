package com.example.lab_week_08

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        val handlerThread = HandlerThread("SecondNotificationThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Third worker process is done")
            .setContentText("Second notification active!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Third process completed, second notification running!")
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, builder.build())
        return builder
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0

        return PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), flag
        )
    }

    private fun createNotificationChannel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "002"
            val channelName = "002 Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            channelId
        } else ""
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)
        val id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        serviceHandler.post {
            countDownFromFiveToZero(notificationBuilder)
            notifyCompletion(id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return returnValue
    }

    private fun countDownFromFiveToZero(builder: NotificationCompat.Builder) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (i in 5 downTo 0) {
            Thread.sleep(1000L)
            builder.setContentText("$i seconds left before completion").setSilent(true)
            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun notifyCompletion(id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA8
        const val EXTRA_ID = "Id2"
        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
