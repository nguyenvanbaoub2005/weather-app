package com.example.textn.ui.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Lưu lại lớp thời tiết hiện tại (mặc định là "wind")
    private var currentLayer = "wind"

    // Cờ để chỉ setup WebView một lần
    private var isWebViewInitialized = false

    // Lưu trữ vị trí đã lấy được
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var geminiViewModel: GeminiViewModel
    private lateinit var forecastAdapter: DayForecastAdapter

    // Biến để giữ tham chiếu đến loading dialog
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tạo WeatherViewModel sử dụng Repository và Retrofit
        val apiService = RetrofitClient.instance
        val repository = WeatherRepository(apiService)
        val factory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(requireActivity(), factory)[WeatherViewModel::class.java]

        // Khởi tạo GeminiViewModel
        geminiViewModel = GeminiViewModel(requireContext())

        // Thiết lập RecyclerView dự báo thời tiết
        setupForecastRecyclerView()

        // Quan sát dữ liệu thời tiết
        setupWeatherObservers()

        // Thiết lập observers cho GeminiViewModel
        setupGeminiObservers()

        // Thêm sự kiện click cho nút btnFavoriteName
        binding.btnFavoriteName.setOnClickListener {
            findNavController().navigate(R.id.nav_Location_type)
        }

        // Thêm sự kiện click cho cardMap để chuyển sang Fragment bản đồ chi tiết
        binding.btnExpandMap.setOnClickListener {
            // Sử dụng NavController để điều hướng đến FullMapFragment
            findNavController().navigate(R.id.nav_weather)
        }

        // Thêm sự kiện click cho cardWeather để chuyển sang WindyFragment
        binding.cardWeather.setOnClickListener {
            // Truyền dữ liệu vị trí nếu cần
            val bundle = Bundle().apply {
                lastLat?.let { it1 -> putDouble("lat", it1) }
                lastLon?.let { it1 -> putDouble("lon", it1) }
            }

              findNavController().navigate(R.id.action_tabularForecastFragment, bundle)
        }


        // Nút mở menu navigation drawer
        binding.btnMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        // Nút bản đồ (hiển thị bản đồ đầy đủ)
        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.nav_weather)
        }

        // Nút chọn lớp dữ liệu thời tiết (wind, temp, rain,...)
        binding.cardWeather.findViewById<View>(R.id.btn_settings).setOnClickListener {
            showWeatherLayerOptions()
        }

        // Thiết lập WebView chỉ một lần
        if (!isWebViewInitialized) {
            WeatherHelper.setupWebView(binding.webViewWindyHome)
            // Lần đầu tiên lấy vị trí và tải bản đồ
            getLocationAndUpdateMap()
            isWebViewInitialized = true
        }
    }

    override fun onResume() {
        super.onResume()
        // Chỉ cập nhật dữ liệu thời tiết nếu đã có vị trí, không tải lại bản đồ
        if (lastLat != null && lastLon != null) {
            weatherViewModel.fetchWeather(lastLat!!, lastLon!!, WeatherHelper.API_KEY)
        }
    }

    // Thiết lập observers cho GeminiViewModel
    private fun setupGeminiObservers() {
        // Observer cho kết quả từ AI
        geminiViewModel.aiResponse.observe(viewLifecycleOwner, Observer { response ->
            // Hiển thị kết quả trong dialog
            showLocationSuggestionsDialog(response)
        })

        // Observer cho loading state
        geminiViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        })

        // Observer cho lỗi
        geminiViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
    }

