package com.pvcresin.wristslide

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager


class MainActivity : AppCompatActivity() {

    lateinit var wristSlideSender: WristSlideSender

    var alreadyLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wristSlideSender = WristSlideSender(this).apply { debug = true }

        // back light always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        wristSlideSender.connect()
    }

    override fun onPause() {
        Log.d("wrist", "pause")
        wristSlideSender.disconnect()
        alreadyLaunched = false
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
