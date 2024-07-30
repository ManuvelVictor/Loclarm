package com.victor.loclarm.ui.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.victor.loclarm.databinding.FragmentGalleryBinding

class AlarmFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val alarmAdapter by lazy { AlarmAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        alarmViewModel.alarms.observe(viewLifecycleOwner, Observer { alarms ->
            alarmAdapter.setData(alarms)
        })

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
