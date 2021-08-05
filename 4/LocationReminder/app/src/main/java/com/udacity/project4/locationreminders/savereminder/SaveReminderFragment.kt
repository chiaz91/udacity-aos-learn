package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_LOCATION
import com.udacity.project4.REQ_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.addGeofenceForReminder
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.koin.android.ext.android.inject

private const val TAG: String = "cy.frag.save_reminder"
class SaveReminderFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val geofencingClient by lazy {
        LocationServices.getGeofencingClient(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        binding.viewModel = _viewModel

        setDisplayHomeAsUpEnabled(true)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminder = ReminderDataItem(title, description, location, latitude, longitude)
            // early ending if user inputs is not passing validation
            if (!_viewModel.validateEnteredData(reminder)) {
                Log.i(TAG, "some user input did not pass validate")
                return@setOnClickListener
            }

            // use the user entered reminder details to:
            //  1) add a geofencing request
            //  2) save the reminder to the local db
            try{
                geofencingClient.addGeofenceForReminder(reminder)
                _viewModel.validateAndSaveReminder(reminder)
            } catch (e:Exception ){
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // checking on location permission is granted
        requestLocationPermissions()
        checkDeviceLocationSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult $requestCode, $resultCode, $data")
        // handle location setting result if needed
    }

    // location permission related
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // let EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            // show dialog to direct user to system setting for app
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermissions()
        }
    }

    /*
     *  Uses the Location Client to check the current state of system's location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(settingsRequest)
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    // for Activity --> exception.startResolutionForResult(requireActivity(), REQ_TURN_DEVICE_LOCATION_ON)
                    startIntentSenderForResult(exception.resolution.intentSender, REQ_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null);
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                _viewModel.showSnackBar.value = getString(R.string.location_required_error)
                // TODO: add action to check location setting again
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                _viewModel.showToast.value = "location turned on"
            }
        }
    }

}
