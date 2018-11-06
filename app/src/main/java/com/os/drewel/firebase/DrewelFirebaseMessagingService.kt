package com.os.drewel.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.os.drewel.R
import com.os.drewel.activity.*
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.model.PNModel
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.BadgeIntentService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.leolin.shortcutbadger.ShortcutBadger
import org.json.JSONObject
import java.util.*

/**
 * Created by monikab on 2/23/2018.
 */
class DrewelFirebaseMessagingService : FirebaseMessagingService() {
    var unread = 0
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.e("Remote Message=blank", "fdg")
        Log.e("Remote Message=", remoteMessage!!.data.toString())
        var data = sendNotif(remoteMessage.data)
        unread = Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT)
        unread += 1
        Prefs.getInstance(this).setPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT, unread)
        ShortcutBadger.applyCount(this, Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT))
        generateNotification(data)
    }
    /*pendingCart
    orderPlaced
    orderCancelled
    productAvailable
    general
    deliveryStatusChange
    deliveryBoyAssigned*/

    object NotificationType {
        const val pendingCart = "pendingCart"
        const val orderPlaced = "orderPlaced"
        const val orderCancelled = "orderCancelled"
        const val productAvailable = "productAvailable"
        const val general = "general"
        const val deliveryStatusChange = "deliveryStatusChange"
        const val deliveryBoyAssigned = "deliveryBoyAssigned"
    }

    private fun generateNotification(data: PNModel) {

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
        val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))

                .setContentTitle(getString(R.string.app_name))
                .setContentText(data.message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setLargeIcon(largeIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(data.message))
                .setContentIntent(setNotificationIntent(data))
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_white)
            mBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        } else
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.default_notification_channel_name)
            val description = getString(R.string.default_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(getString(R.string.default_notification_channel_id), name, importance)
            mChannel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(mChannel)
        }
//        val id = (System.currentTimeMillis() * (Math.random() * 100).toInt()).toInt()
        notificationManager.notify(getRandomNumer(), mBuilder.build())

        when {
            data.notification_type.equals(NotificationType.orderCancelled) ||
                    data.notification_type.equals(NotificationType.deliveryStatusChange) ||
                    data.notification_type.equals(NotificationType.deliveryBoyAssigned)
            -> try {
                if (Prefs.getInstance(this).getPreferenceStringData(Prefs.getInstance(this).KEY_USER_ID).isEmpty()) {
                } else {
                    val intent2 = Intent()
                    intent2.action = "UPDATE_STATUS"
                    sendBroadcast(intent2)
                }
            } catch (e: Exception) {
            }
        }

    }


    private fun setNotificationIntent(data: PNModel): PendingIntent {
        var notificationIntent: Intent? = null
        if (data.notification_type.equals(NotificationType.pendingCart)) {
            notificationIntent = Intent(this, CartActivity::class.java)
        } else if (data.notification_type.equals(NotificationType.orderPlaced)) {
            notificationIntent = Intent(this, MyOrderDetailActivity::class.java)
            notificationIntent.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
        } else if (data.notification_type.equals(NotificationType.orderCancelled)) {
            notificationIntent = Intent(this, MyOrderDetailActivity::class.java)
            notificationIntent.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
        } else if (data.notification_type.equals(NotificationType.productAvailable)) {
            notificationIntent = Intent(this, ProductDetailActivity::class.java)
            notificationIntent.putExtra(AppIntentExtraKeys.PRODUCT_ID, data.item_id)
        } else if (data.notification_type.equals(NotificationType.deliveryStatusChange)) {
            notificationIntent = Intent(this, MyOrderDetailActivity::class.java)
            notificationIntent.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
        } else if (data.notification_type.equals(NotificationType.deliveryBoyAssigned)) {
            notificationIntent = Intent(this, MyOrderDetailActivity::class.java)
            notificationIntent.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
        } else if (data.notification_type.equals(NotificationType.general)) {
//          notificationIntent!!.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
            notificationIntent = Intent(this, NotificationActivity::class.java)
        }

//      val notificationIntent = Intent(this, ProductDetailActivity::class.java)
        notificationIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val parentIntent = Intent(this, HomeActivity::class.java)
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        /* create stack builder if you want to open parent activty on notification click*/
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(HomeActivity::class.java)
//        notificationIntent.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
        notificationIntent.putExtra(AppIntentExtraKeys.FROM, 1)
        notificationIntent.putExtra(AppIntentExtraKeys.NOTIFICATION_ID, data.notification_id)
        /* add all notification to stack*/
        stackBuilder.addNextIntent(parentIntent)
        stackBuilder.addNextIntent(notificationIntent)
        /* get pending intent from stack builder*/
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun sendNotif(data: Map<String, String>): PNModel {
        var notificationsModel = PNModel()
        notificationsModel.notification_type = data["notification_type"]
        notificationsModel.notification_id = data["notification_id"]
        var jsonObject = JSONObject(data["payload"])
        notificationsModel.second_user_id = jsonObject.getString("second_user_id")
        notificationsModel.image = jsonObject.getString("image")
        notificationsModel.profile_image = jsonObject.getString("profile_image")
        notificationsModel.amount = jsonObject.getString("amount")
        notificationsModel.user_id = jsonObject.getString("user_id")
        notificationsModel.item_id = jsonObject.getString("item_id")
        notificationsModel.last_name = jsonObject.getString("last_name")
        notificationsModel.title = jsonObject.getString("title")
        notificationsModel.first_name = jsonObject.getString("first_name")
        notificationsModel.badge = Integer.parseInt(data["badge"])
        notificationsModel.message = data["message"]
        Log.e("Notification==>", notificationsModel.toString())
        return notificationsModel
    }

    fun getRandomNumer(): Int {
        val r = Random()
        return r.nextInt(80 - 65) + 65
    }
}
