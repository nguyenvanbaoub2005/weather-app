package com.example.textn.ui.fragment

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.PopupMenu
import androidx.annotation.RequiresApi

class TabularForecastFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTabularForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForecastTabularViewModel
    private lateinit var forecastAdapter: ForecastTabularAdapter
    private lateinit var googleMap: GoogleMap

    // Default model is hardcoded since we've removed the selection UI
    private val defaultModel = "GFS27"

    //Location
    private lateinit var defaultLocation: LatLng

    // Weather layer types
    private val weatherLayers = listOf("Gió", "Nhiệt độ", "Lượng mưa", "Mây")
    // Initialize currentLayer with Vietnamese name to match UI buttons
    private var currentLayer = "Gió"
    private var currentLayerOverlay: com.google.android.gms.maps.model.TileOverlay? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabularForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupWeatherLayerButtons()
        setupObservers()
        setupMap()
        setupToolbar()

        //get Location from HomeFragment
        val lat = requireArguments().getDouble("lat")
        val lon = requireArguments().getDouble("lon")
        defaultLocation = LatLng(lat, lon)

        // Get and display city name
        updateCityName(defaultLocation)

        // Initial data load with default model
        viewModel.fetchForecastData(defaultLocation.latitude, defaultLocation.longitude)
    }

    private fun setupToolbar() {
        // Set back button click listener
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set info button click listener if needed
//        binding.btnInfo.setOnClickListener {
//            Toast.makeText(context, "Thông tin dự báo thời tiết chi tiết", Toast.LENGTH_SHORT).show()
//        }

        // Set share button click listener if needed
        binding.btnShare.setOnClickListener {
            Toast.makeText(context, "Chia sẻ thông tin thời tiết", Toast.LENGTH_SHORT).show()
        }

        // Set menu button click listener
        binding.btnMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        
        // Thiết lập sự kiện click cho các mục menu
        bottomSheetView.findViewById<LinearLayout>(R.id.menu_home_item).setOnClickListener {
            findNavController().navigate(R.id.nav_home)
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.menu_health_alerts_item).setOnClickListener {
            findNavController().navigate(R.id.nav_health)
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.menu_ai_forecast_item).setOnClickListener {
            findNavController().navigate(R.id.geminiAIFragment)
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.menu_settings_item).setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.show()
    }

    private fun updateCityName(location: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                withContext(Dispatchers.Main) {
                    if (addresses?.isNotEmpty() == true) {
                        val cityName = addresses[0]?.locality
                            ?: addresses[0]?.adminArea
                            ?: addresses[0]?.subAdminArea
                            ?: "Unknown Location"

                        binding.textCityName.text = cityName
                    } else {
                        binding.textCityName.text = "Unknown Location"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textCityName.text = "Unknown Location"
                }
            }
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Cấu hình các thiết lập của bản đồ
        with(googleMap) {
            mapType = GoogleMap.MAP_TYPE_SATELLITE // Chọn kiểu bản đồ vệ tinh
            uiSettings.apply {
                // Hiển thị nút phóng to/thu nhỏ trên bản đồ
                isZoomControlsEnabled = true
                // Hiển thị la bàn
                isCompassEnabled = true
                // Cho phép xoay bản đồ
                isRotateGesturesEnabled = true
                // Vô hiệu hóa cử chỉ cuộn để cố định vị trí bản đồ
                isScrollGesturesEnabled = false
                // Vô hiệu hóa cử chỉ nghiêng bản đồ (2 ngón kéo lên/xuống)
                isTiltGesturesEnabled = false
                // Cho phép phóng to/thu nhỏ bằng cử chỉ 2 ngón tay
                isZoomGesturesEnabled = true
            }
        }

        // Di chuyển camera đến vị trí mặc định với mức thu phóng phù hợp
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        // Áp dụng lớp dữ liệu thời tiết mặc định
        applyWeatherLayer(currentLayer)

        // Thiết lập sự kiện khi nhấp vào bản đồ - Cho phép chọn vị trí bằng cách chạm vào bản đồ
//        googleMap.setOnMapClickListener { latLng ->
//            // Cập nhật vị trí marker đến vị trí người dùng đã chọn
//            updateSelectedLocation(latLng)
//
//            // Update the city name for the new location
//            updateCityName(latLng)
//
//            // Lấy dữ liệu dự báo thời tiết mới cho vị trí đã chọn
//            viewModel.fetchForecastData(latLng.latitude, latLng.longitude, defaultModel)
//
//            // Di chuyển camera đến vị trí mới được chọn
//            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//        }
    }


    private fun updateSelectedLocation(latLng: LatLng) {
        // Here you could add or update a marker on the map
        // For simplicity, we'll just update the weather overlay
        applyWeatherLayer(currentLayer)

        // Display coordinates on UI if needed
//        binding.textLocationInfo.text = String.format(
//            Locale.getDefault(),
//            "%.4f, %.4f",
//            latLng.latitude,
//            latLng.longitude
//        )
    }

    private fun applyWeatherLayer(layerType: String) {
        // Remove existing overlay if any
        currentLayerOverlay?.remove()

        // Create new tile overlay based on selected layer
        val tileProvider = createWeatherTileProvider(layerType)

        currentLayerOverlay = googleMap.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.3f)
        )

        currentLayer = layerType
        updateSelectedLayerButton(layerType)
    }

    private fun createWeatherTileProvider(layerType: String): UrlTileProvider {
        // Remove modelName parameter from the method signature
        return object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val layerCode = when (layerType.lowercase()) {
                    "gió" -> "wind"
                    "nhiệt độ" -> "temp"
                    "lượng mưa" -> "precipitation"
                    "mây" -> "clouds"
                    else -> "wind"
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

        // Since we've already initialized the UI buttons, immediately update the selected button
        // to match our default layer (Gió)
        updateSelectedLayerButton(currentLayer)
    }

    private fun updateSelectedLayerButton(selectedLayer: String) {
        // Find all layer buttons and update their state
        for (i in 0 until binding.weatherLayerContainer.childCount) {
            val button = binding.weatherLayerContainer.getChildAt(i) as? TextView
            if (button != null) {
                val isSelected = button.text.toString().equals(selectedLayer, ignoreCase = true)
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
            // Removed model info update since we don't need it anymore
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

    // Hàm để cập nhật hiển thị dự báo thời tiết
    private fun updateForecastDisplay(data: ForecastTabularData) {
        // Kiểm tra xem có dữ liệu dự báo hay không
        if (data.days.isEmpty() || data.days[0].hourlyForecasts.isEmpty()) {
            // Hiển thị thông báo nếu không có dữ liệu
            Toast.makeText(
                context,
                "Không có dữ liệu dự báo cho vị trí này", // Thông báo lỗi
                Toast.LENGTH_SHORT
            ).show()
            binding.forecastContent.visibility = View.GONE // Ẩn nội dung dự báo
            return
        }

        // Cập nhật dữ liệu cho adapter
        forecastAdapter.submitData(data.days)
        binding.forecastContent.visibility = View.VISIBLE // Hiển thị nội dung dự báo
        binding.textModelInfo.text = "Weather forecast " // Cập nhật thông tin mô hình  :Weather forecast based on OpenWeatherMap data
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