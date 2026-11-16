package com.example.binthere.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.binthere.data.local.entities.PickupScheduleEntity
import com.example.binthere.databinding.FragmentListScreenBinding
import com.example.binthere.databinding.ItemRecordCardBinding
import com.example.binthere.ui.viewmodel.WasteViewModel
import com.example.binthere.worker.PickupReminderWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SchedulesFragment : Fragment() {

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

        binding.tvScreenTitle.text = "Waste Pickup Schedules"
        binding.btnAddNew.text = "+ Schedule Pickup"

        val adapter = SchedulesAdapter(
            onRemindNow = { schedule -> enqueueReminderNotification(schedule, 5L) },
            onDelete = { schedule -> viewModel.deleteSchedule(schedule) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnAddNew.setOnClickListener {
            showScheduleDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.schedules.collect { list ->
                        adapter.submitList(list)
                        binding.tvEmptyState.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                        binding.tvEmptyState.text =
                            "No pickups scheduled yet. Tap '+ Schedule Pickup' to coordinate a community or county collection."
                    }
                }
                launch {
                    viewModel.uiMessage.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun enqueueReminderNotification(schedule: PickupScheduleEntity, delaySeconds: Long) {
        val inputData = Data.Builder()
            .putString("zone", schedule.zone)
            .putString("collectionPoint", schedule.collectionPoint)
            .putString("pickupTime", schedule.pickupTime)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PickupReminderWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)
        Toast.makeText(
            requireContext(),
            "Reminder queued! Notification will trigger in ${delaySeconds}s.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showScheduleDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etZone = EditText(requireContext()).apply {
            hint = "Nairobi Zone (e.g. Kilimani, South B, CBD)"
        }
        val etPoint = EditText(requireContext()).apply {
            hint = "Collection Point (e.g. Gate B Skip)"
        }
        val etDate = EditText(requireContext()).apply {
            hint = "Pickup Date (e.g. 2026-11-20)"
        }
        val etTime = EditText(requireContext()).apply {
            hint = "Pickup Time (e.g. 08:30 AM)"
        }
        val etNotes = EditText(requireContext()).apply {
            hint = "Notes (optional, e.g. Separate plastics)"
        }

        val roles = arrayOf("Community Organizer", "County Official", "Resident Association")
        val spinnerRole = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        }

        container.addView(etZone)
        container.addView(etPoint)
        container.addView(etDate)
        container.addView(etTime)
        container.addView(spinnerRole)
        container.addView(etNotes)

        AlertDialog.Builder(requireContext())
            .setTitle("Schedule Waste Pickup")
            .setView(container)
            .setPositiveButton("Save & Set Reminder") { _, _ ->
                viewModel.submitPickupSchedule(
                    zone = etZone.text.toString(),
                    collectionPoint = etPoint.text.toString(),
                    pickupDate = etDate.text.toString(),
                    pickupTime = etTime.text.toString(),
                    organizerRole = spinnerRole.selectedItem.toString(),
                    notes = etNotes.text.toString()
                ) { createdSchedule ->
                    enqueueReminderNotification(createdSchedule, 10L)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SchedulesAdapter(
    private val onRemindNow: (PickupScheduleEntity) -> Unit,
    private val onDelete: (PickupScheduleEntity) -> Unit
) : RecyclerView.Adapter<SchedulesAdapter.ScheduleViewHolder>() {

    private var items: List<PickupScheduleEntity> = emptyList()

    fun submitList(newItems: List<PickupScheduleEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ScheduleViewHolder(val binding: ItemRecordCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemRecordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCardTitle.text = "${item.zone} • ${item.collectionPoint}"
        holder.binding.tvCardSubtitle.text =
            "When: ${item.pickupDate} at ${item.pickupTime}\nOrganizer: ${item.organizerRole}\nNotes: ${item.notes}"
        holder.binding.tvCardBadge.text = "SCHEDULED PICKUP"

        holder.binding.btnActionPrimary.text = "Test Reminder"
        holder.binding.btnActionPrimary.setOnClickListener { onRemindNow(item) }
        holder.binding.btnActionDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size
}