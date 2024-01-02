package edu.monmouth.cs250.s1328134.njbirdwatching

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import edu.monmouth.cs250.s1328134.njbirdwatching.databinding.ActivityMainBinding
import androidx.navigation.ui.NavigationUiSaveStateControl

class MainActivity : AppCompatActivity(), FetchCompletionListener {

    // for view
    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController

    // for location access
    private val LOCATIONREQUESTCODE = 101
    var locationAccess = false

    // for shared preferences (used throughout so must be accessible)
    companion object {
        var prefs: Prefs? = null
    }

    @OptIn(NavigationUiSaveStateControl::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up shared prefs
        prefs = Prefs(applicationContext)

        // get data
        RecentSightingsService.fetchSightings(this) // Log: onResponse
        NJBirdModel.getBirds("birds.json",this) // Log: getBirds

        // set up navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment!!.findNavController()
        setupWithNavController(binding.bottomNavigationView, navController, false)

        // request permission for location access
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATIONREQUESTCODE)

        Log.i("array test",BirdFamilyNames.birdImageNames.toString())
    }

    override fun onResume() {
        super.onResume()
        if (BirdDetailActivity.toMap) {
            navController.navigate(R.id.recentSightingsMap)
        }
    }

    // for fetch completion listener
    override fun fetchCompleted(bird: String, completionResponse: Boolean, dataResponse: Boolean) {
        runOnUiThread {
            if (completionResponse && dataResponse) {
                // response and data fetched
                Log.i("main","fetch completed")
            } else {
                // response or data fetch failed
                Log.i("main","fetch failed")
            }
        }
    }

    // request permission for location access
    private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permissionType), requestCode )
    }

    // permission response
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATIONREQUESTCODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Unable to show location - permission required",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    locationAccess = true
                }
            }
        }
    }

}