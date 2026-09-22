package org.openedx.app.system.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.braze.push.BrazeFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import org.openedx.app.AppActivity
import org.openedx.app.R
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences

class OpenEdXFirebaseMessagingService : FirebaseMessagingService() {

    private val preferences: CorePreferences by inject()
    private val config: Config by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (BrazeFirebaseMessagingService.handleBrazeRemoteMessage(this, message)) {
            // This Remote Message originated from Braze and a push notification was displayed.
            // No further action is needed.
            return
        } else {
            // This Remote Message did not originate from Braze.
            handlePushNotification(message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferences.pushToken = token
        if (preferences.user != null) {
            SyncFirebaseTokenWorker.schedule(this)
        }
    }

    private fun handlePushNotification(message: RemoteMessage) {
        val data = message.data
        val notification = message.notification

        val title = notification?.title
            ?: data["title"]
            ?: data["subject"]
            ?: config.getPlatformName()

        val body = notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: data["text"]

        if (body.isNullOrBlank() && notification == null) {
            return
        }

        val intent = Intent(this, AppActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (k, v) ->
                putExtra(k, v)
            }
        }

        val code = createId()
        val pendingIntent = PendingIntent.getActivity(
            this,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "${config.getPlatformName()}_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body ?: "")
            )
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                config.getPlatformName(),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Course and platform notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(code, notificationBuilder.build())
    }

    private fun createId(): Int {
        return SystemClock.uptimeMillis().toInt()
    }
}
