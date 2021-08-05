package com.udacity.project4.locationreminders.savereminder


import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_LOCATION
import com.udacity.project4.REQ_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.base.BasePermissionFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.addGeofenceForReminder
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

private const val TAG: String = "cy.frag.save_reminder"
class SaveReminderFragment : BasePermissionFragment() {
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
                doAddGeofenceForReminder(reminder)

            } catch (e:Exception ){
                e.printStackTrace()
            }
        }
    }

    private fun doAddGeofenceForReminder(reminder: ReminderDataItem){
        Log.i(TAG, "doAddGeofenceForReminder")
        /* save reminder flow should be..
            1. check location permissions
            2. check device location is turned on
            3. attempt to add geofence
            4. save reminder
         */

        // check permission
        if (hasForegroundBackgroundLocationPermission()){
            // check location on
            checkDeviceLocationSettingsAndAddGeofence(reminder)
        } else {
            requestForegroundBackgroundLocationPermissions()
        }
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
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsGranted $requestCode")
        when (requestCode){
            REQ_PERMISSION_FOREGROUND_LOCATION, REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION -> {
                Log.i(TAG, "location permissions granted!")
                // check device location?
                checkDeviceLocationSettingsAndAddGeofence(null)
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsDenied $requestCode")
//        requireContext().showPermissionDeniedDialog(getString(R.string.permission_denied_explanation))
//        val view = requireActivity().findViewById<View>(android.R.id.content)

        Snackbar.make(requireView(), R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.settings) {
                requireContext().toSettingPage()
            }.show()
    }

    /*
     *  Uses the Location Client to check the current state of system's location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun Context.checkDeviceLocation(): Task<LocationSettingsResponse> {
        // prepare for setting request
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        return settingsClient.checkLocationSettings(settingsRequest)
    }
    private fun checkDeviceLocationSettingsAndAddGeofence(reminder:ReminderDataItem?, resolve:Boolean = true) {
        Log.i(TAG, "checkDeviceLocationSettingsAndAddGeofence")
        val locationSettingsResponseTask = requireContext().checkDeviceLocation().apply{
            addOnFailureListener { exception ->
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
            addOnCompleteListener {
                if ( it.isSuccessful ) {
                    Log.i(TAG, "device's location is on")
                    reminder?.let {
                        geofencingClient.addGeofenceForReminder(reminder)
                        _viewModel.saveReminder(reminder)
                    }
                }
            }
        }
    }

}
