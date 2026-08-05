package com.iraqb7r.app

import android.content.Context

data class NearestInfo(val id: Int, val name: String, val cartType: String, val remainingMs: Long, val critical: Boolean)

object CartEngine {

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
        return NearestInfo(upcoming.id, upcoming.allianceName, upcoming.cartType, remaining, remaining <= 30000)
    }

    fun typeLabel(type: String): String = when (type) {
        "skill" -> "مهارة"
        "accessory" -> "إكسسوار"
        else -> "عتاد"
    }

    fun removeCartById(ctx: Context, id: Int) {
        val carts = DataStore.getCarts(ctx)
        val target = carts.find { it.id == id } ?: return
        val remaining = carts.filter { it.id != id }
        DataStore.saveCarts(ctx, remaining)
        target.archivedAt = System.currentTimeMillis()
        target.archiveReason = "حُذفت من الويدجت العائم"
        val archive = DataStore.getArchive(ctx)
        archive.add(0, target)
        DataStore.saveArchive(ctx, archive)
    }

    fun formatDuration(ms: Long): String {
        val safe = if (ms < 0) 0 else ms
        val h = safe / 3600000
        val m = (safe % 3600000) / 60000
        val s = (safe % 60000) / 1000
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
