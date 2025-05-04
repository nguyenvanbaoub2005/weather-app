package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.textn.databinding.FragmentLegalInfoBinding
import com.example.textn.R

class LegalInfoFragment : Fragment() {

    private var _binding: FragmentLegalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLegalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        // Setup back button
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }

        // Setup Privacy Policy button
        binding.cardPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.navigation_privacy_policy)
        }

        // Setup Terms of Use button
        binding.cardTermsOfUse.setOnClickListener {
            findNavController().navigate(R.id.navigation_terms_of_use)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}