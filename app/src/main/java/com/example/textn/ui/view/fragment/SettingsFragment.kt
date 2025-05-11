package com.example.textn.ui.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.databinding.FragmentSettingsBinding
import com.example.textn.utils.ThemeManager
import com.example.textn.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setupObservers()
        setupListeners()
        setupCloseButton()
    }

    private fun setupObservers() {
        // Observe theme changes
        viewModel.isDarkTheme.observe(viewLifecycleOwner) { isDarkTheme ->
            binding.switchTheme.isChecked = isDarkTheme
            updateThemeText(isDarkTheme)
        }

        // Observe font size changes
        viewModel.fontSize.observe(viewLifecycleOwner) { fontSize ->
            binding.seekFontSize.progress = fontSize
            applyFontSize(fontSize)
        }
    }

    private fun setupListeners() {
        // Theme switch
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.isDarkTheme.value != isChecked) {
                viewModel.toggleTheme()
                ThemeManager.applyTheme(isChecked)
            }
        }

        // Font size slider
        binding.seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val actualProgress = if (progress < 12) 12 else progress
                    viewModel.setFontSize(actualProgress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Units card
        binding.cardUnit.setOnClickListener {
            findNavController().navigate(R.id.nav_units)
        }

        // Feedback card
        binding.cardFeedback.setOnClickListener {
            sendFeedbackEmail()
        }

        // Legal info card
        binding.cardLegal.setOnClickListener {
            // Navigate to Legal info fragment
            findNavController().navigate(R.id.navigation_legal_info)
        }
    }

    private fun setupCloseButton() {
        binding.buttonClose.setOnClickListener {
            val navController = findNavController()
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id

            if (previousDestinationId == R.id.tabularForecastFragment) {
                navController.navigateUp()
            } else {
                navController.navigate(R.id.nav_home)
            }
        }
    }

    private fun updateThemeText(isDarkTheme: Boolean) {
        binding.tvThemeLabel.text = if (isDarkTheme) {
            getString(R.string.theme_dark)
        } else {
            getString(R.string.theme_light)
        }
    }

    private fun applyFontSize(size: Int) {
        // In a real app, this would apply the font size to the entire app
        // This is just a simple implementation for demo purposes
        binding.tvSettingsTitle.textSize = size.toFloat()
        binding.tvUnitLabel.textSize = size.toFloat()
        binding.tvFeedbackLabel.textSize = size.toFloat()
        binding.tvLegalLabel.textSize = size.toFloat()
        binding.tvThemeLabel.textSize = size.toFloat()
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@weatherapp.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback - Weather App")
        }
        startActivity(Intent.createChooser(intent, "Send Feedback"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}