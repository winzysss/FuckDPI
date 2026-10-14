package io.github.winzysss.fuckdpi.core

class DesyncProxy {
    companion object {
        init {
            System.loadLibrary("desync")
        }
    }

    fun startProxy(args: Array<String>): Int = jniStartProxy(args)

    fun stopProxy(): Int {
        return jniStopProxy()
    }

    private external fun jniStartProxy(args: Array<String>): Int
    private external fun jniStopProxy(): Int
}
