package com.example.twolines

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        fun btn(text: String, onClick: () -> Unit) = Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
            root.addView(this)
        }

        btn("بدء") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "فعّل إذن الظهور فوق التطبيقات", Toast.LENGTH_LONG).show()
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            } else send(OverlayService.ACTION_START)
        }
        btn("إيقاف") { send(OverlayService.ACTION_STOP) }
        btn("إغلاق التطبيق") {
            send(OverlayService.ACTION_CLOSE)
            finishAndRemoveTask()
        }
        setContentView(root)
    }

    private fun send(action: String) {
        val i = Intent(this, OverlayService::class.java).setAction(action)
        if (action == OverlayService.ACTION_START) startForegroundService(i) else startService(i)
    }
}
