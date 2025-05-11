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
import com.example.textn.R
import com.example.textn.data.model.ForecastTabularData
import com.example.textn.databinding.FragmentTabularForecastBinding
import com.example.textn.ui.adapter.ForecastTabularAdapter
import com.example.textn.viewmodel.ForecastTabularViewModel
import com.example.textn.viewmodel.ForecastTabularViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

class TabularForecastFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTabularForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForecastTabularViewModel
    private lateinit var forecastAdapter: ForecastTabularAdapter
    private lateinit var googleMap: GoogleMap

    // Default model is hardcoded since we've removed the selection UI
    private val defaultModel = "GFS27"

    // Default location coordinates (Da Nang, Vietnam)
    private lateinit var defaultLocation: LatLng

    // Weather layer types
    private val weatherLayers = listOf("Wind", "Temperature", "Precipitation", "Clouds")
    private var currentLayer = "Wind"
    private var currentLayerOverlay: com.google.android.gms.maps.model.TileOverlay? = null

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
        setupWeatherLayerButtons()
        setupObservers()
        setupMap()

        val lat = requireArguments().getDouble("lat")
        val lon = requireArguments().getDouble("lon")
        defaultLocation = LatLng(lat, lon)

        // Initial data load with default model
        viewModel.fetchForecastData(defaultLocation.latitude, defaultLocation.longitude, defaultModel)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map settings
        with(googleMap) {
            mapType = GoogleMap.MAP_TYPE_SATELLITE
            uiSettings.apply {
                isZoomControlsEnabled = false
                isCompassEnabled = true
                isRotateGesturesEnabled = true
                isScrollGesturesEnabled = true
            }
        }

        // Move camera to default location with appropriate zoom level
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        // Apply default weather layer
        applyWeatherLayer(currentLayer)

        // Set up location click listener
        googleMap.setOnMapClickListener { latLng ->
            // Update marker position
            updateSelectedLocation(latLng)

            // Fetch new forecast data for selected location
            viewModel.fetchForecastData(latLng.latitude, latLng.longitude, defaultModel)
        }
    }

    private fun updateSelectedLocation(latLng: LatLng) {
        // Here you could add or update a marker on the map
        // For simplicity, we'll just update the weather overlay
        applyWeatherLayer(currentLayer)

        // Display coordinates on UI if needed
        binding.textLocationInfo.text = String.format(
            Locale.getDefault(),
            "%.4f, %.4f",
            latLng.latitude,
            latLng.longitude
        )
    }

    private fun applyWeatherLayer(layerType: String) {
        // Remove existing overlay if any
        currentLayerOverlay?.remove()

        // Create new tile overlay based on selected layer and model
        val tileProvider = createWeatherTileProvider(
            layerType,
            defaultModel
        )

        currentLayerOverlay = googleMap.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.3f)
        )

        currentLayer = layerType
        updateSelectedLayerButton(layerType)
    }

    private fun createWeatherTileProvider(layerType: String, modelName: String): UrlTileProvider {
        // In a real app, you would use your weather data provider's API
        return object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val layerCode = when (layerType.lowercase()) {
                    "wind" -> "wind"
                    "temperature" -> "temp"
                    "precipitation" -> "precipitation"
                    "clouds" -> "clouds"
                    else -> "clouds"
                }
                val apiKey = "455ecceeeb343a351bdba343de087d00"
                val urlStr = "https://tile.openweathermap.org/map/$layerCode/$zoom/$x/$y.png?appid=$apiKey"
                return try {
                    URL(urlStr)
                } catch (e: MalformedURLException) {
                    null
                }
            }
        }
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

    private fun setupWeatherLayerButtons() {
        // Dynamically create weather layer buttons
        weatherLayers.forEach { layer ->
            val layerButton = layoutInflater.inflate(
                R.layout.item_forecast_model_button,
                binding.weatherLayerContainer,
                false
            ) as TextView

            layerButton.text = layer
            layerButton.setOnClickListener {
                if (::googleMap.isInitialized) {
                    applyWeatherLayer(layer)
                }
            }

            binding.weatherLayerContainer.addView(layerButton)
        }

        // Set default selected layer
        updateSelectedLayerButton(currentLayer)
    }

    private fun updateSelectedLayerButton(selectedLayer: String) {
        // Find all layer buttons and update their state
        for (i in 0 until binding.weatherLayerContainer.childCount) {
            val button = binding.weatherLayerContainer.getChildAt(i) as? TextView
            if (button != null) {
                val isSelected = button.text.toString() == selectedLayer
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
    }

    private fun setupObservers() {
        viewModel.forecastData.observe(viewLifecycleOwner) { data ->
            updateForecastDisplay(data)
            updateModelInfo(data)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarCard.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.forecastContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateForecastDisplay(data: ForecastTabularData) {
        forecastAdapter.submitData(data.days)
    }

    private fun updateModelInfo(data: ForecastTabularData) {
        val modelInfoText = "${data.modelResolution}km resolution, ${data.modelAccuracy}% accuracy"
        binding.textModelInfo.text = modelInfoText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}