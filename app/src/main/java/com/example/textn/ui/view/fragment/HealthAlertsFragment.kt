package com.example.textn.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.textn.R
import com.example.textn.data.network.RetrofitClient
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.databinding.FragmentHealthAlertsBinding
import com.example.textn.ui.adapter.HealthAlertsAdapter
import com.example.textn.viewmodel.HealthAlertViewModel
import com.example.textn.viewmodel.HealthAlertViewModelFactory

class HealthAlertsFragment : Fragment() {

    private lateinit var binding: FragmentHealthAlertsBinding
    private val viewModel: HealthAlertViewModel by viewModels {
        val repository = WeatherRepository(RetrofitClient.instance)
        HealthAlertViewModelFactory(requireActivity().application, repository)
    }
    private lateinit var adapter: HealthAlertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupCloseButton()

        // Lấy vị trí đã lưu và gọi fetch dữ liệu
        val prefs = requireContext()
            .getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val lastLoc = prefs.getString("last_location", null)
        if (!lastLoc.isNullOrBlank()) {
            viewModel.fetchWeatherData(lastLoc)
        } else {
            // Dùng vị trí mẫu nếu chưa có
            viewModel.fetchWeatherData("21.0285,105.8342")
        }
    }

    private fun setupRecyclerView() {
        adapter = HealthAlertsAdapter(emptyList())
        binding.recyclerHealthAlerts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HealthAlertsFragment.adapter
        }
    }

    private fun setupCloseButton() {
        binding.btnReturn.setOnClickListener {
            val navController = findNavController()
                navController.navigate(R.id.nav_home)
        }
    }

    private fun observeViewModel() {
        viewModel.healthAlerts.observe(viewLifecycleOwner) { alerts ->
            adapter.updateData(alerts)

            if (alerts.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerHealthAlerts.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerHealthAlerts.visibility = View.VISIBLE
            }
        }
    }
}
