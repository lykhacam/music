// ExploreFragment.kt
package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S3Activity
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.databinding.FragmentExploreBinding
import com.example.myapplication.model.Category
import com.example.myapplication.viewmodel.CategoryViewModel

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            openPlaylist(category)
        }

        binding.playListRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.setData(categories)
        }
    }

    private fun openPlaylist(category: Category) {
        val intent = Intent(requireContext(), S3Activity::class.java).apply {
            putExtra("category_id", category.id)
            putExtra("category_image", category.image)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
