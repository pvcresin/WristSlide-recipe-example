package com.pvcresin.wristslide

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable


open class WristSlideReceiver(context: Context): MessageApi.MessageListener {

    var TAG = "WristSlide Receiver 49"
    var debug = false

    var moving = false
    var pressed = false

    private var MESSAGE = "/message"

    private lateinit var mGoogleApiClient: GoogleApiClient

    init {
        mGoogleApiClient = GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        Log.d(TAG, "Google Api Client connected")

                        Wearable.MessageApi.addListener(mGoogleApiClient, this@WristSlideReceiver)
                    }
                    override fun onConnectionSuspended(i: Int) {    }
                }).build()
    }

    open fun onDown() {    }
    open fun onUp() {    }

    open fun onStartMoving() {    }
    open fun onMove(dx: Short, dy: Short) {    }
    open fun onStopMoving() {    }

    fun connect() {
        if (!mGoogleApiClient.isConnected || !mGoogleApiClient.isConnecting)
            mGoogleApiClient.connect()
    }
    fun disconnect() {
        if (mGoogleApiClient.isConnected || mGoogleApiClient.isConnecting)
            mGoogleApiClient.disconnect()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {// move,10,20
        if (messageEvent.path == MESSAGE) {

            val msg = String(messageEvent.data)

            val data = msg.split(",")

            if (debug) Log.d(TAG, "receive: " + msg)

            when (data[0]) {
                "down" -> {
                    pressed = true
                    onDown()
                }

                "up" -> {
                    pressed = false
                    onUp()
                }

                "start" -> {
                    moving = true
                    onStartMoving()
                }

//                "move" -> onMove((data[1].toShort() * speed).toShort(),
//                        (data[2].toShort() * speed).toShort())

                "move" -> onMove(data[1].toShort(), data[2].toShort())

                "stop" -> {
                    moving = false
                    onStopMoving()
                }
            }
        }
    }
}