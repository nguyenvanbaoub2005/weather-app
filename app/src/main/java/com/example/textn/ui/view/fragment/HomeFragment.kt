package com.example.textn.ui.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.textn.R
import com.example.textn.data.model.DayForecastItem
import com.example.textn.data.network.RetrofitClient
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.databinding.FragmentHomeBinding
import com.example.textn.ui.view.activity.MainActivity
import com.example.textn.ui.adapter.DayForecastAdapter
import com.example.textn.utils.WeatherHelper
import com.example.textn.viewmodel.GeminiViewModel
import com.example.textn.viewmodel.WeatherViewModel
import com.example.textn.viewmodel.WeatherViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModels
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var geminiViewModel: GeminiViewModel

    // Adapters
    private lateinit var forecastAdapter: DayForecastAdapter

    // Location tracking
    private var currentLocation: Pair<Double, Double>? = null
    private var currentWeatherLayer = "wind"

    // Permission handling
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Fine location permission granted, attempt to get location
                fetchCurrentLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Coarse location permission granted
                fetchCurrentLocation()
            }
            else -> {
                // No location permission
                Toast.makeText(
                    requireContext(),
                    "Location permissions are required for weather information",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModels
        initializeViewModels()

        // Setup UI Components
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Initialize WebView and Fetch Location
        initializeWebView()
    }

    private fun initializeViewModels() {
        // Weather ViewModel Setup
        val apiService = RetrofitClient.instance
        val repository = WeatherRepository(apiService)
        val factory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(requireActivity(), factory)[WeatherViewModel::class.java]

        // Gemini ViewModel
        geminiViewModel = GeminiViewModel(requireContext())
    }

    private fun setupRecyclerView() {
        forecastAdapter = DayForecastAdapter()
        binding.cardWeather.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.rv_forecast
        ).apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = forecastAdapter
        }
    }

    private fun setupObservers() {
        // Weather Data Observer
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            // Update forecast list
            val forecastItems = weatherData.daily.take(5).map { dayForecast ->
                val date = Date(dayForecast.dt * 1000)
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

                DayForecastItem(
                    id = dayForecast.dt,
                    day = dayFormat.format(date),
                    date = dateFormat.format(date),
                    temperature = "${dayForecast.temp.day.toInt()}°C",
                    description = dayForecast.weather[0].description,
                    iconId = getWeatherIconResource(dayForecast.weather[0].icon),
                    humidity = "${dayForecast.humidity}%",
                    windSpeed = "${dayForecast.wind_speed} m/s"
                )
            }
            forecastAdapter.submitList(forecastItems)
        }

        // Error Observer
        weatherViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Gemini AI Observers
        setupGeminiObservers()
    }

    private fun setupGeminiObservers() {
        geminiViewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            showLocationSuggestionsDialog(response)
        }

        geminiViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoadingDialog() else dismissLoadingDialog()
        }

        geminiViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        // Navigation listeners
        binding.apply {
            btnExpandMap.setOnClickListener {
                findNavController().navigate(R.id.nav_weather)
            }

            cardWeather.setOnClickListener {
                val bundle = Bundle().apply {
                    currentLocation?.let { loc ->
                        putDouble("lat", loc.first)
                        putDouble("lon", loc.second)
                    }
                }
                findNavController().navigate(R.id.action_tabularForecastFragment, bundle)
            }

            btnMenu.setOnClickListener {
                (activity as? MainActivity)?.openDrawer()
            }

            btnMap.setOnClickListener {
                findNavController().navigate(R.id.nav_weather)
            }

            tvChat.setOnClickListener {
                findNavController().navigate(R.id.nav_gemini_ai)
            }

            tvLocationType.setOnClickListener {
                findNavController().navigate(R.id.nav_Location_type)
            }

            btnBlog.setOnClickListener {
                findNavController().navigate(R.id.nav_communityFragment)
            }

            // Weather layer settings
            cardWeather.findViewById<View>(R.id.btn_settings).setOnClickListener {
                showWeatherLayerOptions()
            }
        }
    }

    private fun initializeWebView() {
        // Setup WebView
        WeatherHelper.setupWebView(binding.webViewWindyHome)

        // Check and request location permissions
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                fetchCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain why location is needed
                showLocationPermissionRationaleDialog()
            }
            else -> {
                // Request permissions
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun fetchCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude

                    // Update current location
                    currentLocation = Pair(lat, lon)

                    // Update map and weather data
                    updateMapAndWeatherData(lat, lon)
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Không thể lấy vị trí hiện tại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (securityException: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Location permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateMapAndWeatherData(lat: Double, lon: Double) {
        // Update Windy map
        WeatherHelper.updateWindyMapWithCurrentLocation(
            requireActivity(),
            binding.webViewWindyHome,
            currentWeatherLayer
        )

        // Fetch weather data
        weatherViewModel.fetchWeather(lat, lon, WeatherHelper.API_KEY)

        // Update location name
        updateLocationName(lat, lon)
    }

    private fun updateLocationName(lat: Double, lon: Double) {
        try {
            val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locationName = address.locality
                    ?: address.adminArea
                    ?: address.countryName
                    ?: "Unknown Location"

                binding.cardWeather.findViewById<android.widget.TextView>(
                    R.id.tv_location_name
                ).text = locationName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLocationPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Permission")
            .setMessage("This app needs location access to provide accurate weather information.")
            .setPositiveButton("OK") { _, _ ->
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWeatherLayerOptions() {
        val layers = arrayOf("wind", "temp", "rain", "clouds", "pressure")
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn lớp thời tiết")
            .setItems(layers) { _, which ->
                currentWeatherLayer = layers[which]

                // Update map layer if location is available
                currentLocation?.let { loc ->
                    WeatherHelper.updateMapLayer(
                        binding.webViewWindyHome,
                        currentWeatherLayer,
                        loc.first,
                        loc.second
                    )
                }
            }
            .show()
    }

    private fun showLoadingDialog() {
        // Implement loading dialog if needed
    }

    private fun dismissLoadingDialog() {
        // Dismiss loading dialog if needed
    }

    private fun showLocationSuggestionsDialog(response: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Địa điểm gần đây")
            .setMessage(response)
            .setPositiveButton("Đóng") { dialog, _ -> dialog.dismiss() }
            .show()
            .apply {
                window?.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                )
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
    }

    private fun getWeatherIconResource(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_clear_day
            "01n" -> R.drawable.ic_clear_night
            "02d" -> R.drawable.ic_partly_cloudy_day
            "02n" -> R.drawable.ic_partly_cloudy_night
            "03d", "03n" -> R.drawable.ic_cloudy
            "04d", "04n" -> R.drawable.ic_cloudy
            "09d", "09n" -> R.drawable.ic_rain
            "10d", "10n" -> R.drawable.ic_rain
            "11d", "11n" -> R.drawable.ic_thunder
            "13d", "13n" -> R.drawable.ic_snow
            "50d", "50n" -> R.drawable.ic_fog
            else -> R.drawable.ic_clear_day
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh location and weather data if location is available
        currentLocation?.let { loc ->
            updateMapAndWeatherData(loc.first, loc.second)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}