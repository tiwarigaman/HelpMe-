package com.mobile.emergencysos

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class MainActivity2 : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private val destination = LatLng(37.7749, -122.4194)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var nameUser : String? = "aman"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val fabCurrentLocation: FloatingActionButton = findViewById(R.id.fabCurrentLocation)



        fabCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Add a marker at a specific location and move the camera
        val location = LatLng(37.7749, -122.4194) // San Francisco, CA
        googleMap.addMarker(MarkerOptions().position(location).title("Marker in San Francisco"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,20.0f))
        // Get and display the current location
        getCurrentLocation()
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        val updateMap = HashMap<String, Any>()
        updateMap["lat"] = latitude.toString()
        updateMap["lng"] = longitude.toString()
        val db : DatabaseReference = FirebaseDatabase.getInstance().getReference("user")
        db.child(nameUser!!).updateChildren(updateMap).addOnCompleteListener {task->
            if (task.isSuccessful) {
                // Update successful
//                Toast.makeText(this@MainActivity2,"$latitude $longitude",Toast.LENGTH_SHORT).show()
            } else {
                // Handle the error
                task.exception?.printStackTrace()
            }
        }

    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        Toast.makeText(this, "Your Location: $currentLatLng", Toast.LENGTH_SHORT).show()

                        var value : String

                        val timer = Timer()

                        val timerTask = object : TimerTask() {
                            override fun run() {
                                // Call your function here
                                updateLocation(currentLatLng.latitude,currentLatLng.longitude)
                            }
                        }
                        timer.schedule(timerTask, 0, 3000)

                        val db : DatabaseReference = FirebaseDatabase.getInstance().getReference("user")
                        db.child(nameUser!!).addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                value = snapshot.child("refer").value.toString()
                                db.child(value).addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val lat = snapshot.child("lat").value.toString().toDoubleOrNull()
                                        val lng = snapshot.child("lng").value.toString().toDoubleOrNull()

                                        if (lat != null && lng != null) {
                                            val locationLatLng = LatLng(lat, lng)
                                            // Add a marker at the retrieved location
                                            googleMap.addMarker(MarkerOptions().position(locationLatLng).title("Your Location"))
                                            Toast.makeText(this@MainActivity2,"$lat $lng",Toast.LENGTH_SHORT).show()
                                            // Move the camera to the retrieved location with a zoom level of 15.0
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 17.0f))
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

//                        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,10.0f))



                        // Add a marker at the current location


                    // Now, you can call the getRoute function with the current location
//                        getRoute(currentLatLng)
                    } else {
                        // Handle the case where the last known location is not available
                        // You might want to request location updates instead
//                        requestLocationUpdates()
                    }
                }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }



//
//    @OptIn(DelicateCoroutinesApi::class)
//    private fun getRoute(currentLatLng: LatLng) {
//        GlobalScope.launch(Dispatchers.Main) {
//            try {
//                val apiKey = "AIzaSyAcZWc7XPSpbbJdwyFZ4QQ12pj40M55bak" // Replace with your API key
//                val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()
//
//                // Ensure 'origin' and 'destination' are String representations of LatLng
//                val origin = "${currentLatLng.latitude},${currentLatLng.longitude}"
//                val destination = "37.7749,-122.4194" // Replace with your destination coordinates
//
//                Toast.makeText(this@MainActivity2, "Your Location: $currentLatLng", Toast.LENGTH_SHORT).show()
//
//                val route = withContext(Dispatchers.IO) {
//                    DirectionsApi.newRequest(geoApiContext)
//                        .origin(origin)
//                        .destination(destination)
//                        .await()
//                }
//
//                // Draw the route on the map
//                drawRoute(route)
//            } catch (e: Exception) {
//                // Handle exceptions, such as no route found
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun drawRoute(route: com.google.maps.model.DirectionsResult) {
//        val decodedPath = PolyUtil.decode(route.routes[0].overviewPolyline.encodedPath)
//
//        googleMap.addPolyline(
//            PolylineOptions()
//                .addAll(decodedPath)
//                .width(12f)
//                .color(resources.getColor(R.color.colorGreen))
//        )
//
//        // Optionally, zoom and center the map to fit the route
//        val boundsBuilder = LatLngBounds.builder()
//        decodedPath.forEach { boundsBuilder.include(it) }
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50))
//    }
}