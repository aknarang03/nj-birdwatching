package edu.monmouth.cs250.s1328134.njbirdwatching

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class RecentSightingsMap : Fragment(), LocationListener {

    // for location access
    lateinit var locationManager: LocationManager
    lateinit var currentUserLocation: Location

    private var zoomLevel = 8.0f

    private val callback = OnMapReadyCallback { googleMap ->
        editMapSettings(googleMap)
        showMyData(googleMap)
        if ((activity as MainActivity).locationAccess) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent_sightings_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMapFragment() // shows markers, zooms to NJ (always, despite location access)
        // check if user gave permission to access location. if so, shows the location data
        val permission = ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            (activity as MainActivity).locationAccess = true
            showLocationData()
        }
        setActivityTitle("NJ Birds Map")
        setBarColor(ColorDrawable(Color.parseColor("#b6e5b0")))
    }

    private fun showMyData(mMap : GoogleMap) {
        // center map on New Jersey upon opening
        val njCoords = LatLng(40.0583, -74.4057)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(njCoords, zoomLevel))
        showBirds(mMap)
    }

    private fun showBirds(mMap : GoogleMap) {

        if ((activity as MainActivity).locationAccess) { // location access

            for (bird in RecentSightingsService.recentSightings) {

                val name = bird.comName
                val date = bird.obsDt
                val birdLatLng = LatLng(bird.lat, bird.lng)

                currentUserLocation = getLocation()
                val locationLatitude = currentUserLocation.latitude
                val locationLongitude = currentUserLocation.longitude

                val distance = FloatArray(2)
                Location.distanceBetween(bird.lat,bird.lng,locationLatitude,locationLongitude, distance)
                val distanceInMiles = Math.round((distance[0] / 1609.334) * 100.00) / 100.00

                val snippetString = distanceInMiles.toString() + " mi away | " + "Obs: " + date

                val birdMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(birdLatLng)
                        .title(name)
                        .snippet(snippetString)
                        .icon(BitmapDescriptorFactory.defaultMarker(75f))
                )
                birdMarker?.tag = name

            }

        } else { // no location access

            for (bird in RecentSightingsService.recentSightings) {

                val name = bird.comName
                val date = bird.obsDt
                val birdLatLng = LatLng(bird.lat, bird.lng)

                val birdMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(birdLatLng)
                        .title(name)
                        .snippet("Obs: " + date)
                        .icon(BitmapDescriptorFactory.defaultMarker(75f))
                )
                birdMarker?.tag = name

            }

        }

    }

    private fun showMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    // return user's current location
    private fun getLocation() : Location {
        val providers: List<String> = locationManager.getProviders(true)
        var location: Location? = null
        for (i in providers.size - 1 downTo 0) {
            location= locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }
        return location!!
    }

    private fun showLocationData() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this) // receive change
    }

    private fun editMapSettings(mMap: GoogleMap) {
        val mapSettings = mMap.uiSettings
        mapSettings.isZoomControlsEnabled = true
        mapSettings.isCompassEnabled = true
        mapSettings.isMapToolbarEnabled = true
        mapSettings.isMyLocationButtonEnabled = true
    }

    override fun onLocationChanged(location : Location) {
        Log.i ("Location Data", location.latitude.toString())
        Log.i ("Location Data", location.longitude.toString())
        currentUserLocation = location
    }

}
