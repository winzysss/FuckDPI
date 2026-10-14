package io.github.winzysss.fuckdpi.utility

import android.content.Context
import androidx.core.content.edit
import io.github.winzysss.fuckdpi.data.Command

object PresetUtils {

    private const val SEEDED_KEY = "presets_seeded_v1"

    private val PRESETS = listOf(
        "TR Genel" to "-d1 -s1+s -d3+s -s6+s -a1",
        "TR TTL-Fake" to "-f-1 -t5 -a1 -At,r,s -d1",
        "TR TTL3" to "-f-1 -t3 -a1",
        "TR Discord/UDP" to "-Ku -a2 -An -f-1 -t5 -a1 -At,r,s -d1",
        "TR OOB" to "-o1 -a1 -r-5+se",
        "TR Agresif" to "-d1 -d3+s -s6+s -d9+s -s12+s -d15+s -r1+s -a1",
    )

    fun seedIfNeeded(context: Context): Boolean {
        val prefs = context.getPreferences()
        if (prefs.getBoolean(SEEDED_KEY, false)) return false

        val history = HistoryUtils(context)
        val existing = history.getHistory().map { it.text }.toMutableSet()
        val merged = history.getHistory().toMutableList()

        for ((name, cmd) in PRESETS.asReversed()) {
            if (existing.add(cmd)) {
                merged.add(0, Command(cmd, pinned = true, name = name))
            }
        }

        history.saveHistory(merged)
        prefs.edit { putBoolean(SEEDED_KEY, true) }
        return true
    }
}
