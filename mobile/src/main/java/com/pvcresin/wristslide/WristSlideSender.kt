package com.pvcresin.wristslide

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Long
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer


// http://techblog.qoncept.jp/?p=356
open class WristSlideSender(private val context: Context) {

    var TAG = "WristSlide 50"
    var debug = false
    var MESSAGE = "/message"
    private var mGoogleApiClient  = GoogleApiClient.Builder(context)
            .addApi(Wearable.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    Log.d(TAG, "Google Api Client connected")
                }
                override fun onConnectionSuspended(i: Int) {    }
            }).build()
    private lateinit var nodes: Collection<String>

    private var isAlive = false
    private var moving = false
    private var timeoutCount = 0
    private var sumRawDx = 0
    private var sumRawDy = 0

    var leftHand = true
    var speed = 0.2f


    // open -> override in your code
    open fun onDown() {  sendMessage("down")  }
    open fun onUp() {  sendMessage("up")  }

    open fun onStartMoving() {  sendMessage("start")  }
    open fun onMove(dx: Short, dy: Short) {  sendMessage("move,$dx,$dy")  }
    open fun onStopMoving() {  sendMessage("stop")  }

    // basic method
    fun connect() {
        mGoogleApiClient.connect()
        thread {
            nodes = getConnectedNodes()
            Log.d(TAG, "node connected. nodes.size = ${nodes.size}")
        }
        isAlive = true

        detectMouseMove("adb", "shell", "getevent", "-lt") // thread start

        setSendTimer()
    }

    fun disconnect() {
        isAlive = false
        if (mGoogleApiClient.isConnected) mGoogleApiClient.disconnect()
    }

    fun sendMessage(str: String) {
        thread {
            val data = str.toByteArray()
            nodes.forEach {
                val result = Wearable.MessageApi
                        .sendMessage(mGoogleApiClient, it, MESSAGE, data).await()
                if (!result.status.isSuccess) {
                    Log.e(TAG, "ERROR: failed to send Message: " + result.status)
                }
            }

//            try {
//                Log.d(TAG, "Ip: $remoteIp, Port: $remotePort")
//                val dp = DatagramPacket(data, data.size, InetAddress.getByName(remoteIp), remotePort)
//                val ds = DatagramSocket()
//                ds.send(dp)
//                ds.close()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }

            if (debug) Log.d(TAG, "send: $str")
        }
    }

    fun changeHand() {
        leftHand = !leftHand
        Log.d(TAG, "hand changed: " + if (leftHand) "left" else "right")
    }
    fun changeSpeed(newSpeed: Float) {
        speed = newSpeed
        Log.d(TAG, "speed changed: $speed")
    }

    private fun getConnectedNodes(): Collection<String> {
        val results = HashSet<String>()
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().nodes.forEach {
            results.add(it.id)
        }
        return results
    }

    private fun detectMouseMove(vararg command: String) {
        val processBuilder = ProcessBuilder(*command)
        val process = try {
            processBuilder.start()
        } catch (e: Exception) {
            Log.d(TAG, "process build is failed")
            return
        }

        val iStream = process.inputStream
        val isReader = InputStreamReader(iStream)
        val bufferedReader = BufferedReader(isReader)

        fun killProcess() {
            try {
                process.errorStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                process.inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                process.outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            process.destroy()
            isAlive = false
            Log.d(TAG, "kill process")
        }

        thread {
            while (isAlive) {
                var line: String?
                try {
                    line = bufferedReader.readLine()    // length = 72

                    if (line == null) {   // ends this thread
                        killProcess()
                        Log.d(TAG, "stream is null")
                        return@thread
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isAlive = false
                    killProcess()
                    Log.d(TAG, "read line exception")
                    return@thread
                }

                // split a series of spaces
                val results = line.split(" +".toRegex()).dropLastWhile { it == "" }.toTypedArray()

//                Log.d("results", "" + results.size)

                val baseNum = 4
                if (results.size >= baseNum + 1) {
                    when (results[baseNum]) {
                        "BTN_LEFT" -> {
                            if (results[baseNum + 1] == "DOWN") onDown()
                            else onUp()
                        }
                        "REL_X", "REL_Y" -> {
                            timeoutCount = 0

                            val dif = java.lang.Long.parseLong(results[baseNum + 1], 16).toInt()

                            if (results[baseNum] == "REL_X") sumRawDx += dif
                            else sumRawDy += dif

                        }
                    }
                }
            } // while
        } // thread

        Log.d(TAG, "thread started")
        Toast.makeText(context, "started", Toast.LENGTH_SHORT).show()
    }

    private fun setSendTimer() {
        timer(period = 100) {
            if (!isAlive) { // stream is dead
                this.cancel()
                Log.d(TAG, "not isAlive -> mouse move listener canceled")
            }

            if (sumRawDy != 0 || sumRawDy != 0) {   // moving
                if (!moving) {
                    moving = true
                    onStartMoving()
                }
                if (moving) {
                    val direction = if (leftHand) 1 else -1
                    onMove((sumRawDx * speed * direction).toShort(),
                            (sumRawDy * speed * direction).toShort())
                    sumRawDx = 0
                    sumRawDy = 0
                }
            } else {    // not moving
                timeoutCount++

                val timeoutCountMax = 3

                if (moving && timeoutCount >= timeoutCountMax) { // time passes -> released
                    moving = false
                    onStopMoving()
                }
            }
        }
        Log.d(TAG, "set the send timer")
    }

}
