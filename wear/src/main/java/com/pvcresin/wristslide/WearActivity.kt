package com.pvcresin.wristslide

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.ViewPager
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager

class WearActivity : WearableActivity() {

    private lateinit var wristSlideReceiver: WristSlideReceiver

    internal var TAG = "WearActivity"

    internal lateinit var mGestureDetector: GestureDetector

    val initPos = 200f
    var px = initPos
    var py = initPos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAmbientEnabled()

        // https://cookpad.com/recipe/4273918
        // https://cookpad.com/recipe/4270954
        // https://cookpad.com/recipe/4271989

        val adapter = CustomPagerAdapter(this)
        adapter.add(Recipe(R.mipmap.dish0,
                "バターチキンカレー",
                "甘辛さが絶妙です！",
                listOf("鳥もも肉", "玉ねぎ大", "バター", "トマトケチャップ", "お水", "カレールウ"),
                listOf("300g", "1個", "20g", "大さじ1", "700cc", "1/2箱"),
                listOf("1. お鍋にバターを入れ溶かす。1㎝幅にスライスした玉ねぎを入れ、あめ色近くになるまでよく炒める。",
                        "2. 一口大に切った鳥もも肉を入れ、玉ねぎと一緒に炒める。",
                        "3. お水を入れて沸騰してきたらケチャップを入れ、20分ほどコトコト煮込む。",
                        "4. 市販ルウを入れてルウが溶けてきて、トロミがついたら完成です。"))
        ).add(Recipe(R.mipmap.dish1,
                "甘み野菜のポークカレー",
                "甘みたっぷり野菜たっぷりの本格カレーです♪",
                listOf("1st ing", "2nd ing"),
                listOf("10mg", "20mg"),
                listOf("2. 一口大に切った鳥もも肉を入れ\n　玉ねぎと一緒に炒める",
                        "2nd step"))
        ).add(Recipe(R.mipmap.dish2,
                "嫌いな野菜も食べやすく↑↑",
                "パプリカしめじカレー",
                listOf("1st ing", "2nd ing"),
                listOf("10mg", "20mg"),
                listOf("1. step", "2. step"))
        )

        // ViewPager を生成
        val viewPager = ViewPager(this)
        viewPager.adapter = adapter

        setContentView(viewPager)


        fun init() {
            px = initPos
            py = initPos
        }

        init()

        wristSlideReceiver = object : WristSlideReceiver(this) {
            override fun onStartMoving() {
                val t = SystemClock.uptimeMillis()
                dispatchTouchEvent(
                        MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, px, py, 0))
            }
            override fun onMove(dx: Short, dy: Short) {
                px += dx
                py += dy
                val t = SystemClock.uptimeMillis()
                dispatchTouchEvent(
                        MotionEvent.obtain(t, t, MotionEvent.ACTION_MOVE, px, py, 0))
            }
            override fun onStopMoving() {
                val t = SystemClock.uptimeMillis()
                dispatchTouchEvent(
                        MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, px, py, 0))
                px = initPos
                py = initPos
            }
        }.apply { debug = true }

        mGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                Log.d(TAG, "long tap")
                init()
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                Log.d(TAG, "double tap")
                finish()
                return super.onDoubleTap(e)
            }
        })

        // back light always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        wristSlideReceiver.connect()
    }

    override fun onPause() {
        wristSlideReceiver.disconnect()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
