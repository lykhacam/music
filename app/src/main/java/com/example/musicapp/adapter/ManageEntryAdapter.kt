package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ManageItem // ✅ dùng model đúng

class ManageEntryAdapter(
    private val items: MutableList<ManageItem>,
    private val onSave: (ManageItem) -> Unit,
    private val onDelete: (ManageItem) -> Unit
) : RecyclerView.Adapter<ManageEntryAdapter.ManageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_entry, parent, false)
        return ManageViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ManageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ManageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvId: TextView = view.findViewById(R.id.tvId)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val etId: EditText = view.findViewById(R.id.etEditId)
        private val etName: EditText = view.findViewById(R.id.etEditName)
        private val btnSave: ImageButton = view.findViewById(R.id.btnSave)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        private val layoutViewMode: View = view.findViewById(R.id.layoutViewMode)
        private val layoutEditMode: View = view.findViewById(R.id.layoutEditMode)

        fun bind(item: ManageItem) {
            tvId.text = "ID: ${item.id}"
            tvName.text = "Tên: ${item.name}"
            etId.setText(item.id)
            etName.setText(item.name)

            layoutViewMode.visibility = View.VISIBLE
            layoutEditMode.visibility = View.GONE

            layoutViewMode.setOnClickListener {
                layoutViewMode.visibility = View.GONE
                layoutEditMode.visibility = View.VISIBLE
            }

            btnSave.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val newId = etId.text.toString().trim()
                    val newName = etName.text.toString().trim()
                    if (newId.isNotBlank() && newName.isNotBlank()) {
                        val updatedItem = ManageItem(newId, newName)
                        items[pos] = updatedItem
                        onSave(updatedItem)
                        notifyItemChanged(pos)
                    }
                }
            }

            btnDelete.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val removedItem = items[pos]
                    onDelete(removedItem)
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }
    }
}
