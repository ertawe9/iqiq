package com.iraqb7r.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // عناصر تبويب العربات
    private lateinit var liveClock: TextView
    private lateinit var nearestCard: android.view.View
    private lateinit var nearestName: TextView
    private lateinit var nearestTime: TextView
    private lateinit var statGear: TextView
    private lateinit var statSkill: TextView
    private lateinit var statTotal: TextView
    private lateinit var inputAllianceName: EditText
    private lateinit var inputTimeValue: EditText
    private lateinit var inputSearch: EditText
    private lateinit var btnTypeAccessory: Button
    private lateinit var btnTypeGear: Button
    private lateinit var btnTypeSkill: Button
    private lateinit var btnCount1: Button
    private lateinit var btnCount2: Button
    private lateinit var btnUnitHour: Button
    private lateinit var btnUnitMinute: Button
    private lateinit var btnCalc: Button
    private lateinit var emptyCartsMsg: TextView
    private lateinit var cartsRecycler: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    // عناصر تبويب الأرشيف
    private lateinit var archiveRecycler: RecyclerView
    private lateinit var archiveAdapter: ArchiveAdapter
    private lateinit var emptyArchiveMsg: TextView
    private lateinit var btnClearArchive: Button

    // عناصر تبويب الإعدادات
    private lateinit var switchLightMode: Switch
    private lateinit var btnEnableOverlayPermission: Button
    private lateinit var btnToggleWidget: Button
    private lateinit var btnResetApp: Button

    private var selectedType = "gear"
    private var selectedCount = 1
    private var selectedUnit = "minute"

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tickRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.ensureChannels(this)
        requestNotifPermissionIfNeeded()

        bindViews()
        setupBottomNav()
        setupCarsTab()
        setupArchiveTab()
        setupSettingsTab()

        applyAccentColor(DataStore.getAccent(this))
        refreshCarts()
        refreshArchive()
        updateStats()
        updateWidgetButtonLabel()

        startTicking()
    }

    private fun bindViews() {
        liveClock = findViewById(R.id.liveClock)
        nearestCard = findViewById(R.id.nearestCard)
        nearestName = findViewById(R.id.nearestName)
        nearestTime = findViewById(R.id.nearestTime)
        statGear = findViewById(R.id.statGear)
        statSkill = findViewById(R.id.statSkill)
        statTotal = findViewById(R.id.statTotal)
        inputAllianceName = findViewById(R.id.inputAllianceName)
        inputTimeValue = findViewById(R.id.inputTimeValue)
        inputSearch = findViewById(R.id.inputSearch)
        btnTypeAccessory = findViewById(R.id.btnTypeAccessory)
        btnTypeGear = findViewById(R.id.btnTypeGear)
        btnTypeSkill = findViewById(R.id.btnTypeSkill)
        btnCount1 = findViewById(R.id.btnCount1)
        btnCount2 = findViewById(R.id.btnCount2)
        btnUnitHour = findViewById(R.id.btnUnitHour)
        btnUnitMinute = findViewById(R.id.btnUnitMinute)
        btnCalc = findViewById(R.id.btnCalc)
        emptyCartsMsg = findViewById(R.id.emptyCartsMsg)
        cartsRecycler = findViewById(R.id.cartsRecycler)

        archiveRecycler = findViewById(R.id.archiveRecycler)
        emptyArchiveMsg = findViewById(R.id.emptyArchiveMsg)
        btnClearArchive = findViewById(R.id.btnClearArchive)

        switchLightMode = findViewById(R.id.switchLightMode)
        btnEnableOverlayPermission = findViewById(R.id.btnEnableOverlayPermission)
        btnToggleWidget = findViewById(R.id.btnToggleWidget)
        btnResetApp = findViewById(R.id.btnResetApp)
    }

    private fun setupBottomNav() {
        val viewCars = findViewById<android.view.View>(R.id.viewCars)
        val viewArchive = findViewById<android.view.View>(R.id.viewArchive)
        val viewSettings = findViewById<android.view.View>(R.id.viewSettings)
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            viewCars.visibility = android.view.View.GONE
            viewArchive.visibility = android.view.View.GONE
            viewSettings.visibility = android.view.View.GONE
            when (item.itemId) {
                R.id.nav_cars -> viewCars.visibility = android.view.View.VISIBLE
                R.id.nav_archive -> { viewArchive.visibility = android.view.View.VISIBLE; refreshArchive() }
                R.id.nav_settings -> viewSettings.visibility = android.view.View.VISIBLE
            }
            true
        }
    }

    /* ============================ تبويب العربات ============================ */

    private fun setupCarsTab() {
        cartAdapter = CartAdapter(emptyList()) { item -> confirmDeleteCart(item) }
        cartsRecycler.layoutManager = LinearLayoutManager(this)
        cartsRecycler.adapter = cartAdapter

        selectTypeButton(btnTypeGear)
        selectCountButton(btnCount1)
        selectUnitButton(btnUnitMinute)

        btnTypeAccessory.setOnClickListener { selectedType = "accessory"; selectTypeButton(btnTypeAccessory) }
        btnTypeGear.setOnClickListener { selectedType = "gear"; selectTypeButton(btnTypeGear) }
        btnTypeSkill.setOnClickListener { selectedType = "skill"; selectTypeButton(btnTypeSkill) }

        btnCount1.setOnClickListener { selectedCount = 1; selectCountButton(btnCount1) }
        btnCount2.setOnClickListener { selectedCount = 2; selectCountButton(btnCount2) }

        btnUnitHour.setOnClickListener { selectedUnit = "hour"; selectUnitButton(btnUnitHour) }
        btnUnitMinute.setOnClickListener { selectedUnit = "minute"; selectUnitButton(btnUnitMinute) }

        btnCalc.setOnClickListener { addCart() }

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshCarts() }
        })
    }

    private fun selectTypeButton(selected: Button) {
        val accent = AccentTheme.colorFor(this, DataStore.getAccent(this))
        listOf(btnTypeAccessory, btnTypeGear, btnTypeSkill).forEach {
            it.background = getDrawable(R.drawable.bg_option_off)
            it.setTextColor(getColor(R.color.text_main_dark))
        }
        selected.background = getDrawable(R.drawable.bg_button_rounded)
        selected.background.setTint(accent)
        selected.setTextColor(getColor(R.color.bg_dark))
    }

    private fun selectCountButton(selected: Button) {
        val accent = AccentTheme.colorFor(this, DataStore.getAccent(this))
        listOf(btnCount1, btnCount2).forEach {
            it.background = getDrawable(R.drawable.bg_option_off)
            it.setTextColor(getColor(R.color.text_main_dark))
        }
        selected.background = getDrawable(R.drawable.bg_button_rounded)
        selected.background.setTint(accent)
        selected.setTextColor(getColor(R.color.bg_dark))
    }

    private fun selectUnitButton(selected: Button) {
        val accent = AccentTheme.colorFor(this, DataStore.getAccent(this))
        listOf(btnUnitHour, btnUnitMinute).forEach {
            it.background = getDrawable(R.drawable.bg_option_off)
            it.setTextColor(getColor(R.color.text_main_dark))
        }
        selected.background = getDrawable(R.drawable.bg_button_rounded)
        selected.background.setTint(accent)
        selected.setTextColor(getColor(R.color.bg_dark))
    }

    private fun addCart() {
        val name = inputAllianceName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "اكتب اسم التحالف أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        val value = inputTimeValue.text.toString().toIntOrNull()
        if (value == null || value <= 0) {
            Toast.makeText(this, "اكتب وقت صحيح", Toast.LENGTH_SHORT).show()
            return
        }
        val durationMs = if (selectedUnit == "hour") value * 3600000L else value * 60000L
        val now = System.currentTimeMillis()

        val cart = CartItem(
            id = DataStore.nextId(this),
            allianceName = name,
            cartType = selectedType,
            count = selectedCount,
            timestamp = now + durationMs,
            totalDuration = durationMs
        )
        val list = DataStore.getCarts(this)
        list.add(cart)
        DataStore.saveCarts(this, list)

        inputAllianceName.setText("")
        refreshCarts()
        updateStats()
        Toast.makeText(this, "✅ تمت إضافة العربة", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDeleteCart(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("تأكيد حذف العربة")
            .setMessage("سيتم نقل هذه العربة إلى الأرشيف. متابعة؟")
            .setPositiveButton("حذف") { _, _ ->
                val list = DataStore.getCarts(this).filter { it.id != item.id }
                DataStore.saveCarts(this, list)
                item.archivedAt = System.currentTimeMillis()
                item.archiveReason = "محذوفة يدوياً"
                val archive = DataStore.getArchive(this)
                archive.add(0, item)
                DataStore.saveArchive(this, archive)
                refreshCarts(); refreshArchive(); updateStats()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun refreshCarts() {
        val query = inputSearch.text?.toString()?.trim().orEmpty()
        var list = DataStore.getCarts(this)
        if (query.isNotEmpty()) {
            list = list.filter { it.allianceName.contains(query, ignoreCase = true) }.toMutableList()
        }
        list = list.sortedBy { it.timestamp }.toMutableList()
        cartAdapter.updateData(list)
        emptyCartsMsg.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        cartsRecycler.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun updateStats() {
        val list = DataStore.getCarts(this)
        statTotal.text = list.size.toString()
        statSkill.text = list.count { it.cartType == "skill" }.toString()
        statGear.text = list.count { it.cartType == "gear" }.toString()
    }

    /* ============================ تبويب الأرشيف ============================ */

    private fun setupArchiveTab() {
        archiveAdapter = ArchiveAdapter(emptyList(),
            onRestore = { item -> restoreFromArchive(item) },
            onDelete = { item -> deleteFromArchive(item) })
        archiveRecycler.layoutManager = LinearLayoutManager(this)
        archiveRecycler.adapter = archiveAdapter

        btnClearArchive.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("مسح الأرشيف")
                .setMessage("سيتم حذف كل العناصر بالأرشيف نهائياً. متابعة؟")
                .setPositiveButton("مسح") { _, _ ->
                    DataStore.saveArchive(this, emptyList())
                    refreshArchive()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    private fun restoreFromArchive(item: CartItem) {
        val archive = DataStore.getArchive(this).filter { it.id != item.id }
        DataStore.saveArchive(this, archive)
        val list = DataStore.getCarts(this)
        item.notifiedDone = false
        item.notifiedWarning = false
        list.add(item)
        DataStore.saveCarts(this, list)
        refreshArchive(); refreshCarts(); updateStats()
        Toast.makeText(this, "↩️ تمت الاستعادة", Toast.LENGTH_SHORT).show()
    }

    private fun deleteFromArchive(item: CartItem) {
        val archive = DataStore.getArchive(this).filter { it.id != item.id }
        DataStore.saveArchive(this, archive)
        refreshArchive()
    }

    private fun refreshArchive() {
        val list = DataStore.getArchive(this)
        archiveAdapter.updateData(list)
        emptyArchiveMsg.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        archiveRecycler.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    /* ============================ تبويب الإعدادات ============================ */

    private fun setupSettingsTab() {
        switchLightMode.isChecked = DataStore.isLightTheme(this)
        switchLightMode.setOnCheckedChangeListener { _, checked ->
            DataStore.setLightTheme(this, checked)
            Toast.makeText(this, "سيتم تطبيق الثيم بالكامل — أعد فتح التطبيق لأفضل نتيجة", Toast.LENGTH_LONG).show()
        }

        findViewById<ImageButton>(R.id.swatchGold).setOnClickListener { setAccent("gold") }
        findViewById<ImageButton>(R.id.swatchRed).setOnClickListener { setAccent("red") }
        findViewById<ImageButton>(R.id.swatchBlue).setOnClickListener { setAccent("blue") }
        findViewById<ImageButton>(R.id.swatchGreen).setOnClickListener { setAccent("green") }
        findViewById<ImageButton>(R.id.swatchPurple).setOnClickListener { setAccent("purple") }

        btnEnableOverlayPermission.setOnClickListener { requestOverlayPermission() }
        btnToggleWidget.setOnClickListener { toggleWidget() }

        btnResetApp.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("إعادة ضبط البرنامج بالكامل")
                .setMessage("سيتم حذف كل البيانات نهائياً. متابعة؟")
                .setPositiveButton("إعادة ضبط") { _, _ ->
                    stopService(Intent(this, OverlayService::class.java))
                    DataStore.resetAll(this)
                    refreshCarts(); refreshArchive(); updateStats(); updateWidgetButtonLabel()
                    applyAccentColor(DataStore.getAccent(this))
                    switchLightMode.isChecked = false
                    Toast.makeText(this, "تمت إعادة الضبط", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    private fun setAccent(accent: String) {
        DataStore.setAccent(this, accent)
        applyAccentColor(accent)
    }

    private fun applyAccentColor(accent: String) {
        val color = AccentTheme.colorFor(this, accent)
        findViewById<TextView>(R.id.headerTitle).setTextColor(color)
        findViewById<TextView>(R.id.liveClockLabel).setTextColor(color)
        findViewById<TextView>(R.id.nearestName).let { /* اسم بلون النص الأساسي، يبقى كما هو */ }
        nearestTime.setTextColor(color)
        btnCalc.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        btnToggleWidget.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        // إعادة رسم أزرار الاختيار المفعّلة حالياً بنفس اللون الجديد
        selectTypeButton(when (selectedType) { "accessory" -> btnTypeAccessory; "skill" -> btnTypeSkill; else -> btnTypeGear })
        selectCountButton(if (selectedCount == 2) btnCount2 else btnCount1)
        selectUnitButton(if (selectedUnit == "hour") btnUnitHour else btnUnitMinute)
    }

    /* ============================ صلاحية العرض فوق التطبيقات + تشغيل الويدجت ============================ */

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        } else {
            Toast.makeText(this, "الصلاحية مفعّلة أصلاً ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleWidget() {
        val canDraw = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        if (!canDraw) {
            Toast.makeText(this, "فعّل صلاحية العرض فوق التطبيقات أولاً", Toast.LENGTH_LONG).show()
            requestOverlayPermission()
            return
        }
        val running = DataStore.isWidgetEnabled(this)
        val intent = Intent(this, OverlayService::class.java)
        if (running) {
            stopService(intent)
            DataStore.setWidgetEnabled(this, false)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
            DataStore.setWidgetEnabled(this, true)
        }
        updateWidgetButtonLabel()
    }

    private fun updateWidgetButtonLabel() {
        btnToggleWidget.text = if (DataStore.isWidgetEnabled(this))
            getString(R.string.stop_widget) else getString(R.string.start_widget)
    }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /* ============================ العد التنازلي كل ثانية ============================ */

    private fun startTicking() {
        tickRunnable = object : Runnable {
            override fun run() {
                tick()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(tickRunnable)
    }

    private fun tick() {
        val fmt = SimpleDateFormat("hh:mm:ss a", Locale("ar"))
        liveClock.text = fmt.format(Date())

        val nearest = CartEngine.tick(this)
        if (nearest == null) {
            nearestCard.visibility = android.view.View.GONE
        } else {
            nearestCard.visibility = android.view.View.VISIBLE
            nearestName.text = nearest.name
            nearestTime.text = CartEngine.formatDuration(nearest.remainingMs)
            nearestTime.setTextColor(
                if (nearest.critical) getColor(R.color.danger)
                else AccentTheme.colorFor(this, DataStore.getAccent(this))
            )
        }
        // تحديث القوائم والإحصائيات بشكل خفيف كل ثانية
        cartAdapter.tickVisible(cartsRecycler)
        updateStats()

        // إذا صار أرشفة تلقائية (انتهت عربة)، حدّث القائمتين كامل كل بضع ثواني
        if (System.currentTimeMillis() % 3000 < 1000) {
            refreshCarts()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tickRunnable)
    }
}
