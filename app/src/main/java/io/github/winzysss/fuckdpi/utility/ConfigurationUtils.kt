package io.github.winzysss.fuckdpi.utility

import android.content.Context
import io.github.winzysss.fuckdpi.core.EngineCmdPreferences
import io.github.winzysss.fuckdpi.data.Configuration

fun testConfiguration(context: Context, command: String, host: String, port: Int): Configuration {
    val args = EngineCmdPreferences(context, command, host, port).args.toList()
    val (commandHost, commandPort) = args.checkIpAndPortInArgs()
    return Configuration(args, commandHost ?: host, commandPort?.toInt() ?: port)
}
