package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemPlaylistBinding // Sử dụng lại layout cũ
import com.example.myapplication.model.Category

class CategoryAdapter(
    private var categoryList: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun setData(newList: List<Category>) {
        categoryList = newList
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.playlistName.isSelected = true
            binding.playlistName.text = category.name
            binding.playlistDesc.text = category.description

            Glide.with(binding.root.context)
                .load(category.image)
                .into(binding.playlistImage)

            binding.root.setOnClickListener {
                onClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int = categoryList.size
}
