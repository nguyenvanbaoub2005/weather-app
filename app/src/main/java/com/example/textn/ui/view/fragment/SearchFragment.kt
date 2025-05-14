package com.example.textn.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.model.LocationData
import com.example.textn.ui.adapter.SearchResultAdapter
import com.example.textn.viewmodel.SearchViewModel
import com.example.textn.viewmodel.SearchViewModelFactory

class SearchFragment : Fragment() {
    private lateinit var viewModel: SearchViewModel
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize views
        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_results_recycler_view)
        backButton = view.findViewById(R.id.back_button)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchResultAdapter { location ->
            // When a location is clicked, navigate to the forecast screen with its coordinates
            navigateToForecast(location.latitude, location.longitude)
        }
        recyclerView.adapter = adapter

        // Initialize ViewModel with Factory
        viewModel = ViewModelProvider(this, SearchViewModelFactory(requireContext()))
            .get(SearchViewModel::class.java)

        // Set up observers
        setupObservers()

        // Set up search functionality
        setupSearch()

        // Set up back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Load all Vietnamese cities by default
        viewModel.searchCities("Vietnam")

        return view
    }

    private fun setupObservers() {
        // Observe search results from repository
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
        }

        // Observe custom location found via Geocoder
        viewModel.customLocation.observe(viewLifecycleOwner) { customLocation ->
            customLocation?.let {
                // If there's a custom location, display it as a single-item list
                adapter.submitList(listOf(it))
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You could add a progress indicator here
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupSearch() {
        // Change the magnifying glass (🔍) icon color to white
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(android.graphics.Color.WHITE)

        // Clear icon click listener
        val clearButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        clearButton?.setOnClickListener {
            val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            editText?.text?.clear()
            searchView.clearFocus()
            viewModel.searchCities("Vietnam") // Reset to show all cities
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        // When user submits query, use the isSubmit flag to indicate direct submission
                        viewModel.searchCities(it, isSubmit = true)
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 2) {
                        // While typing, use the default isSubmit=false
                        viewModel.searchCities(it)
                    } else if (it.isEmpty()) {
                        viewModel.searchCities("Vietnam")
                    }
                }
                return true
            }
        })
    }

    // Navigation to forecast screen
    private fun navigateToForecast(latitude: Double, longitude: Double) {
        val bundle = Bundle().apply {
            putDouble("lat", latitude)
            putDouble("lon", longitude)
        }
        findNavController().navigate(R.id.action_searchFragment_to_tabularForecastFragment, bundle)
    }
}