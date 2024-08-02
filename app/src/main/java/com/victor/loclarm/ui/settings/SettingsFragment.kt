package com.victor.loclarm.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.victor.loclarm.MainActivity
import com.victor.loclarm.R
import com.victor.loclarm.databinding.FragmentSettingsBinding
import com.victor.loclarm.db.model.UserSettings

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        settingsViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[SettingsViewModel::class.java]

        val languageAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.languages_array,
            android.R.layout.simple_spinner_item
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = languageAdapter

        val unitsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.units_array,
            android.R.layout.simple_spinner_item
        )
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnits.adapter = unitsAdapter

        binding.navigationMenu.setOnClickListener {
            (activity as? MainActivity)?.binding?.drawerLayout?.open()
        }

        binding.buttonSave.setOnClickListener {
            saveSettings()
        }

        settingsViewModel.userSettings.observe(viewLifecycleOwner) { userSettings ->
            userSettings?.let { populateSettings(it) }
        }

        settingsViewModel.loadSettings()

        return root
    }

    private fun saveSettings() {
        val language = binding.spinnerLanguage.selectedItem.toString()
        val units = binding.spinnerUnits.selectedItem.toString()
        val ringtone = "default"
        val volume = binding.seekbarVolume.progress
        val vibration = binding.switchVibration.isChecked

        val userSettings = UserSettings(
            language = language,
            units = units,
            ringtone = ringtone,
            volume = volume,
            vibration = vibration
        )

        settingsViewModel.saveSettings(userSettings)
        Snackbar.make(requireView(), "Settings saved", Snackbar.LENGTH_SHORT).show()
    }

    private fun populateSettings(userSettings: UserSettings) {
        val languagePosition = (binding.spinnerLanguage.adapter as ArrayAdapter<String>).getPosition(userSettings.language)
        binding.spinnerLanguage.setSelection(languagePosition)

        val unitsPosition = (binding.spinnerUnits.adapter as ArrayAdapter<String>).getPosition(userSettings.units)
        binding.spinnerUnits.setSelection(unitsPosition)

        binding.seekbarVolume.progress = userSettings.volume
        binding.switchVibration.isChecked = userSettings.vibration
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}