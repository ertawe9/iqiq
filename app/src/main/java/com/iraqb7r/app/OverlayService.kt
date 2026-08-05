package com.iraqb7r.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tickRunnable: Runnable
    private var currentCartId: Int? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        startForeground(NotificationHelper.WIDGET_NOTIF_ID, NotificationHelper.buildForegroundNotification(this, "الويدجت العائم يعمل الآن"))
        addOverlayView()
        startTicking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun addOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = View.inflate(this, R.layout.overlay_widget, null)

        applyAccentBorder()

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 200

        makeDraggable(overlayView!!, params)

        overlayView?.findViewById<View>(R.id.overlayCloseBtn)?.setOnClickListener {
            currentCartId?.let { id -> CartEngine.removeCartById(this, id) }
            updateOverlayContent()
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun applyAccentBorder() {
        val accentColor = AccentTheme.colorFor(this, DataStore.getAccent(this))
        val root = overlayView?.findViewById<View>(R.id.overlayRoot)
        (root?.background?.mutate() as? GradientDrawable)?.setStroke(
            (1.5f * resources.displayMetrics.density).toInt(), accentColor
        )
        overlayView?.findViewById<TextView>(R.id.overlayLabel)?.setTextColor(accentColor)
        overlayView?.findViewById<TextView>(R.id.overlayTime)?.setTextColor(accentColor)
    }

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams) {
        val dragHandle = view.findViewById<View>(R.id.overlayRoot)
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f

        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager?.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun startTicking() {
        tickRunnable = object : Runnable {
            override fun run() {
                updateOverlayContent()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(tickRunnable)
    }

    private fun updateOverlayContent() {
        val nearest = CartEngine.tick(this)
        val labelView = overlayView?.findViewById<TextView>(R.id.overlayLabel)
        val nameView = overlayView?.findViewById<TextView>(R.id.overlayName)
        val timeView = overlayView?.findViewById<TextView>(R.id.overlayTime)

        if (nearest == null) {
            currentCartId = null
            labelView?.text = "—"
            nameView?.text = "لا توجد عربات نشطة"
            timeView?.text = "--:--:--"
            timeView?.setTextColor(AccentTheme.colorFor(this, DataStore.getAccent(this)))
            return
        }
        currentCartId = nearest.id
        labelView?.text = CartEngine.typeLabel(nearest.cartType)
        nameView?.text = nearest.name
        timeView?.text = CartEngine.formatDuration(nearest.remainingMs)

        val color = if (nearest.critical)
            ContextCompat.getColor(this, R.color.danger)
        else
            AccentTheme.colorFor(this, DataStore.getAccent(this))
        timeView?.setTextColor(color)
        labelView?.setTextColor(color)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tickRunnable)
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) { /* تجاهل */ }
        DataStore.setWidgetEnabled(this, false)
    }
}
