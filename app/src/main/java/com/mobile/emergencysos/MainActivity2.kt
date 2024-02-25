package com.mobile.emergencysos

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity2 : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var otherUser : String
    private lateinit var requestKey : String
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
    private val placesList = mutableListOf<Pair<LatLng, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        otherUser = intent.getStringExtra("uid").toString()
        requestKey = intent.getStringExtra("requestKey").toString()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        val fabCurrentLocation: FloatingActionButton = findViewById(R.id.fabCurrentLocation)

        fabCurrentLocation.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            if(otherUser!="null" && requestKey!="null"){
                intent.putExtra("requestKey", requestKey)
                intent.putExtra("uid", otherUser)
            }
            startActivity(intent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Add a marker at a specific location and move the camera
        val location = LatLng(37.7749, -122.4194) // San Francisco, CA
        googleMap.addMarker(MarkerOptions().position(location).title("Anonymous"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,17.0f))
        // Get and display the current location
        getCurrentLocation()
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        val updateMap = HashMap<String, Any>()
        updateMap["lat"] = latitude.toString()
        updateMap["lng"] = longitude.toString()
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        db.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).updateChildren(updateMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //successfully
            } else {
                // Handle the error
                task.exception?.printStackTrace()
            }
        }

        if(otherUser!="null"){
            db.child(otherUser).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val lat = snapshot.child("lat").value.toString().toDoubleOrNull()
                    val lng = snapshot.child("lng").value.toString().toDoubleOrNull()

                    if (lat != null && lng != null) {
                        val locationLatLng = LatLng(lat, lng)
                        val geocoder = Geocoder(this@MainActivity2, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)

                        val placeName = if (addresses!!.isNotEmpty()) {
                            addresses[0].getAddressLine(0)
                        } else {
                            "Anonymous"
                        }
                        placesList.add(Pair(locationLatLng, placeName))
                        updateMarkersOnMap()

                        // Add a marker at the retrieved location
//                        for ((location, placeName) in placesList) {
//                            googleMap.addMarker(MarkerOptions().position(location).title(placeName))
//                        }
//                        googleMap.addMarker(MarkerOptions().position(locationLatLng).title("Anonymous"))
//                        // Move the camera to the retrieved location with a zoom level of 15.0
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 17.0f))
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun updateMarkersOnMap() {
        // Clear existing markers
        googleMap.clear()

        // Add markers for each place in the list
        for ((location, placeName) in placesList) {
            googleMap.addMarker(MarkerOptions().position(location).title(placeName))
        }

        // Move the camera to the last retrieved location with a zoom level of 17.0
        if (placesList.isNotEmpty()) {
            val lastLocation = placesList.last().first
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 17.0f))
        }
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17.0f))

                        val timer = Timer()

                        val timerTask = object : TimerTask() {
                            override fun run() {
                                // Call your function here
                                updateLocation(currentLatLng.latitude,currentLatLng.longitude)
                            }
                        }
                        timer.schedule(timerTask, 0, 3000)
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Show confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                // Handle exit action or finish the activity
                finish()
            }
            .setNegativeButton("No") { _: DialogInterface, _: Int ->
                // Do nothing or handle other actions
            }
            .show()
    }
}