//    // Dialog để chọn loại địa điểm muốn tìm kiếm
//    private fun showLocationTypeDialog() {
//        val locationTypes = arrayOf("Tất cả", "Ăn uống", "Giải trí", "Mua sắm", "Lưu trú")
//        val locationTypeValues = arrayOf("all", "food", "entertainment", "shopping", "accommodation")
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Chọn loại địa điểm")
//            .setItems(locationTypes) { _, which ->
//                // Lấy giá trị loại địa điểm đã chọn
//                val selectedLocationType = locationTypeValues[which]
//
//                // Gọi API để lấy gợi ý địa điểm gần đó
//                geminiViewModel.getSuggestedLocationsNearby(
//                    geminiApiKey,
//                    numberOfLocations = 5,
//                    locationType = selectedLocationType
//                )
//            }
//            .show()
//    }

    // Hiển thị loading dialog
    private fun showLoadingDialog() {
        val progressBar = ProgressBar(context).apply {
            indeterminateTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_blue_background)
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(progressBar)
            .setMessage("Đang tìm kiếm địa điểm gần đây...")
            .setCancelable(false)

        loadingDialog = builder.create()
        loadingDialog?.show()
    }

    // Đóng loading dialog
    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    // Hiển thị kết quả tìm kiếm trong dialog
    private fun showLocationSuggestionsDialog(response: String) {
        // Tạo dialog với nội dung từ AI
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Địa điểm gần đây")
            .setMessage(response)
            .setPositiveButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
            .apply {
                // Đảm bảo dialog không bị giới hạn chiều cao
                window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
    }

    // Thiết lập RecyclerView hiển thị danh sách dự báo
    private fun setupForecastRecyclerView() {
        forecastAdapter = DayForecastAdapter()
        binding.cardWeather.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.rv_forecast
        ).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastAdapter
        }
    }

    // Lắng nghe thay đổi dữ liệu từ ViewModel
    private fun setupWeatherObservers() {
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            // Lấy vị trí hiện tại và cập nhật UI
            WeatherHelper.updateWindyMapWithCurrentLocation(
                requireActivity(),
                binding.webViewWindyHome,
                "wind"
            ) { lat, lon ->
                // Lưu lại vị trí
                lastLat = lat
                lastLon = lon

                // Sử dụng hàm static để lấy tên địa điểm
                val cityName = WeatherHelper.getLocationFromCoordinates(requireContext(), lat, lon)
                binding.cardWeather.findViewById<android.widget.TextView>(
                    R.id.tv_location_name
                ).text = cityName
            }

            // Hiển thị 5 ngày đầu trong daily forecast
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

        // Hiển thị thông báo khi có lỗi
        weatherViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm mới để lấy vị trí và cập nhật bản đồ
    private fun getLocationAndUpdateMap() {
        // Reset biến isLocationFetched để có thể lấy vị trí mới
        WeatherHelper.resetLocationFetched()

        WeatherHelper.updateWindyMapWithCurrentLocation(
            requireActivity(),
            binding.webViewWindyHome,
            currentLayer
        ) { lat, lon ->
            // Lưu lại vị trí
            lastLat = lat
            lastLon = lon

            // Cập nhật thông tin thời tiết
            weatherViewModel.fetchWeather(lat, lon, WeatherHelper.API_KEY)
            getLocationNameFromCoordinates(lat, lon)
        }
    }

    // Hiển thị danh sách lớp thời tiết để chọn
    private fun showWeatherLayerOptions() {
        val layers = arrayOf("wind", "temp", "rain", "clouds", "pressure")
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn lớp thời tiết")
            .setItems(layers) { _, which ->
                currentLayer = layers[which]

                // Cập nhật lớp thời tiết mà không tải lại toàn bộ bản đồ
                if (lastLat != null && lastLon != null) {
                    WeatherHelper.updateMapLayer(
                        binding.webViewWindyHome,
                        currentLayer,
                        lastLat!!,
                        lastLon!!
                    )
                }
            }
            .show()
    }

    // Lấy tên địa điểm từ tọa độ (hiển thị trên UI)
    private fun getLocationNameFromCoordinates(lat: Double, lon: Double) {
        try {
            val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locationName = address.locality ?: address.adminArea ?: address.countryName ?: "Unknown Location"

                binding.cardWeather.findViewById<android.widget.TextView>(
                    R.id.tv_location_name
                ).text = locationName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Trả về icon tương ứng với mã thời tiết
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Đảm bảo đóng dialog khi fragment bị hủy
        dismissLoadingDialog()
    }
}