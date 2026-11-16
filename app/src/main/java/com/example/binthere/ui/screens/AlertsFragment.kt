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
import com.example.binthere.data.local.entities.CleanupAlertEntity
import com.example.binthere.databinding.FragmentListScreenBinding
import com.example.binthere.databinding.ItemRecordCardBinding
import com.example.binthere.ui.viewmodel.WasteViewModel
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {

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

        binding.tvScreenTitle.text = "Cleanup & Drain Alerts"
        binding.btnAddNew.text = "+ Report Issue"

        val adapter = AlertsAdapter(
            onToggleStatus = { alert -> viewModel.toggleAlertStatus(alert) },
            onDelete = { alert -> viewModel.deleteAlert(alert) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnAddNew.setOnClickListener {
            showReportDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.alerts.collect { list ->
                        adapter.submitList(list)
                        binding.tvEmptyState.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                        binding.tvEmptyState.text =
                            "No cleanup alerts logged yet. Tap '+ Report Issue' to flag a blocked drain or dump site."
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

    private fun showReportDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etZone = EditText(requireContext()).apply {
            hint = "Nairobi Zone (e.g. CBD, South B, Westlands)"
        }
        val etLandmark = EditText(requireContext()).apply {
            hint = "Specific Landmark (e.g. Moi Avenue Bus Stage)"
        }

        val issueTypes = arrayOf("Clogged Storm Drain", "Illegal Dumping", "Overflowing Bin", "Flooding Risk")
        val spinnerIssue = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, issueTypes)
        }

        val severities = arrayOf("High", "Medium", "Low")
        val spinnerSeverity = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, severities)
        }

        val roles = arrayOf("Resident", "Community Volunteer", "County Official")
        val spinnerRole = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        }

        container.addView(etZone)
        container.addView(etLandmark)
        container.addView(spinnerIssue)
        container.addView(spinnerSeverity)
        container.addView(spinnerRole)

        AlertDialog.Builder(requireContext())
            .setTitle("Report Waste / Drain Hazard")
            .setView(container)
            .setPositiveButton("Submit") { _, _ ->
                viewModel.submitCleanupAlert(
                    zone = etZone.text.toString(),
                    landmark = etLandmark.text.toString(),
                    issueType = spinnerIssue.selectedItem.toString(),
                    severity = spinnerSeverity.selectedItem.toString(),
                    reportedByRole = spinnerRole.selectedItem.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class AlertsAdapter(
    private val onToggleStatus: (CleanupAlertEntity) -> Unit,
    private val onDelete: (CleanupAlertEntity) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private var items: List<CleanupAlertEntity> = emptyList()

    fun submitList(newItems: List<CleanupAlertEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class AlertViewHolder(val binding: ItemRecordCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemRecordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCardTitle.text = "${item.issueType} • ${item.zone}"
        holder.binding.tvCardSubtitle.text =
            "Landmark: ${item.landmark}\nReported by: ${item.reportedByRole} | Severity: ${item.severity}"
        holder.binding.tvCardBadge.text = "STATUS: ${item.status.uppercase()}"

        holder.binding.btnActionPrimary.text =
            if (item.status == "Pending") "Mark Cleared" else "Reopen"
        holder.binding.btnActionPrimary.setOnClickListener { onToggleStatus(item) }
        holder.binding.btnActionDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size
}