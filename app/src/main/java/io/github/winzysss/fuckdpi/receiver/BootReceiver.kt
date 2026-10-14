package io.github.winzysss.fuckdpi.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.winzysss.fuckdpi.R
import io.github.winzysss.fuckdpi.activities.MainActivity
import io.github.winzysss.fuckdpi.data.Mode
import io.github.winzysss.fuckdpi.data.START_ACTION
import io.github.winzysss.fuckdpi.services.ServiceManager
import io.github.winzysss.fuckdpi.utility.PermissionUtils
import io.github.winzysss.fuckdpi.utility.getPreferences
import io.github.winzysss.fuckdpi.utility.mode
import io.github.winzysss.fuckdpi.utility.registerNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
        private const val ALERT_CHANNEL = "fuckdpi_alerts"
        private const val ALERT_NOTIFICATION_ID = 5
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") {
            return
        }

        val preferences = context.getPreferences()
        if (!preferences.getBoolean("autostart", false)) return

        val mode = preferences.mode()
        if (mode == Mode.VPN && VpnService.prepare(context) != null) {
            notifyVpnConsentRequired(context)
            return
        }
        val pending = goAsync()

        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                ServiceManager.handleAction(context.applicationContext, START_ACTION, mode)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service after boot", e)
            } finally {
                pending.finish()
            }
        }
    }

    private fun notifyVpnConsentRequired(context: Context) {
        if (!PermissionUtils.hasNotificationPermission(context)) return
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        registerNotificationChannel(context, ALERT_CHANNEL, R.string.notification_title)

        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_REQUEST_CONNECT, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.vpn_permission_denied))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(ALERT_NOTIFICATION_ID, notification)
    }
}
