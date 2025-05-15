package com.example.textn.ui.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.data.model.PostLocation
import com.example.textn.viewmodel.CommunityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddPostFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var imagePreview: ImageView
    private lateinit var layoutPlaceholder: View
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnClose: View
    private lateinit var locationLayout: LinearLayout
    private lateinit var locationText: TextView
    private lateinit var cardImage: CardView

    private var selectedImageUri: Uri? = null
    private var currentLocation: PostLocation? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getLastLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Cần quyền truy cập vị trí để đăng bài",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imagePreview.setImageURI(uri)
                layoutPlaceholder.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]

        // Khởi tạo FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Ánh xạ view
        imagePreview = view.findViewById(R.id.iv_image_preview)
        layoutPlaceholder = view.findViewById(R.id.layout_placeholder)
        descriptionEditText = view.findViewById(R.id.et_description)
        submitButton = view.findViewById(R.id.btn_submit)
        progressBar = view.findViewById(R.id.progress_bar)
        btnClose = view.findViewById(R.id.btn_close)
        locationLayout = view.findViewById(R.id.layout_location)
        locationText = view.findViewById(R.id.tv_location)
        cardImage = view.findViewById(R.id.card_image)

        // Thiết lập sự kiện click cho nút đóng
        btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        // Thiết lập sự kiện chọn ảnh
        cardImage.setOnClickListener {
            openGallery()
        }

        // Thiết lập sự kiện cho nút chọn vị trí
        locationLayout.setOnClickListener {
            checkLocationPermission()
        }

        // Nút đăng bài
        submitButton.setOnClickListener {
            uploadPost()
        }

        // Quan sát trạng thái loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            submitButton.isEnabled = !isLoading
        }

        // Quan sát thông báo lỗi
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Quan sát trạng thái upload
        viewModel.uploadStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(
                    requireContext(),
                    "Đăng bài thành công!",
                    Toast.LENGTH_SHORT
                ).show()
                // Quay lại màn hình Community
                findNavController().navigateUp()
            }
        }

        // Yêu cầu quyền truy cập vị trí khi mở màn hình
        checkLocationPermission()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Giải thích lý do cần quyền truy cập vị trí
                Toast.makeText(
                    requireContext(),
                    "Ứng dụng cần quyền truy cập vị trí để đăng bài",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                // Yêu cầu quyền
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLastLocation() {
        try {
            // Kiểm tra quyền truy cập vị trí
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    requireContext(),
                    "Cần quyền truy cập vị trí",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Lấy vị trí thành công
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val locationName = getLocationName(
                                requireContext(),
                                location.latitude,
                                location.longitude
                            )
                            currentLocation = PostLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationName = locationName ?: "Vị trí hiện tại"
                            )
                            // Cập nhật UI
                            locationText.text = currentLocation?.locationName ?: "Lựa chọn vị trí"
                            Toast.makeText(
                                requireContext(),
                                "Đã lấy được vị trí: ${currentLocation?.locationName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Lỗi khi lấy tên địa điểm: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            currentLocation = PostLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationName = "Vị trí hiện tại"
                            )
                            locationText.text = currentLocation?.locationName ?: "Lựa chọn vị trí"
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không thể lấy vị trí hiện tại. Vui lòng thử lại sau.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Lỗi quyền truy cập vị trí",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun getLocationName(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Tạo tên địa điểm từ các thành phần địa chỉ
                    val addressLine = address.getAddressLine(0) // Địa chỉ đầy đủ
                    addressLine ?: "${address.locality ?: ""}, ${address.countryName ?: ""}".trim()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun uploadPost() {
        val imageUri = selectedImageUri
        val description = descriptionEditText.text.toString().trim()
        val location = currentLocation

        if (imageUri == null) {
            Toast.makeText(
                requireContext(),
                "Vui lòng chọn một hình ảnh",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Vui lòng nhập mô tả",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (location == null) {
            Toast.makeText(
                requireContext(),
                "Đang lấy vị trí. Vui lòng thử lại sau.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Tải lên bài đăng
        viewModel.uploadPostWithImage(imageUri, description, location)
    }
}