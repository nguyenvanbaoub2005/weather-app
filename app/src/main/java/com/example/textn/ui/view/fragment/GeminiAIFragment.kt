package com.example.textn.ui.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.viewmodel.GeminiViewModel
import com.example.textn.data.model.WeatherData
import com.example.textn.databinding.FragmentGeminiAiBinding
import com.example.textn.viewmodel.GeminiViewModelFactory
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class GeminiAIFragment : Fragment() {

    private var _binding: FragmentGeminiAiBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GeminiViewModel

    // API key - trong trường hợp thực tế nên đặt trong BuildConfig hoặc lấy từ settings
    private val geminiApiKey = "AIzaSyD647aAzMdwe0biy5gu_JP0jmEw1UDg3LQ"

    // Launcher để yêu cầu quyền truy cập vị trí
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Quyền đã được cấp, lấy thời tiết tại vị trí hiện tại
                getWeatherForCurrentLocation()
            }
            else -> {
                // Quyền bị từ chối
                Toast.makeText(
                    context,
                    "Bạn cần cấp quyền truy cập vị trí để sử dụng tính năng này",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeminiAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tạo factory với context
        val factory = GeminiViewModelFactory(requireContext())

        // Khởi tạo ViewModel với factory
        viewModel = ViewModelProvider(this, factory).get(GeminiViewModel::class.java)

        // Sử dụng ViewModel
        viewModel.aiResponse.observe(viewLifecycleOwner, Observer {
            // Xử lý dữ liệu từ ViewModel
        })

        setupCloseButton()
        setupObservers()
        setupClickListeners()
        checkLocationPermissionAndGetWeather()

        // Lấy thông tin vị trí hiện tại
        updateLocationDisplay()
    }

    /**
     * Cập nhật hiển thị vị trí hiện tại
     */
    private fun updateLocationDisplay() {
        // Đặt trạng thái mặc định cho text view vị trí
        binding.tvLocation.text = "Đang xác định vị trí..."

        // Kiểm tra quyền vị trí
        if (hasLocationPermission()) {
            // Gọi hàm getCurrentLocation từ ViewModel trong coroutine scope
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val locationName = viewModel.getCurrentLocation()
                    if (locationName != null) {
                        binding.tvLocation.text = locationName
                    } else {
                        binding.tvLocation.text = "Không thể xác định vị trí"
                    }
                } catch (e: Exception) {
                    binding.tvLocation.text = "Lỗi xác định vị trí"
                }
            }
        } else {
            binding.tvLocation.text = "Cần quyền truy cập vị trí"
        }
    }

    private fun setupCloseButton() {
        binding.btnReturn.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }
    }

    private fun setupObservers() {
        // Theo dõi kết quả từ AI
        viewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            showResponse(response)
            // Cuộn xuống khi có kết quả trả về
            scrollToResponse()
        }

        // Theo dõi trạng thái đang tải
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.btnAskWeatherAdvice.isEnabled = !isLoading
            binding.btnAskCustom.isEnabled = !isLoading

            // Nếu đang tải, hiển thị progressBar và cuộn xuống
            if (isLoading) {
                binding.tvResponseTitle.isVisible = true
                scrollToResponse()
            }
        }

        // Theo dõi lỗi
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showError(it)
                // Cuộn xuống khi có lỗi
                scrollToResponse()
            }
        }

        // Theo dõi dữ liệu thời tiết
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            updateWeatherUI(weatherData)
            // Cập nhật lại vị trí khi có dữ liệu thời tiết mới
            updateLocationDisplay()
        }
    }

    private fun setupClickListeners() {
        // Nút lấy lời khuyên dựa trên dữ liệu thời tiết hiện tại
        binding.btnAskWeatherAdvice.setOnClickListener {
            // Hiển thị UI response trước khi lấy dữ liệu
            prepareResponseUI()
            viewModel.getWeatherForCurrentLocationAndAdvice(geminiApiKey)
        }

        // Nút để hỏi câu hỏi tùy chỉnh
        binding.btnAskCustom.setOnClickListener {
            val customPrompt = binding.etCustomPrompt.text.toString().trim()
            if (customPrompt.isNotEmpty()) {
                // Hiển thị UI response trước khi lấy dữ liệu
                prepareResponseUI()
                viewModel.getCustomAdvice(customPrompt, geminiApiKey)
            } else {
                Toast.makeText(context, "Vui lòng nhập câu hỏi của bạn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Chuẩn bị giao diện phản hồi trước khi nhận dữ liệu
     */
    private fun prepareResponseUI() {
        binding.tvResponseTitle.isVisible = true
        binding.cardResponse.isVisible = true
        binding.shimmerLayout.isVisible = true
        binding.tvResponse.text = ""

        // Cuộn đến phần phản hồi
        scrollToResponse()
    }

    /**
     * Cuộn xuống đến phần phản hồi
     */
    private fun scrollToResponse() {
        lifecycleScope.launch {
            delay(500)
            try {
                val scrollView = view?.findViewById<androidx.core.widget.NestedScrollView>(R.id.nested_scroll_view)
                scrollView?.let {
                    // Tính toán vị trí cuộn
                    val responseViewLocation = IntArray(2)
                    binding.tvResponseTitle.getLocationInWindow(responseViewLocation)

                    // Cuộn đến vị trí
                    it.smoothScrollTo(0, responseViewLocation[1])
                }
            } catch (e: Exception) {
                Log.e("GeminiAIFragment", "Error scrolling to response: ${e.message}")
            }
        }
    }

    private fun showResponse(response: String) {
        // Khởi tạo Markwon
        // Tạo Markwon với các tùy chỉnh
        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(requireContext()))
            .build()

        binding.tvResponseTitle.isVisible = true
        binding.cardResponse.isVisible = true
        binding.tvResponse.text = response
        binding.shimmerLayout.isVisible = false
        // Xử lý và hiển thị văn bản Markdown
        markwon.setMarkdown(binding.tvResponse, response)

        binding.shimmerLayout.isVisible = false
    }

    private fun showError(errorMessage: String) {
        binding.tvResponseTitle.isVisible = true
        binding.cardResponse.isVisible = true
        binding.tvResponse.text = "Đã xảy ra lỗi: $errorMessage"
        binding.shimmerLayout.isVisible = false

        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun updateWeatherUI(weatherData: WeatherData) {
        // Cập nhật thông tin thời tiết lên giao diện
        binding.tvTemperature.text = "${weatherData.temperature.toInt()}°"
        binding.tvHumidity.text = "${weatherData.humidity.toInt()}%"
        binding.tvUvIndex.text = String.format("%.1f", weatherData.uvIndex)
        binding.tvCondition.text = translateWeatherCondition(weatherData.condition)
        binding.tvFeelsLike.text = "${(weatherData.temperature + 2).toInt()}°" // Giá trị ví dụ

        // Cập nhật icon thời tiết và màu nền dựa vào điều kiện thời tiết
        updateWeatherIcon(weatherData.condition)
    }

    private fun translateWeatherCondition(condition: String): String {
        return when (condition.lowercase(Locale.getDefault())) {
            "clear" -> "Trời quang"
            "clouds", "cloudy" -> "Nhiều mây"
            "rain" -> "Mưa"
            "thunderstorm" -> "Bão giông"
            "snow" -> "Tuyết"
            "mist", "fog" -> "Sương mù"
            "sunny" -> "Nắng"
            "partly cloudy" -> "Mây rải rác"
            else -> condition
        }
    }

    private fun updateWeatherIcon(condition: String) {
        val iconResource = when (condition.lowercase(Locale.getDefault())) {
            "clear", "sunny" -> R.drawable.ic_weather_sunny
            "clouds", "cloudy", "partly cloudy" -> R.drawable.ic_cloudy
            "rain" -> R.drawable.ic_rain
            "thunderstorm" -> R.drawable.ic_thunder
            "snow" -> R.drawable.ic_snow
            "mist", "fog" -> R.drawable.ic_fog
            else -> R.drawable.ic_weather_sunny
        }

        binding.ivWeatherIcon.setImageResource(iconResource)

        // Thay đổi màu nền dựa trên điều kiện thời tiết
        val backgroundColorRes = when (condition.lowercase(Locale.getDefault())) {
            "clear", "sunny" -> R.color.weather_sunny_bg
            "clouds", "cloudy", "partly cloudy" -> R.color.weather_cloudy_bg
            "rain" -> R.color.weather_rain_bg
            "thunderstorm" -> R.color.weather_thunderstorm_bg
            "snow" -> R.color.weather_snow_bg
            "mist", "fog" -> R.color.weather_fog_bg
            else -> R.color.weather_sunny_bg
        }

        // Áp dụng màu nền cho layout chứa thông tin thời tiết
        val backgroundColor = ContextCompat.getColor(requireContext(), backgroundColorRes)

        // Cập nhật màu nền của container thời tiết
        updateWeatherContainerBackground(backgroundColor)
    }

    /**
     * Cập nhật màu nền cho container thời tiết dựa trên nhiều cách tiếp cận khác nhau
     * để đảm bảo hoạt động trong mọi trường hợp
     */
    private fun updateWeatherContainerBackground(backgroundColor: Int) {
        try {
            // Cách 1: Tìm theo ID weather_container (nếu đã thêm vào layout)
            val container = view?.findViewById<View>(R.id.weather_container)
            if (container != null) {
                container.setBackgroundColor(backgroundColor)
                return
            }

            // Cách 2: Sử dụng parent của tvLocation
            val parent = binding.tvLocation.parent
            if (parent is View) {
                parent.setBackgroundColor(backgroundColor)
                return
            }

            // Cách 3: Trong trường hợp container nằm trong CardView
            val rootView = binding.root
            if (rootView is ViewGroup) {
                // Tìm CardView trong cây view
                findCardViewAndUpdateBackground(rootView, backgroundColor)
            }

        } catch (e: Exception) {
            // Log lỗi nếu cần thiết
            Log.e("GeminiAIFragment", "Error updating background: ${e.message}")
        }
    }

    /**
     * Hàm đệ quy để tìm CardView trong cây view và cập nhật màu nền
     */
    private fun findCardViewAndUpdateBackground(viewGroup: ViewGroup, backgroundColor: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            // Kiểm tra nếu là CardView
            if (child is CardView) {
                child.setCardBackgroundColor(backgroundColor)
                return
            }

            // Nếu là ViewGroup, tìm kiếm đệ quy
            if (child is ViewGroup) {
                findCardViewAndUpdateBackground(child, backgroundColor)
            }
        }
    }
    /**
     * Kiểm tra quyền truy cập vị trí và lấy thời tiết hiện tại
     */
    private fun checkLocationPermissionAndGetWeather() {
        when {
            hasLocationPermission() -> {
                // Đã có quyền, lấy thời tiết tại vị trí hiện tại
                getWeatherForCurrentLocation()
            }
            shouldShowRequestPermissionRationale() -> {
                // Hiển thị giải thích tại sao cần quyền
                Toast.makeText(
                    context,
                    "Ứng dụng cần quyền truy cập vị trí để lấy thông tin thời tiết tại vị trí của bạn",
                    Toast.LENGTH_LONG
                ).show()

                // Yêu cầu quyền
                requestLocationPermission()
            }
            else -> {
                // Yêu cầu quyền lần đầu
                requestLocationPermission()
            }
        }
    }

    private fun getWeatherForCurrentLocation() {
        binding.shimmerLayout.isVisible = true
        viewModel.getWeatherForCurrentLocationAndAdvice(geminiApiKey)
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}