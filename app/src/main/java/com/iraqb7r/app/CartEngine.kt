package com.iraqb7r.app

import android.content.Context

data class NearestInfo(val name: String, val remainingMs: Long, val critical: Boolean)

object CartEngine {

    /**
     * ينفذ منطق العد التنازلي: يرسل تنبيه "قاربت على الانتهاء" قبل دقيقة،
     * وينقل العربات المنتهية (منذ أكثر من 3 ثوانٍ) إلى الأرشيف.
     * يرجع أقرب عربة نشطة (أو null إذا ما فيه عربات).
     */
    fun tick(ctx: Context): NearestInfo? {
        val now = System.currentTimeMillis()
        var carts = DataStore.getCarts(ctx)
        var changed = false

        carts.forEach { c ->
            val left = c.timestamp - now
            if (left in 1..60000 && !c.notifiedWarning) {
                c.notifiedWarning = true
                NotificationHelper.showAlert(ctx, "⏰ اقتربت عربة من الانتهاء", "عربة \"${c.allianceName}\" بتخلص خلال دقيقة")
                changed = true
            }
            if (left <= 0 && !c.notifiedDone) {
                c.notifiedDone = true
                NotificationHelper.showAlert(ctx, "✅ انتهت عربة", "اكتملت عربة \"${c.allianceName}\"")
                changed = true
            }
        }

        val stillActive = mutableListOf<CartItem>()
        val toArchive = mutableListOf<CartItem>()
        carts.forEach { c ->
            if (c.timestamp - now <= -3000) {
                c.archivedAt = now
                c.archiveReason = "مكتملة"
                toArchive.add(c)
            } else {
                stillActive.add(c)
            }
        }
        if (toArchive.isNotEmpty()) {
            val archive = DataStore.getArchive(ctx)
            archive.addAll(0, toArchive)
            DataStore.saveArchive(ctx, archive)
            carts = stillActive
            changed = true
        }

        if (changed) DataStore.saveCarts(ctx, carts)

        val upcoming = carts.filter { it.timestamp - now > 0 }.minByOrNull { it.timestamp }
            ?: return null
        val remaining = upcoming.timestamp - now
        return NearestInfo(upcoming.allianceName, remaining, remaining <= 30000)
    }

    fun formatDuration(ms: Long): String {
        val safe = if (ms < 0) 0 else ms
        val h = safe / 3600000
        val m = (safe % 3600000) / 60000
        val s = (safe % 60000) / 1000
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
