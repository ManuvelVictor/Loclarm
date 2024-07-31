package com.victor.loclarm.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.victor.loclarm.db.AlarmDatabase
import com.victor.loclarm.db.AlarmRepository
import com.victor.loclarm.service.LocationService
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

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 200
    }

    private val permissionNew = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            AlarmRepository(AlarmDatabase.getDatabase(requireContext()).alarmDao()),
            this
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                googleMap.isMyLocationEnabled = true
                getCurrentLocation()
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkPermissions(): Boolean {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (permission in permissionNew) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionsNeeded.add(permission)
            }
        }
        return if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionsNeeded.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                googleMap.isMyLocationEnabled = true
                getCurrentLocation()
            }
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

    @SuppressLint("SetTextI18n")
    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS_COMPONENTS
        )
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            place.latLng?.let { latLng ->
                val zoomLevel = getZoomLevel(place)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
            }
            utils.hideKeyboard(requireContext(), binding.searchEditText)
            searchEditText.setText("")
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getZoomLevel(place: Place): Float {
        place.addressComponents?.let { addressComponents ->
            for (component in addressComponents.asList()) {
                val types = component.types
                if (types.contains("country")) {
                    return 4f
                }
                if (types.contains("administrative_area_level_1")) {
                    return 6f
                }
                if (types.contains("administrative_area_level_2")) {
                    return 10f
                }
                if (types.contains("locality")) {
                    return 12f
                }
                if (types.contains("sublocality")) {
                    return 14f
                }
                if (types.contains("neighborhood")) {
                    return 15f
                }
            }
        }
        return 15f
    }

    private fun setMarkerWithRadius(latLng: LatLng, alarmName: String, radius: Int) {
        googleMap.clear()
        googleMap.addMarker(
            com.google.android.gms.maps.model.MarkerOptions().position(latLng).title("Destination")
        )
        googleMap.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(latLng)
                .radius(radius.toDouble())
                .strokeColor(R.color.black)
                .fillColor(0x22FFD300)
                .strokeWidth(5f)
        )

        val serviceIntent = Intent(requireContext(), LocationService::class.java).apply {
            putExtra("destinationLat", latLng.latitude)
            putExtra("destinationLng", latLng.longitude)
            putExtra("radius", radius)
            putExtra("alarmName", alarmName)
        }
        ContextCompat.startForegroundService(requireContext(), serviceIntent)

        viewModel.saveAlarm(latLng, alarmName, radius)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.setOnMapLongClickListener { latLng ->
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_radius_input, null)
            val radiusInputField = dialogView.findViewById<EditText>(R.id.input_radius)
            val nameInputField = dialogView.findViewById<EditText>(R.id.input_alarm_name)

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    val radius = radiusInputField.text.toString().toIntOrNull() ?: 0
                    val alarmName = nameInputField.text.toString()
                    setMarkerWithRadius(latLng, alarmName, radius)
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
            dialog.show()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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