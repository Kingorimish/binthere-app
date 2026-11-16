package com.example.binthere.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.binthere.R
import com.example.binthere.databinding.FragmentDashboardBinding
import com.example.binthere.ui.viewmodel.WasteViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WasteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRefreshWeather.setOnClickListener {
            viewModel.refreshWeather()
        }

        binding.btnQuickReport.setOnClickListener {
            findNavController().navigate(R.id.alertsFragment)
        }

        binding.btnQuickSchedule.setOnClickListener {
            findNavController().navigate(R.id.schedulesFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.weatherStatus.collect { weather ->
                        binding.tvWeatherHeader.text =
                            "Nairobi: ${weather.temperature} • ${weather.condition}"
                        binding.tvFloodAdvisory.text = weather.advisoryMessage
                        binding.tvFloodAdvisory.setTextColor(
                            if (weather.isFloodRiskHigh) Color.parseColor("#C62828")
                            else Color.parseColor("#1B5E20")
                        )
                    }
                }
                launch {
                    viewModel.alerts.collect { list ->
                        val pending = list.count { it.status == "Pending" }
                        binding.tvPendingCount.text = pending.toString()
                    }
                }
                launch {
                    viewModel.schedules.collect { list ->
                        binding.tvSchedulesCount.text = list.size.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}