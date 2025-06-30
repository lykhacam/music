package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ManageEntryAdapter
import com.example.myapplication.databinding.FragmentManageEntryBinding
import com.example.myapplication.model.ManageItem
import com.google.firebase.database.FirebaseDatabase

class ManageCategoryFragment : Fragment() {

    private var _binding: FragmentManageEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ManageEntryAdapter
    private val itemList = mutableListOf<ManageItem>()
    private val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("categories")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ManageEntryAdapter(itemList,
            onSave = { item ->
                db.child(item.id).setValue(item) // 👈 Lưu full object thay vì chỉ name
                val index = itemList.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    itemList[index] = item.copy()
                    adapter.notifyItemChanged(index)
                }
                Toast.makeText(context, "Đã lưu ${item.name}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { item ->
                db.child(item.id).removeValue()
                val index = itemList.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    itemList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
                Toast.makeText(context, "Đã xoá ${item.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val id = binding.etId.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            if (id.isNotEmpty() && name.isNotEmpty()) {
                val newItem = ManageItem(id, name)
                db.child(id).setValue(newItem) // 👈 Lưu cả id + name vào object
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)
                binding.etId.setText("")
                binding.etName.setText("")
            } else {
                Toast.makeText(context, "Vui lòng nhập đầy đủ ID và tên", Toast.LENGTH_SHORT).show()
            }
        }

        loadCategories()
    }

    private fun loadCategories() {
        db.get().addOnSuccessListener { snapshot ->
            itemList.clear()
            snapshot.children.forEach {
                val item = it.getValue(ManageItem::class.java)
                if (item != null) {
                    itemList.add(item)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}