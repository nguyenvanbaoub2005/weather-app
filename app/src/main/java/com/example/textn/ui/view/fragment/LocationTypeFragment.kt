package com.example.yourapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.textn.databinding.FragmentLocationTypeSelectionBinding
import com.example.textn.viewmodel.GeminiViewModel
import android.location.Location

class LocationTypeFragment : Fragment() {

    private val geminiApiKey = "AIzaSyD647aAzMdwe0biy5gu_JP0jmEw1UDg3LQ"
    private var _binding: FragmentLocationTypeSelectionBinding? = null
    private val binding get() = _binding!!

    private val geminiViewModel: GeminiViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationTypeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show location type dialog directly when cards are clicked
        setupCardClickListeners()
//        observeViewModel()
    }

    private fun setupCardClickListeners() {
        // Set click listeners for each card to directly show the dialog
        binding.cardAll.setOnClickListener { showLocationTypeDialog() }
        binding.cardFood.setOnClickListener { showLocationTypeDialog() }
        binding.cardEntertainment.setOnClickListener { showLocationTypeDialog() }
        binding.cardShopping.setOnClickListener { showLocationTypeDialog() }
        binding.cardAccommodation.setOnClickListener { showLocationTypeDialog() }

        // Also add listener to continue button if you want to keep it
        binding.btnContinue.setOnClickListener { showLocationTypeDialog() }
    }

    // Dialog approach from HomeFragment
    private fun showLocationTypeDialog() {
        val locationTypes = arrayOf("Tất cả", "Ăn uống", "Giải trí", "Mua sắm", "Lưu trú")
        val locationTypeValues = arrayOf("all", "food", "entertainment", "shopping", "accommodation")

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn loại địa điểm")
            .setItems(locationTypes) { _, which ->
                // Get selected location type
                val selectedLocationType = locationTypeValues[which]

                // Show loading indicator
                showLoading(true)

                // Call API to get location suggestions
                geminiViewModel.getSuggestedLocationsNearby(
                    geminiApiKey,
                    numberOfLocations = 5,
                    locationType = selectedLocationType
                )
            }
            .show()
    }

//    private fun observeViewModel() {
//        geminiViewModel.suggestedLocations.observe(viewLifecycleOwner) { result ->
//            showLoading(false)
//
//            when (result) {
//                is Resource.Success -> {
//                    // Navigate to results screen
//                    navigateToResultsScreen(result.data)
//                }
//                is Resource.Error -> {
//                    // Show error message
//                    Toast.makeText(
//                        requireContext(),
//                        "Có lỗi xảy ra: ${result.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                is Resource.Loading -> {
//                    // Already handled in showLoading()
//                }
//            }
//        }
//    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnContinue.isEnabled = !isLoading
    }

    private fun navigateToResultsScreen(locations: List<Location>) {
        // Navigation component or FragmentTransaction to navigate to results screen
        // Example: findNavController().navigate(R.id.action_locationTypeFragment_to_resultsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}