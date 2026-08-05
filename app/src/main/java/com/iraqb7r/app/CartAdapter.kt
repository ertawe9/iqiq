package com.iraqb7r.app

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartItem>,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tags: TextView = view.findViewById(R.id.itemTags)
        val name: TextView = view.findViewById(R.id.itemName)
        val arrival: TextView = view.findViewById(R.id.itemArrival)
        val countdown: TextView = view.findViewById(R.id.itemCountdown)
        val delete: TextView = view.findViewById(R.id.itemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val typeLabel = when (item.cartType) {
            "skill" -> "مهارة"
            "accessory" -> "إكسسوار"
            else -> "عتاد"
        }
        holder.tags.text = "#${position + 1} · $typeLabel · العدد: ${item.count}"
        holder.name.text = item.allianceName
        holder.arrival.text = "يكتمل: ${formatClock(item.timestamp)}"
        updateCountdown(holder, item)
        holder.delete.setOnClickListener { onDelete(item) }
    }

    fun updateCountdown(holder: VH, item: CartItem) {
        val left = item.timestamp - System.currentTimeMillis()
        holder.countdown.text = "⏳ المتبقي: ${CartEngine.formatDuration(left)}"
    }

    private fun formatClock(ts: Long): String {
        val fmt = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale("ar"))
        return fmt.format(java.util.Date(ts))
    }

    fun updateData(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    /** يحدث فقط نصوص العدّ التنازلي المرئية بدون إعادة رسم كامل القائمة */
    fun tickVisible(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            val holder = recyclerView.getChildViewHolder(child) as? VH ?: continue
            val pos = holder.bindingAdapterPosition
            if (pos in items.indices) updateCountdown(holder, items[pos])
        }
    }

    override fun getItemCount(): Int = items.size
}
