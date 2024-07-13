package com.victor.loclarm.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.victor.loclarm.MainActivity
import com.victor.loclarm.R
import com.victor.loclarm.databinding.FragmentHomeBinding
import com.victor.loclarm.utils.utils


class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().window.decorView.windowInsetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding.menu.setOnClickListener {
            (activity as? MainActivity)?.binding?.drawerLayout?.open()
        }

        binding.currentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.googleMapsView.onCreate(savedInstanceState)
        binding.googleMapsView.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.GOOGLE_MAPS_API_KEY))
        }
        placesClient = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupSearchView()

        return root
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Unable to get current location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun setupSearchView() {
        searchEditText = binding.searchEditText
        searchResultsRecyclerView = binding.searchResultsRecyclerView
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchResultsAdapter = SearchResultsAdapter { placeId ->
            fetchPlaceDetails(placeId)
            binding.searchResultsRecyclerView.visibility = View.GONE
        }
        searchResultsRecyclerView.adapter = searchResultsAdapter

        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.search_icon, 0, 0, 0
        )

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchPlaces(query)
                    binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.search_icon, 0, R.drawable.close_icon, 0
                    )
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.search_icon, 0, 0, 0
                    )
                    binding.searchResultsRecyclerView.visibility = View.GONE
                }
            }
        })

        binding.searchEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawables = binding.searchEditText.compoundDrawables
                val clearButtonDrawable = drawables[2]
                if (clearButtonDrawable != null) {
                    val clearButtonStart: Float =
                        (binding.searchEditText.right - clearButtonDrawable.bounds.width()).toFloat()
                    if (event.rawX >= clearButtonStart) {
                        binding.searchEditText.setText("")
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.search_icon, 0, 0, 0
                        )
                        utils.hideKeyboard(requireContext(), binding.searchEditText)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun searchPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            searchResultsAdapter.submitList(response.autocompletePredictions)
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            place.latLng?.let { latLng ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
            utils.hideKeyboard(requireContext(), binding.searchEditText)
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setMarkerWithRadius(latLng: LatLng, radius: Int) {
        googleMap.clear()
        googleMap.addMarker(
            com.google.android.gms.maps.model.MarkerOptions().position(latLng).title("Destination")
        )
        googleMap.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(latLng)
                .radius(radius.toDouble())
                .strokeColor(R.color.teal_200)
                .fillColor(0x220000FF)
                .strokeWidth(5f)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.setOnMapLongClickListener { latLng ->
            val inputField = EditText(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter radius in meters")
                .setView(inputField)
                .setPositiveButton("OK") { _, _ ->
                    val radius = inputField.text.toString().toIntOrNull() ?: 0
                    setMarkerWithRadius(latLng, radius)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap.isMyLocationEnabled = true
                    getCurrentLocation()
                }
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.googleMapsView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.googleMapsView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.googleMapsView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.googleMapsView.onLowMemory()
    }
}