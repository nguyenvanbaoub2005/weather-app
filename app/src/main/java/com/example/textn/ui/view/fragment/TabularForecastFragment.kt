package com.example.textn.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.DayForecast
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.data.model.HourlyForecast
import com.example.textn.databinding.FragmentTabularForecastBinding
import com.example.textn.ui.adapter.ForecastTabularAdapter
import com.example.textn.ui.view.WindDirectionView
import com.example.textn.viewmodel.ForecastTabularViewModel
import com.example.textn.viewmodel.ForecastTabularViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class TabularForecastFragment : Fragment() {

    private var _binding: FragmentTabularForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForecastTabularViewModel
    private lateinit var forecastAdapter: ForecastTabularAdapter

    private val forecastModels = listOf("GFS27", "ECMWF", "ICON")

    private val modelButtons = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabularForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupForecastModelButtons()
        setupObservers()

        // Initial data load - using fixed coordinates for demo
        viewModel.fetchForecastData(16.0544, 108.2022) // Da Nang, Vietnam coordinates (from map in screenshot)
    }

    private fun setupViewModel() {
        val factory = ForecastTabularViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[ForecastTabularViewModel::class.java]
    }

    private fun setupRecyclerView() {
        forecastAdapter = ForecastTabularAdapter()
        binding.recyclerViewForecast.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }
    }

    private fun setupForecastModelButtons() {
        // Dynamically create model selection buttons
        forecastModels.forEach { model ->
            val modelButton = layoutInflater.inflate(
                R.layout.item_forecast_model_button,
                binding.forecastModelContainer,
                false
            ) as TextView

            modelButton.text = model
            modelButton.setOnClickListener {
                viewModel.changeForecastModel(model)
                updateSelectedModelButton(model)
            }

            binding.forecastModelContainer.addView(modelButton)
            modelButtons.add(modelButton)
        }
    }

    private fun updateSelectedModelButton(selectedModel: String) {
        modelButtons.forEachIndexed { index, button ->
            val isSelected = forecastModels[index] == selectedModel
            button.isSelected = isSelected
            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.forecast_model_selected_text else R.color.forecast_model_text
                )
            )
            button.setBackgroundResource(
                if (isSelected) R.drawable.bg_forecast_model_selected else R.drawable.bg_forecast_model
            )
        }
    }

    private fun setupObservers() {
        viewModel.forecastData.observe(viewLifecycleOwner) { data ->
            updateForecastDisplay(data)
            updateModelInfo(data)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.forecastContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.selectedForecastModel.observe(viewLifecycleOwner) { model ->
            updateSelectedModelButton(model)
        }
    }

    private fun updateForecastDisplay(data: ForecastTabularData) {
        forecastAdapter.submitData(data.days)
    }

    private fun updateModelInfo(data: ForecastTabularData) {
        val modelInfoText = "${data.modelName} - ${data.modelResolution}km resolution, ${data.modelAccuracy}% accuracy"
        binding.textModelInfo.text = modelInfoText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}