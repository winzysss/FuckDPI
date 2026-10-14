package io.github.winzysss.fuckdpi.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.winzysss.fuckdpi.BuildConfig
import io.github.winzysss.fuckdpi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    private const val API_URL =
        "https://api.github.com/repos/winzysss/FuckDPI/releases/latest"
    private const val USER_AGENT = "FuckDPI-Android"
    private const val APK_MIME = "application/vnd.android.package-archive"
    private const val CONNECT_TIMEOUT = 10_000
    private const val READ_TIMEOUT = 20_000
    private const val LAST_PROMPTED_KEY = "update_last_prompted"
    private const val LAST_CHECK_KEY = "update_last_check"
    private const val CHECK_INTERVAL_MS = 6L * 60 * 60 * 1000

    private var pendingApk: File? = null

    class Release(
        @SerializedName("tag_name") val tagName: String? = null,
        @SerializedName("body") val body: String? = null,
        @SerializedName("assets") val assets: List<Asset>? = null,
        @SerializedName("draft") val draft: Boolean = false,
        @SerializedName("prerelease") val prerelease: Boolean = false,
    ) {
        class Asset(
            @SerializedName("name") val name: String? = null,
            @SerializedName("browser_download_url") val url: String? = null,
        )
    }

    private fun Activity.isDead() = isFinishing || isDestroyed

    private fun toast(activity: Activity, resId: Int) {
        if (!activity.isDead()) {
            Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndPrompt(activity: Activity, scope: LifecycleCoroutineScope, manual: Boolean) {
        if (!manual) {
            val prefs = activity.getPreferences()
            val last = prefs.getLong(LAST_CHECK_KEY, 0)
            if (System.currentTimeMillis() - last < CHECK_INTERVAL_MS) return
        }
        scope.launch(Dispatchers.IO) {
            val release = fetchLatest()
            val tag = release?.tagName
            if (release == null || tag == null) {
                if (manual) withContext(Dispatchers.Main) {
                    toast(activity, R.string.update_check_failed)
                }
                return@launch
            }

            if (!manual) {
                activity.getPreferences().edit {
                    putLong(LAST_CHECK_KEY, System.currentTimeMillis())
                }
            }
            if (release.draft || release.prerelease) return@launch

            if (!isNewer(tag)) {
                if (manual) withContext(Dispatchers.Main) {
                    toast(activity, R.string.update_none)
                }
                return@launch
            }

            val lastPrompted = activity.getPreferences()
                .getString(LAST_PROMPTED_KEY, null)
            if (!manual && tag == lastPrompted) return@launch

            withContext(Dispatchers.Main) {
                if (!activity.isDead()) showUpdateDialog(activity, scope, release, tag)
            }
        }
    }

    private fun showUpdateDialog(
        activity: Activity,
        scope: LifecycleCoroutineScope,
        release: Release,
        tag: String,
    ) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.update_available, normalizeTag(tag)))
            .setMessage(release.body?.take(600))
            .setPositiveButton(R.string.update_download) { _, _ ->
                downloadAndInstall(activity, scope, release)
            }
            .setNeutralButton(R.string.update_skip) { _, _ ->
                activity.getPreferences().edit {
                    putString(LAST_PROMPTED_KEY, tag)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun downloadAndInstall(
        activity: Activity,
        scope: LifecycleCoroutineScope,
        release: Release,
    ) {
        val asset = pickAsset(release)
        if (asset == null) {
            toast(activity, R.string.update_download_failed)
            return
        }

        val padding = (24 * activity.resources.displayMetrics.density).toInt()
        val progress = ProgressBar(activity).apply { isIndeterminate = true }
        val container = FrameLayout(activity).apply {
            setPadding(padding, padding, padding, padding)
            addView(progress, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            ))
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.update_downloading)
            .setView(container)
            .setCancelable(false)
            .create()
        dialog.show()

        scope.launch(Dispatchers.IO) {
            val file = try {
                download(activity, asset)
            } catch (e: Exception) {
                null
            }

            withContext(Dispatchers.Main) {
                if (dialog.isShowing) dialog.dismiss()
                if (activity.isDead()) return@withContext

                if (file == null) {
                    toast(activity, R.string.update_download_failed)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    !activity.packageManager.canRequestPackageInstalls()
                ) {
                    pendingApk = file
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.update_install_permission)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            activity.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${activity.packageName}")
                                )
                            )
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                } else {
                    install(activity, file)
                }
            }
        }
    }

    private fun fetchLatest(): Release? = try {
        val conn = URL(API_URL).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = CONNECT_TIMEOUT
            conn.readTimeout = READ_TIMEOUT
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", USER_AGENT)
            if (conn.responseCode != HttpURLConnection.HTTP_OK) null
            else conn.inputStream.bufferedReader().use {
                Gson().fromJson(it, Release::class.java)
            }
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }

    private fun normalizeTag(tag: String) = tag.trim().trimStart('v', 'V', '.')

    fun isNewer(tagName: String, current: String = BuildConfig.VERSION_NAME): Boolean {
        fun parse(version: String): List<Int> =
            version.trim().trimStart('v', 'V', '.').substringBefore('-')
                .split('.').map { it.toIntOrNull() ?: 0 }

        val latest = parse(tagName)
        val installed = parse(current)
        for (i in 0 until maxOf(latest.size, installed.size)) {
            val a = latest.getOrElse(i) { 0 }
            val b = installed.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    private fun pickAsset(release: Release): Release.Asset? {
        val apks = release.assets.orEmpty().filter {
            it.name?.endsWith(".apk") == true && it.url != null
        }
        for (abi in Build.SUPPORTED_ABIS) {
            apks.firstOrNull { it.name?.contains("-$abi-") == true }?.let { return it }
        }
        return apks.firstOrNull { it.name?.contains("universal") == true }
    }

    private fun download(context: Context, asset: Release.Asset): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val out = File(dir, "update.apk")
        val conn = URL(asset.url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = CONNECT_TIMEOUT
            conn.readTimeout = READ_TIMEOUT
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", USER_AGENT)
            if (conn.responseCode !in 200..299) {
                throw java.io.IOException("HTTP ${conn.responseCode}")
            }
            conn.inputStream.use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
        } finally {
            conn.disconnect()
        }
        return out
    }

    fun installPending(activity: Activity) {
        val file = pendingApk ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            activity.packageManager.canRequestPackageInstalls()
        ) {
            pendingApk = null
            install(activity, file)
        }
    }

    private fun install(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            file.delete()
        }
    }
}
