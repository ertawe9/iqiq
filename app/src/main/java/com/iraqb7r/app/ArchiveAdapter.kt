package com.iraqb7r.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveAdapter(
    private var items: List<CartItem>,
    private val onRestore: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<ArchiveAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.archiveName)
        val info: TextView = view.findViewById(R.id.archiveInfo)
        val restoreBtn: View = view.findViewById(R.id.archiveRestoreBtn)
        val deleteBtn: View = view.findViewById(R.id.archiveDeleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_archive, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.allianceName
        holder.info.text = "السبب: ${item.archiveReason}"
        holder.restoreBtn.setOnClickListener { onRestore(item) }
        holder.deleteBtn.setOnClickListener { onDelete(item) }
    }

    fun updateData(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
