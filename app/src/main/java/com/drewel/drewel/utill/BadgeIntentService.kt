package com.drewel.drewel.utill

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat

import com.drewel.drewel.R

import me.leolin.shortcutbadger.ShortcutBadger
import java.util.*

class BadgeIntentService : IntentService("BadgeIntentService") {

    private var notificationId = 0

    private var mNotificationManager: NotificationManager? = null

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val badgeCount = intent.getIntExtra("badgeCount", 0)
            val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
            val notificationManager = getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            val mBuilder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                    .setContentTitle("")
                    .setContentText("")
                    .setSmallIcon(R.mipmap.ic_launcher);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher_white)
                mBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
            } else
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.default_notification_channel_name)
                val description = getString(R.string.default_notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(getString(R.string.default_notification_channel_id), name, importance)
                mChannel.description = description
                notificationManager.createNotificationChannel(mChannel)
            }
            notificationManager.notify(getRandomNumer(), mBuilder.build())
            ShortcutBadger.applyCount(applicationContext, badgeCount)
        }
    }

    fun getRandomNumer(): Int {
        val r = Random()
        return r.nextInt(80 - 65) + 65
    }
}
