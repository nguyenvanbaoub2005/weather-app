package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.textn.R
import com.example.textn.databinding.FragmentUnitsBinding
import com.example.textn.viewmodel.SettingsViewModel
import com.google.android.material.slider.Slider

class UnitsFragment : Fragment() {

    private var _binding: FragmentUnitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

        setupObservers()
        setupListeners()
        setupCloseButton()
        setupWeightSlider()
    }

    private fun setupObservers() {
        // Existing observers
        viewModel.weatherWidgetUnit.observe(viewLifecycleOwner) { unit ->
            updateWeatherWidgetButtons(unit)
        }

        viewModel.favoritePlacesUnit.observe(viewLifecycleOwner) { unit ->
            updateFavoritePlacesButtons(unit)
        }

        viewModel.roseDiagramMode.observe(viewLifecycleOwner) { mode ->
            updateRoseDiagramButtons(mode)
        }

        viewModel.windSpeedUnit.observe(viewLifecycleOwner) { unit ->
            updateWindSpeedButtons(unit)
        }

        viewModel.currentSpeedUnit.observe(viewLifecycleOwner) { unit ->
            updateCurrentSpeedButtons(unit)
        }

        // New observers for XML units
        viewModel.temperatureUnit.observe(viewLifecycleOwner) { unit ->
            updateTemperatureButtons(unit)
        }

        viewModel.pressureUnit.observe(viewLifecycleOwner) { unit ->
            updatePressureButtons(unit)
        }

        viewModel.tideUnit.observe(viewLifecycleOwner) { unit ->
            updateTideButtons(unit)
        }

        viewModel.distanceUnit.observe(viewLifecycleOwner) { unit ->
            updateDistanceButtons(unit)
        }

        viewModel.heightUnit.observe(viewLifecycleOwner) { unit ->
            updateHeightButtons(unit)
        }

        viewModel.precipitationUnit.observe(viewLifecycleOwner) { unit ->
            updatePrecipitationButtons(unit)
        }

        viewModel.timeFormat.observe(viewLifecycleOwner) { format ->
            updateTimeFormatButtons(format)
        }

        viewModel.forecastPeriod.observe(viewLifecycleOwner) { period ->
            updateForecastPeriodButtons(period)
        }

        viewModel.userWeight.observe(viewLifecycleOwner) { weight ->
            updateWeightDisplay(weight)
        }
    }

    private fun setupListeners() {
        // Existing listeners for original units will remain the same
        // Weather Widget Toggle Group
        binding.btnWeatherOff.setOnClickListener {
            viewModel.setWeatherWidgetUnit(SettingsViewModel.UNIT_OFF)
        }
        binding.btnWeatherWindDirection.setOnClickListener {
            viewModel.setWeatherWidgetUnit(SettingsViewModel.UNIT_WIND)
        }
        binding.btnWeatherWaveHeight.setOnClickListener {
            viewModel.setWeatherWidgetUnit(SettingsViewModel.UNIT_WAVE)
        }
        binding.btnWeatherTemperature.setOnClickListener {
            viewModel.setWeatherWidgetUnit(SettingsViewModel.UNIT_TEMP)
        }
        binding.btnWeatherSnow.setOnClickListener {
            viewModel.setWeatherWidgetUnit(SettingsViewModel.UNIT_SNOW)
        }

        // Favorite Places Toggle Group
        binding.btnFavoritesOff.setOnClickListener {
            viewModel.setFavoritePlacesUnit(SettingsViewModel.UNIT_OFF)
        }
        binding.btnFavoritesWindDirection.setOnClickListener {
            viewModel.setFavoritePlacesUnit(SettingsViewModel.UNIT_WIND)
        }
        binding.btnFavoritesWaveHeight.setOnClickListener {
            viewModel.setFavoritePlacesUnit(SettingsViewModel.UNIT_WAVE)
        }
        binding.btnFavoritesTemperature.setOnClickListener {
            viewModel.setFavoritePlacesUnit(SettingsViewModel.UNIT_TEMP)
        }
        binding.btnFavoritesSnow.setOnClickListener {
            viewModel.setFavoritePlacesUnit(SettingsViewModel.UNIT_SNOW)
        }

        // Rose Diagram Buttons
        binding.btnCompass.setOnClickListener {
            viewModel.setRoseDiagramMode(SettingsViewModel.MODE_COMPASS)
        }
        binding.btnNorth.setOnClickListener {
            viewModel.setRoseDiagramMode(SettingsViewModel.MODE_SATELLITE)
        }

        // Wind Speed Buttons
        binding.btnMps.setOnClickListener {
            viewModel.setWindSpeedUnit(SettingsViewModel.SPEED_MPS)
        }
        binding.btnMph.setOnClickListener {
            viewModel.setWindSpeedUnit(SettingsViewModel.SPEED_MPH)
        }
        binding.btnKmh.setOnClickListener {
            viewModel.setWindSpeedUnit(SettingsViewModel.SPEED_KPH)
        }
        binding.btnKnots.setOnClickListener {
            viewModel.setWindSpeedUnit(SettingsViewModel.SPEED_KNOTS)
        }

        // Current Speed Buttons
        binding.btnMpsCurrent.setOnClickListener {
            viewModel.setCurrentSpeedUnit(SettingsViewModel.SPEED_MPS)
        }
        binding.btnMphCurrent.setOnClickListener {
            viewModel.setCurrentSpeedUnit(SettingsViewModel.SPEED_MPH)
        }
        binding.btnKphCurrent.setOnClickListener {
            viewModel.setCurrentSpeedUnit(SettingsViewModel.SPEED_KPH)
        }
        binding.btnKnotsCurrent.setOnClickListener {
            viewModel.setCurrentSpeedUnit(SettingsViewModel.SPEED_KNOTS)
        }

        // New listeners for additional units in XML

        // Temperature Buttons
        binding.btnCelsius.setOnClickListener {
            viewModel.setTemperatureUnit(SettingsViewModel.TEMP_CELSIUS)
        }
        binding.btnFahrenheit.setOnClickListener {
            viewModel.setTemperatureUnit(SettingsViewModel.TEMP_FAHRENHEIT)
        }
        binding.btnKelvin.setOnClickListener {
            viewModel.setTemperatureUnit(SettingsViewModel.TEMP_KELVIN)
        }

        // Pressure Buttons
        binding.btnHpa.setOnClickListener {
            viewModel.setPressureUnit(SettingsViewModel.PRESSURE_HPA)
        }
        binding.btnInhg.setOnClickListener {
            viewModel.setPressureUnit(SettingsViewModel.PRESSURE_INHG)
        }
        binding.btnMmhg.setOnClickListener {
            viewModel.setPressureUnit(SettingsViewModel.PRESSURE_MMHG)
        }

        // Tide Level Buttons
        binding.btnLat.setOnClickListener {
            viewModel.setTideUnit(SettingsViewModel.TIDE_LAT)
        }
        binding.btnMllw.setOnClickListener {
            viewModel.setTideUnit(SettingsViewModel.TIDE_MLLW)
        }
        binding.btnMsl.setOnClickListener {
            viewModel.setTideUnit(SettingsViewModel.TIDE_MSL)
        }

        // Distance Buttons
        binding.btnKilometers.setOnClickListener {
            viewModel.setDistanceUnit(SettingsViewModel.DISTANCE_KM)
        }
        binding.btnMiles.setOnClickListener {
            viewModel.setDistanceUnit(SettingsViewModel.DISTANCE_MILES)
        }
        binding.btnNauticalMiles.setOnClickListener {
            viewModel.setDistanceUnit(SettingsViewModel.DISTANCE_NAUTICAL)
        }

        // Height Buttons
        binding.btnMeters.setOnClickListener {
            viewModel.setHeightUnit(SettingsViewModel.HEIGHT_METERS)
        }
        binding.btnFeet.setOnClickListener {
            viewModel.setHeightUnit(SettingsViewModel.HEIGHT_FEET)
        }

        // Precipitation Buttons
        binding.btnMm.setOnClickListener {
            viewModel.setPrecipitationUnit(SettingsViewModel.PRECIP_MM)
        }
        binding.btnInch.setOnClickListener {
            viewModel.setPrecipitationUnit(SettingsViewModel.PRECIP_INCH)
        }

        // Time Format Buttons
        binding.btn24h.setOnClickListener {
            viewModel.setTimeFormat(SettingsViewModel.TIME_24H)
        }
        binding.btn12h.setOnClickListener {
            viewModel.setTimeFormat(SettingsViewModel.TIME_12H)
        }

        // Forecast Period Buttons
        binding.btn1Hour.setOnClickListener {
            viewModel.setForecastPeriod(SettingsViewModel.FORECAST_1H)
        }
        binding.btn3Hours.setOnClickListener {
            viewModel.setForecastPeriod(SettingsViewModel.FORECAST_3H)
        }
    }

    private fun setupCloseButton() {
        binding.buttonClose.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }
    }

    private fun setupWeightSlider() {
        binding.sliderWeight.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                viewModel.setUserWeight(value.toInt())
            }
        }
    }

    // Existing update methods for original units will remain the same
    private fun updateWeatherWidgetButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnWeatherOff.setBackgroundColor(darkBlueCardColor)
        binding.btnWeatherWindDirection.setBackgroundColor(darkBlueCardColor)
        binding.btnWeatherWaveHeight.setBackgroundColor(darkBlueCardColor)
        binding.btnWeatherTemperature.setBackgroundColor(darkBlueCardColor)
        binding.btnWeatherSnow.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.UNIT_OFF -> binding.btnWeatherOff.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_WIND -> binding.btnWeatherWindDirection.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_WAVE -> binding.btnWeatherWaveHeight.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_TEMP -> binding.btnWeatherTemperature.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_SNOW -> binding.btnWeatherSnow.setBackgroundColor(primaryColor)
        }
    }

    private fun updateFavoritePlacesButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnFavoritesOff.setBackgroundColor(darkBlueCardColor)
        binding.btnFavoritesWindDirection.setBackgroundColor(darkBlueCardColor)
        binding.btnFavoritesWaveHeight.setBackgroundColor(darkBlueCardColor)
        binding.btnFavoritesTemperature.setBackgroundColor(darkBlueCardColor)
        binding.btnFavoritesSnow.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.UNIT_OFF -> binding.btnFavoritesOff.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_WIND -> binding.btnFavoritesWindDirection.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_WAVE -> binding.btnFavoritesWaveHeight.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_TEMP -> binding.btnFavoritesTemperature.setBackgroundColor(primaryColor)
            SettingsViewModel.UNIT_SNOW -> binding.btnFavoritesSnow.setBackgroundColor(primaryColor)
        }
    }

    private fun updateRoseDiagramButtons(mode: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnCompass.setBackgroundColor(darkBlueCardColor)
        binding.btnNorth.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (mode) {
            SettingsViewModel.MODE_COMPASS -> binding.btnCompass.setBackgroundColor(primaryColor)
            SettingsViewModel.MODE_SATELLITE -> binding.btnNorth.setBackgroundColor(primaryColor)
        }
    }

    private fun updateWindSpeedButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnMps.setBackgroundColor(darkBlueCardColor)
        binding.btnMph.setBackgroundColor(darkBlueCardColor)
        binding.btnKmh.setBackgroundColor(darkBlueCardColor)
        binding.btnKnots.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.SPEED_MPS -> binding.btnMps.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_MPH -> binding.btnMph.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_KPH -> binding.btnKmh.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_KNOTS -> binding.btnKnots.setBackgroundColor(primaryColor)
        }
    }

    private fun updateCurrentSpeedButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnMpsCurrent.setBackgroundColor(darkBlueCardColor)
        binding.btnMphCurrent.setBackgroundColor(darkBlueCardColor)
        binding.btnKphCurrent.setBackgroundColor(darkBlueCardColor)
        binding.btnKnotsCurrent.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.SPEED_MPS -> binding.btnMpsCurrent.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_MPH -> binding.btnMphCurrent.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_KPH -> binding.btnKphCurrent.setBackgroundColor(primaryColor)
            SettingsViewModel.SPEED_KNOTS -> binding.btnKnotsCurrent.setBackgroundColor(primaryColor)
        }
    }

    // New update methods for additional units from XML
    private fun updateTemperatureButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnCelsius.setBackgroundColor(darkBlueCardColor)
        binding.btnFahrenheit.setBackgroundColor(darkBlueCardColor)
        binding.btnKelvin.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.TEMP_CELSIUS -> binding.btnCelsius.setBackgroundColor(primaryColor)
            SettingsViewModel.TEMP_FAHRENHEIT -> binding.btnFahrenheit.setBackgroundColor(primaryColor)
            SettingsViewModel.TEMP_KELVIN -> binding.btnKelvin.setBackgroundColor(primaryColor)
        }
    }

    private fun updatePressureButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnHpa.setBackgroundColor(darkBlueCardColor)
        binding.btnInhg.setBackgroundColor(darkBlueCardColor)
        binding.btnMmhg.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.PRESSURE_HPA -> binding.btnHpa.setBackgroundColor(primaryColor)
            SettingsViewModel.PRESSURE_INHG -> binding.btnInhg.setBackgroundColor(primaryColor)
            SettingsViewModel.PRESSURE_MMHG -> binding.btnMmhg.setBackgroundColor(primaryColor)
        }
    }

    private fun updateTideButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnLat.setBackgroundColor(darkBlueCardColor)
        binding.btnMllw.setBackgroundColor(darkBlueCardColor)
        binding.btnMsl.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.TIDE_LAT -> binding.btnLat.setBackgroundColor(primaryColor)
            SettingsViewModel.TIDE_MLLW -> binding.btnMllw.setBackgroundColor(primaryColor)
            SettingsViewModel.TIDE_MSL -> binding.btnMsl.setBackgroundColor(primaryColor)
        }
    }

    private fun updateDistanceButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnKilometers.setBackgroundColor(darkBlueCardColor)
        binding.btnMiles.setBackgroundColor(darkBlueCardColor)
        binding.btnNauticalMiles.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.DISTANCE_KM -> binding.btnKilometers.setBackgroundColor(primaryColor)
            SettingsViewModel.DISTANCE_MILES -> binding.btnMiles.setBackgroundColor(primaryColor)
            SettingsViewModel.DISTANCE_NAUTICAL -> binding.btnNauticalMiles.setBackgroundColor(primaryColor)
        }
    }

    private fun updateHeightButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnMeters.setBackgroundColor(darkBlueCardColor)
        binding.btnFeet.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.HEIGHT_METERS -> binding.btnMeters.setBackgroundColor(primaryColor)
            SettingsViewModel.HEIGHT_FEET -> binding.btnFeet.setBackgroundColor(primaryColor)
        }
    }

    private fun updatePrecipitationButtons(unit: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btnMm.setBackgroundColor(darkBlueCardColor)
        binding.btnInch.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (unit) {
            SettingsViewModel.PRECIP_MM -> binding.btnMm.setBackgroundColor(primaryColor)
            SettingsViewModel.PRECIP_INCH -> binding.btnInch.setBackgroundColor(primaryColor)
        }
    }

    private fun updateTimeFormatButtons(format: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btn24h.setBackgroundColor(darkBlueCardColor)
        binding.btn12h.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (format) {
            SettingsViewModel.TIME_24H -> binding.btn24h.setBackgroundColor(primaryColor)
            SettingsViewModel.TIME_12H -> binding.btn12h.setBackgroundColor(primaryColor)
        }
    }

    private fun updateForecastPeriodButtons(period: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val darkBlueCardColor = ContextCompat.getColor(requireContext(), R.color.dark_blue_card)

        // Reset all buttons to default color
        binding.btn1Hour.setBackgroundColor(darkBlueCardColor)
        binding.btn3Hours.setBackgroundColor(darkBlueCardColor)

        // Set selected button to primary color
        when (period) {
            SettingsViewModel.FORECAST_1H -> binding.btn1Hour.setBackgroundColor(primaryColor)
            SettingsViewModel.FORECAST_3H -> binding.btn3Hours.setBackgroundColor(primaryColor)
        }
    }

    private fun updateWeightDisplay(weight: Int) {
        binding.sliderWeight.value = weight.toFloat()
        binding.textWeightValue.text = "Cân nặng của bạn: $weight lbs"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}