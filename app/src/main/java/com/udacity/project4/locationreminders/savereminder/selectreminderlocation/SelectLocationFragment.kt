package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val PERMISSION_CODE_LOCATION_REQUEST = 1
private const val DEFAULT_ZOOM_LEVEL =15f

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = SelectLocationFragment::class.java.simpleName


    // ViewModel for the SaveReminder fragment, provided by Koin
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding

    //Google map instance
    private lateinit var map: GoogleMap

    //Market to represent the selected location
    private var marker: Marker? = null


    // Lazy initialization of fusedLocationProviderClient for accessing location services
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        // Get reference to the map fragment and set up the map asynchronously
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.maps_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for the button to handle location selection
        binding.selectLocationBtn.setOnClickListener {
            onLocationSelected()
        }
    }

    // Callback method for when the map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        // Initialize map instance
        map = googleMap

        // Set map style, points of interest click, and long click listeners
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)

        // Check if location permissions are granted and request if needed
        if (hasLocationPermission()) {
            getUserLocation()
        } else {
            requestLocationPermission()
        }
    }



    // Called when a location is selected on the map
    private fun onLocationSelected() {
        marker?.let { selectedMarker ->
            // Set the selected location details in the ViewModel
            _viewModel.latitude.value = selectedMarker.position.latitude
            _viewModel.longitude.value = selectedMarker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedMarker.title
            // Navigate back to the previous screen
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Checks if location permissions are granted
    private fun hasLocationPermission() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Requests location permissions from the user if not granted
    private fun requestLocationPermission() {
        // If both fine and coarse location permissions are granted, enable my location layer on the map
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Request location permissions from the user
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_CODE_LOCATION_REQUEST
            )
        }
    }



    // Handles the result of the location permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permission is granted, get the user's location; otherwise, show rationale
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        } else {
            showRationale()
        }
    }

    // Shows rationale for location permissions
    private fun showRationale() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            // Show an alert dialog explaining why the permission is needed
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_permission)
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _, ->
                    requestLocationPermission()
                }
                .create()
                .show()
        } else {
            requestLocationPermission()
        }
    }

    // Sets up a point of interest click listener on the map
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            // Clear previous markers and add a new marker at the selected point of interest
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            // Show the info window for the new marker
            marker?.showInfoWindow()
            // Animate the camera to the new point of interest
            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    // Sets up a long click listener on the map
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // Clear previous markers and add a new marker at the long-pressed location
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            // Show the info window for the new marker
            marker?.showInfoWindow()
            // Animate the camera to the new marker
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    // Gets the user's current location and centers the map on it
    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        map.isMyLocationEnabled = true
        Log.d(TAG, "Getting last known location")
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // If location is found, move the camera and add a marker at the user's location
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM_LEVEL))
                    marker = map.addMarker(
                        MarkerOptions().position(userLocation)
                            .title(getString(R.string.my_location))
                    )
                    marker?.showInfoWindow()
                }
            }
    }

    // Applies the map style using a JSON object defined in a raw resource file
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Failed to apply map style")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Map style resource not found", e)
        }
    }
}