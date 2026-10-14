package io.github.winzysss.fuckdpi.data

import io.github.winzysss.fuckdpi.BuildConfig

const val STARTED_BROADCAST = "${BuildConfig.APPLICATION_ID}.STARTED"
const val STOPPED_BROADCAST = "${BuildConfig.APPLICATION_ID}.STOPPED"
const val FAILED_BROADCAST = "${BuildConfig.APPLICATION_ID}.FAILED"

const val SENDER = "sender"

enum class Sender(val senderName: String) {
    Proxy("Proxy"),
    VPN("VPN")
}