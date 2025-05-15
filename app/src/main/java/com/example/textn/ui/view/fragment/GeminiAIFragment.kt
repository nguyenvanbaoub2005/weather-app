package com.example.textn.ui.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.viewmodel.GeminiViewModel
import com.example.textn.data.model.WeatherData
import com.example.textn.databinding.FragmentGeminiAiBinding
import com.example.textn.viewmodel.GeminiViewModelFactory
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
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

        setupObservers()
        setupClickListeners()
        setupCloseButton()
        checkLocationPermissionAndGetWeather()
    }

    private fun setupObservers() {
        // Theo dõi kết quả từ AI
        viewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            showResponse(response)
        }

        // Theo dõi trạng thái đang tải
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.btnAskWeatherAdvice.isEnabled = !isLoading
            binding.btnAskCustom.isEnabled = !isLoading
        }

        // Theo dõi lỗi
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showError(it)
            }
        }

        // Theo dõi dữ liệu thời tiết
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            updateWeatherUI(weatherData)
        }
    }

    private fun setupClickListeners() {
        // Nút lấy lời khuyên dựa trên dữ liệu thời tiết hiện tại
        binding.btnAskWeatherAdvice.setOnClickListener {
            viewModel.getWeatherForCurrentLocationAndAdvice(geminiApiKey)
        }

        // Nút để hỏi câu hỏi tùy chỉnh
        binding.btnAskCustom.setOnClickListener {
            val customPrompt = binding.etCustomPrompt.text.toString().trim()
            if (customPrompt.isNotEmpty()) {
                viewModel.getCustomAdvice(customPrompt, geminiApiKey)
            } else {
                Toast.makeText(context, "Vui lòng nhập câu hỏi của bạn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCloseButton() {
        binding.btnReturn.setOnClickListener {
            findNavController().navigateUp()
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

        // Cập nhật icon thời tiết dựa vào điều kiện thời tiết
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