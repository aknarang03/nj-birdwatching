package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

data class RecentBird(val comName : String, val obsDt : String, val lat : Double, val lng : Double)

object RecentSightingsService {

    private val client = OkHttpClient() // using OkHttp to get sightings data from API
    private const val apiKey : String = "olk577l6inii" // eBird

    var recentSightings = ArrayList<RecentBird>()
    var allSightings = ArrayList<RecentBird>() // to save sightings to restore after filtering

    fun fetchSightings (context: Context) {

        // build request
        val birdsUrl = "https://api.ebird.org/v2/data/obs/US-NJ/recent"
        val request = Request.Builder().url(birdsUrl).header("X-eBirdApiToken",apiKey).tag("get recent sightings").build()

        client.newCall(request).enqueue(object: Callback {

            override fun onFailure(call: Call, e: IOException) {
                val requestTag = call.request().tag() as String
                val cxt = context as FetchCompletionListener
                cxt.fetchCompleted(requestTag,false, false)
                Log.i("onFailure",request.toString() + " " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {

                val requestTag = call.request().tag() as String

                val jsonString = response.body?.string()
                Log.i("onResponse",jsonString!!)

                val jsonArray = JSONArray(jsonString)

                (0 until jsonArray.length()).mapTo(recentSightings) {
                    RecentBird (jsonArray.getJSONObject(it).getString("comName"), //name
                        jsonArray.getJSONObject(it).getString("obsDt"), // date
                        jsonArray.getJSONObject(it).getDouble("lat"),
                        jsonArray.getJSONObject(it).getDouble("lng")
                    ) }
                allSightings = recentSightings

                val cxt = context as FetchCompletionListener
                Log.i("onResponse",recentSightings.toString())

                if (response.isSuccessful) {
                    // data is valid
                    cxt.fetchCompleted(requestTag,true, true)
                } else {
                    // data is not there
                    cxt.fetchCompleted(requestTag, true, false)
                }

            }

        }
        )

    }

    // for map
    fun filterBirds (birdName: String) {
        recentSightings = recentSightings.filter {it.comName == birdName} as ArrayList<RecentBird>
    }

    fun resetBirds () {
        recentSightings = allSightings
    }

}