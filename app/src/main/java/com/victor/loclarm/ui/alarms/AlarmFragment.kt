package com.victor.loclarm.ui.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.victor.loclarm.MainActivity
import com.victor.loclarm.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val alarmAdapter by lazy { AlarmAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        alarmViewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            if (alarms.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.nothingHereText.visibility = View.VISIBLE
                binding.nothingHereImage.visibility = View.VISIBLE
            } else {
                binding.nothingHereImage.visibility = View.GONE
                binding.nothingHereText.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                alarmAdapter.setData(alarms)
            }
        }

        binding.navigationMenu.setOnClickListener {
            (activity as? MainActivity)?.binding?.drawerLayout?.open()
        }

        return root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
