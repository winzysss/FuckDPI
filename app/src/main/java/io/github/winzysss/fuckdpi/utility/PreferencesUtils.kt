package io.github.winzysss.fuckdpi.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.winzysss.fuckdpi.data.Mode

private const val DEFAULT_CMD_ARGS = "-o1 -a1 -r-5+se"

val PreferenceFragmentCompat.sharedPreferences
    get() = preferenceScreen.sharedPreferences

fun Context.getPreferences(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(this).also(::migrateLegacyPreferenceKeys)

private const val LEGACY_MIGRATION_FLAG = "engine_keys_migrated"
private val legacyKeyPrefixes = listOf("byedpi_", "bye_dpi_")

private fun migrateLegacyPreferenceKeys(prefs: SharedPreferences) {
    if (prefs.getBoolean(LEGACY_MIGRATION_FLAG, false)) return

    val legacy = prefs.all.filterKeys { key ->
        legacyKeyPrefixes.any { key.startsWith(it) }
    }
    if (legacy.isNotEmpty()) {
        prefs.edit {
            legacy.forEach { (key, value) ->
                val suffix = legacyKeyPrefixes
                    .first { key.startsWith(it) }
                    .let { key.removePrefix(it) }
                val newKey = "engine_$suffix"
                if (newKey == "engine_command_history" && value is Set<*>) {
                    val commands = value.filterIsInstance<String>()
                        .map { io.github.winzysss.fuckdpi.data.Command(it) }
                    putString(newKey, com.google.gson.Gson().toJson(commands))
                } else when (value) {
                    is Boolean -> putBoolean(newKey, value)
                    is String -> putString(newKey, value)
                    is Number -> putString(newKey, value.toString())
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        putStringSet(newKey, value.filterIsInstance<String>().toSet())
                    }
                }
                remove(key)
            }
            putBoolean(LEGACY_MIGRATION_FLAG, true)
        }
    } else {
        prefs.edit { putBoolean(LEGACY_MIGRATION_FLAG, true) }
    }
}

fun String.toCurrentPreferenceKey(): String =
    legacyKeyPrefixes.firstOrNull { startsWith(it) }
        ?.let { "engine_" + removePrefix(it) }
        ?: this

fun SharedPreferences.getIntStringNotNull(key: String, defValue: Int): Int =
    getString(key, defValue.toString())?.toIntOrNull() ?: defValue

fun SharedPreferences.getLongStringNotNull(key: String, defValue: Long): Long =
    getString(key, defValue.toString())?.toLongOrNull() ?: defValue

fun SharedPreferences.getStringNotNull(key: String, defValue: String): String =
    getString(key, defValue) ?: defValue

fun SharedPreferences.mode(): Mode =
    Mode.fromString(getStringNotNull("engine_mode", "vpn"))

fun <T : Preference> PreferenceFragmentCompat.findPreferenceNotNull(key: CharSequence): T =
    findPreference(key) ?: throw IllegalStateException("Preference $key not found")

fun SharedPreferences.getSelectedApps(): List<String> {
    return getStringSet("selected_apps", emptySet())?.toList() ?: emptyList()
}

fun SharedPreferences.getCmdEnable(): Boolean {
    return getBoolean("engine_enable_cmd_settings", true)
}

fun SharedPreferences.getCmdArgs(): String {
    return getStringNotNull("engine_cmd_args", DEFAULT_CMD_ARGS)
}

fun SharedPreferences.checkIpAndPortInCmd(): Pair<String?, String?> {
    if (!getCmdEnable()) return Pair(null, null)
    return shellSplit(getCmdArgs()).checkIpAndPortInArgs()
}

fun List<String>.checkIpAndPortInArgs(): Pair<String?, String?> {
    val flags = setOf(
        "--daemon", "--no-domain", "--no-ipv6", "--no-udp", "--help", "--version",
        "--transparent", "--tfo", "--md5sig", "--wait-send", "--drop-sack",
    )
    var host: String? = null
    var port: String? = null
    var index = 0
    while (index < size) {
        val arg = this[index++]
        if (arg == "--") break
        if (arg.startsWith("--")) {
            val key = arg.substringBefore('=')
            if (flags.any { it.startsWith(key) }) continue
            val value = if ('=' in arg) arg.substringAfter('=') else getOrNull(index++)
            when (key) {
                "--ip" -> host = value
                "--po", "--por", "--port" -> port = value
            }
        } else if (arg.startsWith("-")) {
            for (position in 1 until arg.length) {
                val key = arg[position]
                if (key in "DNXUhvEFSZY") continue
                val value = arg.substring(position + 1).ifEmpty { getOrNull(index++) }
                when (key) {
                    'i' -> host = value
                    'p' -> port = value
                }
                break
            }
        }
    }
    return host to port
}


fun SharedPreferences.getProxyIpAndPort(): Pair<String, String> {
    val (cmdIp, cmdPort) = checkIpAndPortInCmd()

    val ip = cmdIp ?: getStringNotNull("engine_proxy_ip", "127.0.0.1")
    val port = cmdPort ?: getStringNotNull("engine_proxy_port", "1080")

    return Pair(ip, port)
}
