package com.example.textn.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.textn.data.model.WeatherResponse
import com.example.textn.data.repository.WeatherRepository
import com.example.textn.databinding.FragmentWeatherBinding
import com.example.textn.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeatherHelper(
    private val context: Context,
    private val binding: FragmentWeatherBinding,
    private val viewModel: WeatherViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val fragment: Fragment
) {
    companion object {
        const val API_KEY = "32ea3752b81cf12722a46358a7a9739c"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private var isLocationFetched = false

        fun resetLocationFetched() {
            isLocationFetched = false
            Log.d("Weather", "Đã reset trạng thái lấy vị trí")
        }

        fun setupWebView(webView: WebView) {
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowContentAccess = true
                allowFileAccess = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webView.webViewClient = WebViewClient()
        }

        fun updateWindyMap(context: Context, webView: WebView, location: String, layer: String = "wind") {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val result = geocoder.getFromLocationName(location, 1)

                val lat = if (!result.isNullOrEmpty()) result[0].latitude else 16.0471
                val lon = if (!result.isNullOrEmpty()) result[0].longitude else 108.2062

                val html = generateWindyHtml(lat, lon, layer)
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi tải bản đồ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        fun updateMapLayer(webView: WebView, layer: String, lat: Double, lon: Double) {
            val html = generateWindyHtml(lat, lon, layer)
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            Log.d("Weather", "Cập nhật lớp thời tiết sang: $layer tại vị trí: $lat, $lon")
        }

        fun updateWindyMapWithCurrentLocation(
            activity: Activity,
            webView: WebView,
            layer: String = "wind",
            onLocationFetched: ((lat: Double, lon: Double) -> Unit)? = null
        ) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    updateWindyMapHtml(webView, lat, lon, layer)
                    onLocationFetched?.invoke(lat, lon)
                    isLocationFetched = true
                } else {
                    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 1000
                        fastestInterval = 500
                        numUpdates = 1
                    }

                    val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            val freshLocation = locationResult.lastLocation
                            if (freshLocation != null) {
                                val lat = freshLocation.latitude
                                val lon = freshLocation.longitude
                                updateWindyMapHtml(webView, lat, lon, layer)
                                onLocationFetched?.invoke(lat, lon)
                                isLocationFetched = true
                            } else {
                                Toast.makeText(activity, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                }
            }
        }

        private fun updateWindyMapHtml(webView: WebView, lat: Double, lon: Double, layer: String) {
            val html = generateWindyHtml(lat, lon, layer)
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }

        private fun generateWindyHtml(lat: Double, lon: Double, layer: String): String {
            return """
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            html, body { margin: 0; height: 100%; overflow: hidden; }
            iframe { width: 100%; height: 100%; border: none; }
          </style>
        </head>
        <body>
          <iframe id="windyFrame"
            src="https://embed.windy.com/embed2.html?lat=$lat&lon=$lon&detailLat=$lat&detailLon=$lon&width=100%25&height=100%25&zoom=7&level=surface&overlay=$layer&menu=&message=&marker=&calendar=24&pressure=&type=map&location=coordinates&detail=&metricWind=default&metricTemp=default&radarRange=-1&product=gfs"
          ></iframe>
        </body>
        </html>
    """.trimIndent()
        }

        fun getCoordinatesFromLocation(context: Context, location: String): Pair<Double, Double>? {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val result = geocoder.getFromLocationName(location, 1)

                return if (!result.isNullOrEmpty()) {
                    Pair(result[0].latitude, result[0].longitude)
                } else {
                    null
                }
            } catch (e: Exception) {
                return null
            }
        }

        fun getLocationFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                return if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    if (address.locality != null) {
                        address.locality
                    } else if (address.subAdminArea != null) {
                        address.subAdminArea
                    } else {
                        address.adminArea ?: "Unknown Location"
                    }
                } else {
                    "Unknown Location"
                }
            } catch (e: Exception) {
                return "Unknown Location"
            }
        }
    }

    private var currentLayer = "wind"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun initialize() {
        setupBindingWebView()
        setupSearchListener()
        setupSearchButtonListener() // Thêm listener cho nút tìm kiếm
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        updateWindyMapWithCurrentLocation()
    }

    private fun setupBindingWebView() {
        val webView: WebView = binding.windyWebView
        setupWebView(webView)
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                Toast.makeText(context, "Lỗi tải bản đồ: ${error.description}", Toast.LENGTH_SHORT).show()
            }
        }
        updateWindyMap("Da Nang")
    }

    private fun setupSearchListener() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    fetchLocationWeather(it)
                    updateWindyMap(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }

    // Thêm listener cho nút tìm kiếm
    private fun setupSearchButtonListener() {
        binding.btnSearch.setOnClickListener {
            val query = binding.searchView.query.toString()
            if (query.isNotBlank()) {
                fetchLocationWeather(query)
                updateWindyMap(query)
            } else {
                Toast.makeText(context, "Vui lòng nhập địa điểm để tìm kiếm!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchLocationWeather(location: String) {
        val coordinates = getCoordinatesFromLocation(context, location)
        if (coordinates != null) {
            viewModel.fetchWeather(coordinates.first, coordinates.second, API_KEY)
        } else {
            Toast.makeText(context, "Không tìm thấy địa điểm!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateWindyMap(location: String) {
        updateWindyMap(context, binding.windyWebView, location, currentLayer)
    }

    fun updateWindyMapWithCurrentLocation(onLocationFetched: ((lat: Double, lon: Double) -> Unit)? = null) {
        val activity = fragment.requireActivity()
        updateWindyMapWithCurrentLocation(activity, binding.windyWebView, currentLayer) { lat, lon ->
            viewModel.fetchWeather(lat, lon, API_KEY)
            onLocationFetched?.invoke(lat, lon)
        }
    }
}