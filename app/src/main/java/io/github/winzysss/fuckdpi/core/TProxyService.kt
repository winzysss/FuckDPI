package io.github.winzysss.fuckdpi.core

object TProxyService {
    init {
        System.loadLibrary("desync")
    }

    external fun startTunnel(config: String, fd: Int): Int

    external fun stopTunnel()
}
