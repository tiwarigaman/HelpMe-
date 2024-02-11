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
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private val destination = LatLng(37.7749, -122.4194)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        // Get and display the current location
        getCurrentLocation()
    }
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        Toast.makeText(this, "Your Location: $currentLatLng", Toast.LENGTH_SHORT).show()

                        // Add a marker at the current location
                        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))

                        // Now, you can call the getRoute function with the current location
                        getRoute(currentLatLng)
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




    @OptIn(DelicateCoroutinesApi::class)
    private fun getRoute(currentLatLng: LatLng) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val apiKey = "AIzaSyAcZWc7XPSpbbJdwyFZ4QQ12pj40M55bak" // Replace with your API key
                val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()

                // Ensure 'origin' and 'destination' are String representations of LatLng
                val origin = "${currentLatLng.latitude},${currentLatLng.longitude}"
                val destination = "37.7749,-122.4194" // Replace with your destination coordinates

                Toast.makeText(this@MainActivity2, "Your Location: $currentLatLng", Toast.LENGTH_SHORT).show()

                val route = withContext(Dispatchers.IO) {
                    DirectionsApi.newRequest(geoApiContext)
                        .origin(origin)
                        .destination(destination)
                        .await()
                }

                // Draw the route on the map
                drawRoute(route)
            } catch (e: Exception) {
                // Handle exceptions, such as no route found
                e.printStackTrace()
            }
        }
    }

    private fun drawRoute(route: com.google.maps.model.DirectionsResult) {
        val decodedPath = PolyUtil.decode(route.routes[0].overviewPolyline.encodedPath)

        googleMap.addPolyline(
            PolylineOptions()
                .addAll(decodedPath)
                .width(12f)
                .color(resources.getColor(R.color.colorGreen))
        )

        // Optionally, zoom and center the map to fit the route
        val boundsBuilder = LatLngBounds.builder()
        decodedPath.forEach { boundsBuilder.include(it) }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50))
    }
}