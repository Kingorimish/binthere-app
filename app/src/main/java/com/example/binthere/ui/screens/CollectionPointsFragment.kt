package com.example.binthere.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.binthere.data.local.entities.CollectionPointEntity
import com.example.binthere.databinding.FragmentListScreenBinding
import com.example.binthere.databinding.ItemRecordCardBinding
import com.example.binthere.ui.viewmodel.WasteViewModel
import kotlinx.coroutines.launch

class CollectionPointsFragment : Fragment() {

    private var _binding: FragmentListScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WasteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvScreenTitle.text = "Nairobi Collection Points"
        binding.btnAddNew.visibility = View.GONE

        val adapter = CollectionPointsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.collectionPoints.collect { points ->
                    adapter.submitList(points)
                    binding.tvEmptyState.visibility =
                        if (points.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvEmptyState.text = "Loading Nairobi drop-off hubs..."
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CollectionPointsAdapter :
    RecyclerView.Adapter<CollectionPointsAdapter.PointViewHolder>() {

    private var items: List<CollectionPointEntity> = emptyList()

    fun submitList(newItems: List<CollectionPointEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class PointViewHolder(val binding: ItemRecordCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val binding = ItemRecordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCardTitle.text = "${item.pointName} (${item.zone})"
        holder.binding.tvCardSubtitle.text =
            "Hours: ${item.operatingHours}"
        holder.binding.tvCardBadge.text = "COUNTY DESIGNATED DROP-OFF"
        holder.binding.layoutCardActions.visibility = View.GONE
    }

    override fun getItemCount(): Int = items.size
}