package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,  EasyPermissions.PermissionCallbacks {
    private val TAG: String = "frag.select_location"
    private val DEFAULT_ZOOM = 15f;
    private val REQ_PERMISSION_FOREGROUND_LOCATION = 1001
    private val REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION = 1002
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var selectedPOI: PointOfInterest? = null

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
        requestLocationPermissions()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // add the map setup implementation
        // add style to the map
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        setMapStyle(map)
        setPoiClick(map)

        // zoom to the user location after taking his permission
        if (hasLocationPermissions()){
            locateUser()
        } else {
            requestLocationPermissions()
        }
        _viewModel.showToast.value = getString(R.string.select_poi)
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
            map.clear()
            addAndShowMarker(map, poi.name, poi.latLng)
            selectedPOI = poi
        }
    }

//    private fun setMapLongClick(map:GoogleMap) {
//        map.setOnMapLongClickListener { latLng ->
//            map.clear()
//            addAndShowMarker(map, getString(R.string.dropped_pin), latLng)
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun locateUser(){
        // enable locate user on ui
        map.isMyLocationEnabled = true

        // retrieve and move camera to device's location
        getDeviceLocation { latlng ->
            val camUpdate = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM);
            map.animateCamera(camUpdate)
            map.addMarker(MarkerOptions().position(latlng).title("Current Location"))
        }
    }

    private fun getDeviceLocation(callback : (LatLng) -> Unit) {
        try {
            val locationTask = fusedLocationProviderClient.lastLocation
            locationTask.addOnCompleteListener(requireActivity()) { task ->
                // default location to Googleplex
                var latLng = LatLng(37.42206, -122.08409)
                if (task.isSuccessful ) {
                    // return the current location of the device.
                    latLng = LatLng(task.result!!.latitude, task.result!!.longitude)
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
        selectedPOI?.let {
            _viewModel.reminderSelectedLocationStr.value = it.name
            _viewModel.selectedPOI.value = it
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.longitude.value = it.latLng.longitude
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }



    // Permission related
    @TargetApi(29)
    private fun hasLocationPermissions(): Boolean{
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater){
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        return EasyPermissions.hasPermissions(requireContext(), *permissions)
    }

    @TargetApi(29)
    private fun requestLocationPermissions(){
        if (hasLocationPermissions())
            return
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            runningQOrLater -> {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION
            }
            else -> REQ_PERMISSION_FOREGROUND_LOCATION
        }
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.permission_denied_explanation),
            requestCode,
            *permissions
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // let EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        locateUser()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermissions()
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
