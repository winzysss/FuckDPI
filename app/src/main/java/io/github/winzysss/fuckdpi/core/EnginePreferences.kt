package io.github.winzysss.fuckdpi.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.winzysss.fuckdpi.data.UISettings
import io.github.winzysss.fuckdpi.utility.DomainListUtils
import io.github.winzysss.fuckdpi.utility.checkIpAndPortInArgs
import io.github.winzysss.fuckdpi.utility.getCmdArgs
import io.github.winzysss.fuckdpi.utility.getCmdEnable
import io.github.winzysss.fuckdpi.utility.getStringNotNull
import io.github.winzysss.fuckdpi.utility.shellSplit

sealed interface EnginePreferences {
    companion object {
        fun fromSharedPreferences(preferences: SharedPreferences, context: Context): EnginePreferences =
            when (preferences.getCmdEnable()) {
                true -> EngineCmdPreferences(preferences, context)
                false -> EngineUiPreferences(preferences)
            }
    }
}

class EngineCmdPreferences(val args: Array<String>) : EnginePreferences {
    companion object {
        private fun parseCmdToArguments(context: Context, command: String, host: String, port: String): Array<String> {
            val blacklist = setOf("--help", "--version", "-h", "-v")
            val args = shellSplit(getLists(command, context))
                .dropWhile { !it.startsWith("-") }
                .filter { it !in blacklist }
            val (cmdIp, cmdPort) = args.checkIpAndPortInArgs()
            val prefix = buildList {
                if (cmdIp == null) addAll(listOf("--ip", host))
                if (cmdPort == null) addAll(listOf("--port", port))
            }

            Log.d("ProxyPref", "CMD: ${args.joinToString(" ")}")
            Log.d("ProxyPref", "Added from settings: ${prefix.joinToString(" ")}")

            return (listOf("desync") + prefix + args).toTypedArray()
        }

        private fun getLists(cmd: String, context: Context): String {
            return Regex("""\{list:([^}]+)\}""").replace(cmd) { matchResult ->
                val listName = matchResult.groupValues[1].trim()
                val lists = DomainListUtils.getLists(context)
                val domainList = lists.find { it.name.equals(listName, ignoreCase = true) }

                if (domainList != null && domainList.domains.isNotEmpty()) {
                    domainList.domains.joinToString(" ")
                } else {
                    Log.w("ProxyPref", "List '$listName' not found or empty")
                    ""
                }
            }
        }
    }

    constructor(preferences: SharedPreferences, context: Context) : this(
        parseCmdToArguments(
            context,
            preferences.getCmdArgs(),
            preferences.getStringNotNull("engine_proxy_ip", "127.0.0.1"),
            preferences.getStringNotNull("engine_proxy_port", "1080"),
        )
    )

    constructor(context: Context, command: String, host: String, port: Int) : this(
        parseCmdToArguments(context, command, host, port.toString())
    )
}

class EngineUiPreferences(val settings: UISettings = UISettings()) : EnginePreferences {

    constructor(preferences: SharedPreferences) : this(
        UISettings.fromSharedPreferences(preferences)
    )

    val uiargs: Array<String>
        get() {
            val args = mutableListOf("desync")

            if (settings.ip.isNotEmpty()) args.add("-i${settings.ip}")
            if (settings.port != 0) args.add("-p${settings.port}")
            if (settings.maxConnections != 0) args.add("-c${settings.maxConnections}")
            if (settings.bufferSize != 0) args.add("-b${settings.bufferSize}")

            val protocols = buildList {
                if (settings.desyncHttps) add("t")
                if (settings.desyncHttp) add("h")
            }

            if (!settings.hosts.isNullOrBlank()) {
                val hostStr = ":${settings.hosts.replace("\n", " ")}"
                when (settings.hostsMode) {
                    UISettings.HostsMode.Blacklist -> {
                        args.add("-H$hostStr")
                        args.add("-An")
                        if (protocols.isNotEmpty()) args.add("-K${protocols.joinToString(",")}")
                    }
                    UISettings.HostsMode.Whitelist -> {
                        if (protocols.isNotEmpty()) args.add("-K${protocols.joinToString(",")}")
                        args.add("-H$hostStr")
                    }
                    else -> {}
                }
            } else {
                if (protocols.isNotEmpty()) args.add("-K${protocols.joinToString(",")}")
            }

            if (settings.defaultTtl != 0) args.add("-g${settings.defaultTtl}")
            if (settings.noDomain) args.add("-N")

            if (settings.splitPosition != 0) {
                val posArg = settings.splitPosition.toString() + if (settings.splitAtHost) "+h" else ""
                val option = when (settings.desyncMethod) {
                    UISettings.DesyncMethod.Split -> "-s"
                    UISettings.DesyncMethod.Disorder -> "-d"
                    UISettings.DesyncMethod.OOB -> "-o"
                    UISettings.DesyncMethod.DISOOB -> "-q"
                    UISettings.DesyncMethod.Fake -> "-f"
                    UISettings.DesyncMethod.None -> ""
                }
                if (option.isNotEmpty()) args.add("$option$posArg")
            }

            if (settings.desyncMethod == UISettings.DesyncMethod.Fake) {
                if (settings.fakeTtl != 0) args.add("-t${settings.fakeTtl}")
                if (settings.fakeSni.isNotEmpty()) args.add("-n${settings.fakeSni}")
                if (settings.fakeOffset != 0) args.add("-O${settings.fakeOffset}")
            }

            if (settings.desyncMethod == UISettings.DesyncMethod.OOB || settings.desyncMethod == UISettings.DesyncMethod.DISOOB) {
                val oobByte = (settings.oobChar.firstOrNull() ?: 'a').code.toByte()
                args.add("-e$oobByte")
            }

            val modHttpFlags = buildList {
                if (settings.hostMixedCase) add("h")
                if (settings.domainMixedCase) add("d")
                if (settings.hostRemoveSpaces) add("r")
            }

            if (modHttpFlags.isNotEmpty()) args.add("-M${modHttpFlags.joinToString(",")}")

            if (settings.tlsRecordSplit && settings.tlsRecordSplitPosition != 0) {
                val tlsRecArg = settings.tlsRecordSplitPosition.toString() + if (settings.tlsRecordSplitAtSni) "+s" else ""
                args.add("-r$tlsRecArg")
            }

            if (settings.tcpFastOpen) args.add("-F")
            if (settings.dropSack) args.add("-Y")

            args.add("-An")

            if (settings.desyncUdp) {
                args.add("-Ku")
                if (settings.udpFakeCount != 0) args.add("-a${settings.udpFakeCount}")
                args.add("-An")
            }

            Log.d("ProxyPref", "UI to cmd: ${args.joinToString(" ")}")
            return args.toTypedArray()
        }
}
