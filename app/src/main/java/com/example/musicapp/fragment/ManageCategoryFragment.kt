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

        adapter = ManageEntryAdapter(
            itemList,
            onSave = { item ->
                db.child(item.firebaseKey).setValue(item) // ✅ đúng key (c5, c6...)
                val index = itemList.indexOfFirst { it.firebaseKey == item.firebaseKey }
                if (index != -1) {
                    itemList[index] = item.copy()
                    adapter.notifyItemChanged(index)
                }
                Toast.makeText(context, "Đã lưu ${item.name}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { item ->
                db.child(item.id).removeValue().addOnSuccessListener {
                    val index = itemList.indexOfFirst { it.id == item.id }
                    if (index in itemList.indices) {
                        itemList.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    } else {
                        // fallback nếu index không hợp lệ
                        adapter.notifyDataSetChanged()
                    }
                    Toast.makeText(context, "Đã xoá ${item.name}", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, "Xoá thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val inputId = binding.etId.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            if (inputId.isEmpty() || name.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ ID và tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateNextCategoryId { generatedKey ->
                val newItem = ManageItem(id = inputId, name = name)
                db.child(generatedKey).setValue(newItem)
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)

                binding.etId.setText("")
                binding.etName.setText("")
                Toast.makeText(context, "Đã thêm $name với mã $inputId", Toast.LENGTH_SHORT).show()
            }
        }


        loadCategories()
    }

    private fun loadCategories() {
        db.get().addOnSuccessListener { snapshot ->
            itemList.clear()
            snapshot.children.forEach {
                val item = it.getValue(ManageItem::class.java)
                val key = it.key
                if (item != null && key != null) {
                    item.firebaseKey = key  // Gán khóa Firebase cho mỗi item
                    itemList.add(item)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    private fun generateNextCategoryId(onIdGenerated: (String) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val ids = snapshot.children.mapNotNull {
                it.key?.removePrefix("c")?.toIntOrNull()
            }
            val nextNumber = (ids.maxOrNull() ?: 0) + 1
            val newId = "c$nextNumber"
            onIdGenerated(newId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
