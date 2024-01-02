package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object SearchService {

    private val client = OkHttpClient()
    private const val apiKey : String = "7d73417be725c00dc103deb50913367e257d617594ea9c1b0416de69a3974991"

    var images = ArrayList<String>()
    var firstImage : String = ""

    fun fetchImages(birdName: String, context: Context) {

        images.clear()

        Log.i("fetchImages","in fetch images")

        val url = "https://serpapi.com/search.json?engine=google_images"
        val query = "&q=" + birdName + "%20bird" // if something messes up get rid of the %20 bird
        val apiKeyString = "&api_key=" + apiKey
        val request = Request.Builder().url(url+query+apiKeyString).tag("get images").build()

        Log.i("fetchImages",url+query+apiKeyString)

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

                val json = JSONObject(jsonString)
                val jsonArray = json.getJSONArray("images_results")

                (0 until jsonArray.length()).mapTo(images) {
                    jsonArray.getJSONObject(it).getString("thumbnail")
                }
                firstImage = images[0]
                Log.i("thumbnail",images[0])

                val cxt = context as FetchCompletionListener
                Log.i("onResponse", images.toString())

                if (response.isSuccessful) {
                    // data is valid
                    Log.i("valid data","data")
                    cxt.fetchCompleted(requestTag,true, true)
                } else {
                    // data is not there
                    Log.i("no data","no data")
                    cxt.fetchCompleted(requestTag, true, false)
                }

            }

        }
        )

    }

}