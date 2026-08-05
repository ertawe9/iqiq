package com.iraqb7r.app

import org.json.JSONArray
import org.json.JSONObject

data class CartItem(
    var id: Int,
    var allianceName: String,
    var cartType: String,      // "gear" | "skill" | "accessory"
    var count: Int,            // 1 or 2
    var timestamp: Long,       // وقت الاكتمال (millis)
    var totalDuration: Long,   // المدة الكلية بالـ millis (لشريط التقدم)
    var notifiedWarning: Boolean = false,
    var notifiedDone: Boolean = false,
    var archivedAt: Long = 0L,
    var archiveReason: String = ""
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("id", id)
        o.put("allianceName", allianceName)
        o.put("cartType", cartType)
        o.put("count", count)
        o.put("timestamp", timestamp)
        o.put("totalDuration", totalDuration)
        o.put("notifiedWarning", notifiedWarning)
        o.put("notifiedDone", notifiedDone)
        o.put("archivedAt", archivedAt)
        o.put("archiveReason", archiveReason)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): CartItem {
            return CartItem(
                id = o.optInt("id"),
                allianceName = o.optString("allianceName"),
                cartType = o.optString("cartType", "gear"),
                count = o.optInt("count", 1),
                timestamp = o.optLong("timestamp"),
                totalDuration = o.optLong("totalDuration"),
                notifiedWarning = o.optBoolean("notifiedWarning", false),
                notifiedDone = o.optBoolean("notifiedDone", false),
                archivedAt = o.optLong("archivedAt", 0L),
                archiveReason = o.optString("archiveReason", "")
            )
        }

        fun listToJsonArray(list: List<CartItem>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJsonArray(json: String?): MutableList<CartItem> {
            val result = mutableListOf<CartItem>()
            if (json.isNullOrBlank()) return result
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    result.add(fromJson(arr.getJSONObject(i)))
                }
            } catch (e: Exception) { /* تجاهل بيانات تالفة */ }
            return result
        }
    }
}
