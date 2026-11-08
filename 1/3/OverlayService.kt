package com.example.twolines

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        const val ACTION_CLOSE = "close"
    }

    private lateinit var wm: WindowManager
    private var line1: View? = null
    private var line2: View? = null
    private var panel: View? = null
    private var p1: WindowManager.LayoutParams? = null
    private var p2: WindowManager.LayoutParams? = null
    private var linesVisible = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> { startAsForeground(); showAll() }
            ACTION_STOP -> hideLines()
            ACTION_CLOSE -> { removeAll(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        }
        return START_NOT_STICKY
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("lines", "Two Lines", NotificationManager.IMPORTANCE_LOW)
        )
        val n = Notification.Builder(this, "lines")
            .setContentTitle("الخطان يعملان")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(1, n)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun linePrams(x: Int) = WindowManager.LayoutParams(
        dp(3), WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        // NOT_TOUCHABLE: touches pass through to the app below
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x }

    private fun showAll() {
        if (panel == null) buildPanel()
        if (!linesVisible) showLines()
    }

    private fun showLines() {
        val w = resources.displayMetrics.widthPixels
        p1 = linePrams(w / 3); p2 = linePrams(w * 2 / 3)
        line1 = View(this).apply { setBackgroundColor(Color.RED) }
        line2 = View(this).apply { setBackgroundColor(Color.CYAN) }
        wm.addView(line1, p1); wm.addView(line2, p2)
        linesVisible = true
    }

    private fun hideLines() {
        line1?.let { wm.removeView(it) }; line2?.let { wm.removeView(it) }
        line1 = null; line2 = null; linesVisible = false
    }

    private fun move(line: View?, p: WindowManager.LayoutParams?, dx: Int) {
        if (line == null || p == null) return
        val max = resources.displayMetrics.widthPixels
        p.x = (p.x + dx).coerceIn(0, max)
        wm.updateViewLayout(line, p)
    }

    private fun buildPanel() {
        val step = dp(6)
        fun b(t: String, f: () -> Unit) = Button(this).apply {
            text = t; setOnClickListener { f() }
        }
        fun row(vararg v: View) = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            v.forEach { addView(it) }
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xCC000000.toInt())
            addView(row(
                b("◀ 1") { move(line1, p1, -step) }, b("1 ▶") { move(line1, p1, step) },
                b("◀ 2") { move(line2, p2, -step) }, b("2 ▶") { move(line2, p2, step) }
            ))
            addView(row(
                b("بدء") { if (!linesVisible) showLines() },
                b("إيقاف") { hideLines() },
                b("إغلاق") {
                    removeAll(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
                }
            ))
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = dp(48) }
        wm.addView(box, lp)
        panel = box
    }

    private fun removeAll() {
        hideLines()
        panel?.let { wm.removeView(it) }; panel = null
    }

    override fun onDestroy() { removeAll(); super.onDestroy() }
}
