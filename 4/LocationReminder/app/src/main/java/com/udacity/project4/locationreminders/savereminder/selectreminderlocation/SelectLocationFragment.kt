package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_LOCATION
import com.udacity.project4.base.BasePermissionFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject
import java.util.*
import android.content.Intent

import android.location.LocationManager

import android.content.IntentFilter
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.locationreminders.savereminder.LocationStateChangedReceiver


class SelectLocationFragment : BasePermissionFragment(), OnMapReadyCallback {
    private val TAG: String = "cy.frag.select_location"
    private val DEFAULT_ZOOM = 15f;
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var latLngGoogleplex = LatLng(37.42206, -122.08409)
    private val locationStateReceiver = LocationStateChangedReceiver { isEnabled ->
        Log.i(TAG, "location state is $isEnabled")
        // receive location changes, should update the ui?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        binding.saveButton.setOnClickListener {
            // call this function after the user confirms on the selected location
            onLocationSelected()
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // init google map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_frag) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        if (hasForegroundLocationPermission()){
            locateUser()
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        requireActivity().registerReceiver(locationStateReceiver, filter)
    }

    override fun onPause() {
        requireActivity().unregisterReceiver(locationStateReceiver)
        super.onPause()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "Google map is ready!")
        // add the map setup implementation
        // add style to the map
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
        setMarkerClick(map)

        // zoom to the user location after taking his permission
        if (hasForegroundLocationPermission()){
            locateUser()
        } else {
            requestForegroundLocationPermissions()
        }
//        _viewModel.showToast.value = getString(R.string.select_poi)
        _viewModel.selectedPOI.observe(viewLifecycleOwner, androidx.lifecycle.Observer { poi ->
            try{
                val camUpdate = CameraUpdateFactory.newLatLngZoom(poi.latLng, DEFAULT_ZOOM);
                map.animateCamera(camUpdate)
            } catch (e:Exception){
                e.printStackTrace()
            }
        })
    }

    private fun createPoi(latLng: LatLng): PointOfInterest{
        return PointOfInterest(latLng, "id", latLng.format(3))
    }

    // generate the style with wizard @ https://mapstyle.withgoogle.com/
    // applied only with normal mode
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )

            if (!success) {
                Log.e(TAG, "Map style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Map style not found. Error: ", e)
        }
    }


    // put a marker to location that the user selected
    private fun addAndShowMarker(map: GoogleMap, name: String, position: LatLng): Marker{
        // A Snippet is Additional text that's displayed below the title.
        val snippet = String.format(
            Locale.getDefault(),
            getString(R.string.lat_long_snippet),
            position.latitude,
            position.longitude
        )
        val marker = map.addMarker(
            MarkerOptions()
                .position(position)
                .title(name)
                .snippet(snippet)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        marker.showInfoWindow()
        return marker
    }

    // point of interest
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            Log.i(TAG, "onPoiClicked: ${poi.name}, ${poi.latLng}")
            map.clear()
            addAndShowMarker(map, poi.name, poi.latLng)
            _viewModel.selectedPOI.value = poi
        }
    }

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            Log.i(TAG, "OnMapLongClicked $latLng")
            map.clear()
            addAndShowMarker(map, getString(R.string.dropped_pin), latLng)

            _viewModel.selectedPOI.value = createPoi(latLng)
        }
    }

    private fun setMarkerClick(map:GoogleMap){
        map.setOnMarkerClickListener { marker ->
            Log.i(TAG, "onMarkerClicked ${marker.title}, ${marker.snippet}, ${marker.position}")
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun locateUser(){
        try {
            // enable locate user on ui
            map.isMyLocationEnabled = true

            // retrieve and move camera to device's location
            getDeviceLocation { latLng ->
                Log.i(TAG, "deviceLocation: ${latLng.format()}")
                map.clear()
                map.addMarker(MarkerOptions().position(latLng).title("Current Location"))
                _viewModel.selectedPOI.value = createPoi(latLng)
            }
        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun getDeviceLocation(callback : (LatLng) -> Unit) {
        try {
            val locationTask = fusedLocationProviderClient.lastLocation
            locationTask.addOnCompleteListener(requireActivity()) { task ->
                // default location to Googleplex
                var latLng = latLngGoogleplex
                if (task.isSuccessful ) {
                    // has permission and location is turned on
                    try{
                        // return the current location of the device.
                        latLng = LatLng(task.result!!.latitude, task.result!!.longitude)
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
                callback(latLng)
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun onLocationSelected() {
        // When the user confirms on the selected location,
        // send back the selected location details to the view model
        // and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.selectedPOI.value?.let {
            with(_viewModel){
                reminderSelectedLocationStr.value = it.name
                latitude.value = it.latLng.latitude
                longitude.value = it.latLng.longitude
                navigationCommand.value = NavigationCommand.Back
            }

        } ?: run{
            // for testing, uses default location
            with(_viewModel){
                selectedPOI.value = createPoi(latLngGoogleplex)
                reminderSelectedLocationStr.value = "Googleplex"
                latitude.value = latLngGoogleplex.latitude
                longitude.value = latLngGoogleplex.longitude
                navigationCommand.value = NavigationCommand.Back
            }

        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        locateUser()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        when (requestCode){
            REQ_PERMISSION_FOREGROUND_LOCATION -> {
                if (shouldShowRequestPermissionsRationale(perms)){
//                    requireContext().showPermissionDeniedDialog(getString(R.string.permission_denied_explanation))
                    Snackbar.make(requireView(), R.string.permission_denied_explanation, Snackbar.LENGTH_LONG)
                        .setAction(R.string.settings) {
                            requireContext().toSettingPage()
                        }.show()
                }
            }

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

}
