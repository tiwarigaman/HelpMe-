package com.mobile.emergencysos

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity3 : AppCompatActivity(),OnMapReadyCallback {
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("live_sharing")
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val interval: Long = 300000 // 5 minutes in milliseconds

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
//        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE


        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment2) as SupportMapFragment
        mapFragment.getMapAsync(this@MainActivity3)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity3)

        // Create a LocationRequest object
        locationRequest = LocationRequest.create()
            .setInterval(interval)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create a LocationCallback object
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    updateLocationOnMap(LatLng(location.latitude, location.longitude))
                }
            }
        }

        // Request location updates
        requestLocationUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getCurrentLocation()
        try {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        updateLocationOnMap(LatLng(location.latitude, location.longitude))
                    }
                }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateLocationOnMap(location: LatLng) {
        googleMap.clear() // Clear existing markers
        googleMap.addMarker(MarkerOptions().position(location).title("Your Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}