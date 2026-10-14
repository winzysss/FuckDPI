package io.github.winzysss.fuckdpi.services

import io.github.winzysss.fuckdpi.data.AppStatus
import io.github.winzysss.fuckdpi.data.Mode

var appStatus = AppStatus.Halted to Mode.VPN
    private set

fun setStatus(status: AppStatus, mode: Mode) {
    appStatus = status to mode
}
