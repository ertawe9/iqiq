package com.iraqb7r.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.ensureChannels(this)
        requestNotifPermissionIfNeeded()

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    inner class AndroidBridge {

        @JavascriptInterface
        fun saveCarts(json: String) {
            val arr = JSONArray(json)
            val list = mutableListOf<CartItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    CartItem(
                        id = o.optInt("id"),
                        allianceName = o.optString("name"),
                        cartType = o.optString("cartType", "gear"),
                        count = o.optInt("skillsCount", 1),
                        timestamp = o.optLong("timestamp"),
                        totalDuration = o.optLong("totalDuration"),
                        notifiedDone = o.optBoolean("notifiedDone", false)
                    )
                )
            }
            DataStore.saveCarts(this@MainActivity, list)
        }

        @JavascriptInterface
        fun requestOverlayPermission() {
            runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "الصلاحية مفعّلة أصلاً ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun setWidgetEnabled(enabled: Boolean) {
            runOnUiThread {
                val canDraw = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)
                if (enabled && !canDraw) {
                    Toast.makeText(this@MainActivity, "فعّل صلاحية العرض فوق التطبيقات أولاً", Toast.LENGTH_LONG).show()
                    requestOverlayPermission()
                    return@runOnUiThread
                }
                val intent = Intent(this@MainActivity, OverlayService::class.java)
                if (enabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
                } else {
                    stopService(intent)
                }
                DataStore.setWidgetEnabled(this@MainActivity, enabled)
            }
        }

        @JavascriptInterface
        fun isWidgetEnabled(): Boolean = DataStore.isWidgetEnabled(this@MainActivity)

        @JavascriptInterface
        fun canDrawOverlays(): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)
    }
}
