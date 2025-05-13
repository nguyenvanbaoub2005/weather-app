package com.example.textn.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.ui.adapter.SearchAdapter
import com.example.textn.viewmodel.SearchViewModel
import com.example.textn.viewmodel.SearchViewModelFactory

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var recentAdapter: SearchAdapter
    private lateinit var searchView: SearchView
    private lateinit var recyclerViewResults: RecyclerView
    private lateinit var recyclerViewRecent: RecyclerView
    private lateinit var clearButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, SearchViewModelFactory())[SearchViewModel::class.java]

        // Initialize views
        searchView = view.findViewById(R.id.search_view)
        recyclerViewResults = view.findViewById(R.id.recycler_view_search_results)
        recyclerViewRecent = view.findViewById(R.id.recycler_view_recent)
        clearButton = view.findViewById(R.id.btn_clear_history)

        // Setup recycler views
        setupRecyclerViews()

        // Setup search view
        setupSearchView()

        // Setup clear button
        clearButton.setOnClickListener {
            viewModel.clearRecentSearches()
            recentAdapter.submitList(emptyList())
        }

        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { locations ->
            searchAdapter.submitList(locations)
        }

        // Observe recent searches
        viewModel.recentSearches.observe(viewLifecycleOwner) { locations ->
            recentAdapter.submitList(locations)
        }

        // Load recent searches when fragment is created
        viewModel.loadRecentSearches()
    }

    private fun setupRecyclerViews() {
        // Setup search results recycler view
        searchAdapter = SearchAdapter { location ->
            navigateToForecast(location.latitude, location.longitude)
        }
        recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        // Setup recent searches recycler view
        recentAdapter = SearchAdapter { location ->
            navigateToForecast(location.latitude, location.longitude)
        }
        recyclerViewRecent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentAdapter
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchLocations(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty() && newText.length >= 2) {
                    viewModel.searchLocations(newText)
                }
                return true
            }
        })

        // Make search view automatically focused
        searchView.isIconified = false
        searchView.requestFocus()
    }

    private fun navigateToForecast(lat: Double, lon: Double) {
        val bundle = Bundle().apply {
            putDouble("lat", lat)
            putDouble("lon", lon)
        }
        findNavController().navigate(R.id.action_searchFragment_to_tabularForecastFragment, bundle)
    }
}