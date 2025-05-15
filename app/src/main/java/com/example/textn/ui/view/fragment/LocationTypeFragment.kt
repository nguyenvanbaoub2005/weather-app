package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.databinding.FragmentLocationTypeSelectionBinding
import com.example.textn.viewmodel.GeminiViewModel
import com.example.textn.viewmodel.GeminiViewModelFactory
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

class LocationTypeFragment : Fragment() {

    private val geminiApiKey = "AIzaSyD647aAzMdwe0biy5gu_JP0jmEw1UDg3LQ"
    private var _binding: FragmentLocationTypeSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var geminiViewModel: GeminiViewModel
    private var selectedLocationType: String = "all"
    private var lastSelectedCard: CardView? = null
    private lateinit var markwon: Markwon

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationTypeSelectionBinding.inflate(inflater, container, false)

        // Khởi tạo Markwon
        markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .build()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo ViewModel với factory
        val factory = GeminiViewModelFactory(requireContext())
        geminiViewModel = ViewModelProvider(this, factory).get(GeminiViewModel::class.java)

        setupCardClickListeners()
        setupGeminiObservers()
        setupCloseButton()

        // Ẩn kết quả AI ban đầu
        binding.tvAIResult.visibility = View.GONE
        binding.tvResultTitle.visibility = View.GONE
        binding.loadingContainer.visibility = View.GONE  // Ẩn container loading ban đầu
    }

    private fun setupCloseButton() {
        binding.btnReturn.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.nav_home)
        }
    }

    private fun setupCardClickListeners() {
        // Thiết lập click listener cho từng card
        binding.cardAll.setOnClickListener {
            processLocationTypeSelection("all", binding.cardAll)
        }

        binding.cardFood.setOnClickListener {
            processLocationTypeSelection("food", binding.cardFood)
        }

        binding.cardEntertainment.setOnClickListener {
            processLocationTypeSelection("entertainment", binding.cardEntertainment)
        }

        binding.cardShopping.setOnClickListener {
            processLocationTypeSelection("shopping", binding.cardShopping)
        }

        binding.cardAccommodation.setOnClickListener {
            processLocationTypeSelection("accommodation", binding.cardAccommodation)
        }
    }

    private fun processLocationTypeSelection(locationType: String, cardView: CardView) {
        // Lưu loại địa điểm được chọn
        selectedLocationType = locationType

        // Highlight card được chọn
        highlightSelectedCard(cardView)

        // Hiển thị tiêu đề kết quả
        binding.tvResultTitle.visibility = View.VISIBLE

        // Đổi tiêu đề dựa trên loại địa điểm được chọn
        updateResultTitle(locationType)

        // Hiển thị loading
        binding.loadingContainer.visibility = View.VISIBLE

        // Gọi API để lấy gợi ý
        getAISuggestions(locationType)
    }

    private fun updateResultTitle(locationType: String) {
        binding.tvResultTitle.text = when (locationType) {
            "all" -> "Các địa điểm gợi ý gần đây"
            "food" -> "Các địa điểm ăn uống gợi ý"
            "entertainment" -> "Các địa điểm giải trí gợi ý"
            "shopping" -> "Các địa điểm mua sắm gợi ý"
            "accommodation" -> "Các địa điểm lưu trú gợi ý"
            else -> "Các địa điểm gợi ý"
        }
    }

    private fun getAISuggestions(locationType: String) {
        // Gọi API để lấy gợi ý
        geminiViewModel.getSuggestedLocationsNearbyEntertaiment(
            geminiApiKey,
            numberOfLocations = 5,
            locationType = locationType,
        )
    }

    private fun highlightSelectedCard(selectedCard: CardView) {
        // Reset trạng thái của card đã chọn trước đó (nếu có)
        lastSelectedCard?.let {
            it.setCardBackgroundColor(resources.getColor(android.R.color.white, null))
            it.cardElevation = resources.getDimension(R.dimen.card_elevation_normal)
        }

        // Highlight card mới được chọn
        selectedCard.setCardBackgroundColor(resources.getColor(R.color.divider_light, null))
        selectedCard.cardElevation = resources.getDimension(R.dimen.card_elevation_selected)

        // Lưu card đã chọn
        lastSelectedCard = selectedCard
    }

    // Thiết lập observers cho GeminiViewModel
    private fun setupGeminiObservers() {
        geminiViewModel.aiResponse.observe(viewLifecycleOwner, Observer { response ->
            // Ẩn loading khi nhận được kết quả
            binding.loadingContainer.visibility = View.GONE

            // Hiển thị kết quả
            displayAIResultsInView(formatAIResponse(response, selectedLocationType))
        })

        geminiViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        geminiViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            // Ẩn loading khi có lỗi
            binding.loadingContainer.visibility = View.GONE

            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                binding.tvAIResult.visibility = View.VISIBLE
                binding.tvAIResult.text = "Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử lại sau."
            }
        })
    }

    private fun formatAIResponse(response: String, locationType: String): String {
        // Kiểm tra nếu response trống hoặc không hợp lệ
        if (response.isBlank()) {
            return "Không có kết quả phù hợp. Vui lòng thử lại sau."
        }

        // Thêm định dạng Markdown cho các phần của phản hồi
        var formattedResponse = response

        // Bước 1: Đảm bảo các dòng trống giữa các mục
        formattedResponse = formattedResponse.replace("\\n(\\d+\\.)", "\n\n$1")

        // Bước 2: In đậm cho các tiêu đề địa điểm
        formattedResponse = formattedResponse.replace("(\\d+\\.)\\s+([^\\n]+)", "$1 **$2**")

        // Bước 3: In đậm các nhãn
        formattedResponse = formattedResponse.replace("(Địa điểm:|Lý do:|Gợi ý:|Khoảng cách:)", "**$1**")

        return formattedResponse
    }

    private fun displayAIResultsInView(suggestions: String) {
        // Hiển thị kết quả AI trong TextView
        binding.tvAIResult.visibility = View.VISIBLE

        try {
            // Sử dụng Markwon để hiển thị văn bản Markdown
            markwon.setMarkdown(binding.tvAIResult, suggestions)
        } catch (e: Exception) {
            // Fallback nếu có lỗi với Markwon
            binding.tvAIResult.text = suggestions
        }

        // Cuộn xuống để xem kết quả
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}