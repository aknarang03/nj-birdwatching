package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException

data class Bird (val sciName: String, val comName: String, val familyComName: String, val familySciName: String, val taxonOrder: Int) {var icon : Int = 0; var seen : Boolean = false}

object NJBirdModel {

    var njBirds = ArrayList<Bird>()
    var saveBirds = ArrayList<Bird>()

    // get Birds from JSON file
    fun getBirds (filename: String, context: Context) {

        try {

            val jsonString = loadJson(filename,context)

            if (jsonString != null) {

                // parse JSON String into JSON Array
                val jsonArray = JSONArray(jsonString)

                // now map to njBirds array
                (0 until jsonArray.length()).mapTo(njBirds) {

                    Bird (jsonArray.getJSONObject(it).getString("sciName"),
                        jsonArray.getJSONObject(it).getString("comName"),
                        jsonArray.getJSONObject(it).getString("familyComName"),
                        jsonArray.getJSONObject(it).getString("familySciName"),
                        jsonArray.getJSONObject(it).getInt("taxonOrder")
                    ) }

                saveBirds = njBirds

            } else {
                println("Invalid JSON String")
            }

        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        // get other data from elsewhere
        getIcons(context)
        getSeenStatus()

        // confirm you have data
        Log.i("getBirds",njBirds.toString())

    }

    // load JSON from file
    private fun loadJson (filename: String,context: Context) : String? {
        val jsonString: String?
        try {
            val inputStream = context.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            // read input
            inputStream.read(buffer)
            inputStream.close()
            // convert input to string
            val charset = Charsets.UTF_8
            jsonString = buffer.toString(charset)
        } catch (ex: java.io.IOException) {
            ex.printStackTrace()
            return null
        }
        return jsonString
    }

    // default shows up for family names I'm not including
    private fun getIcons(context: Context) {

        val names = BirdFamilyNames.birdImageNames

        for (bird in njBirds) {

            var birdImg = R.drawable.default_bird
            val birdIndex = names.indexOfFirst {
                bird.familyComName.replace(" ","_").replace(",","").replace("-","").lowercase() == it
            }

            if (birdIndex != -1) {
                birdImg = context.resources.getIdentifier(
                    names[birdIndex],
                    "drawable",
                    context.packageName
                )
            }

            bird.icon = birdImg
        }

    }

    private fun getSeenStatus() {
        for (bird in njBirds) {
            val seen = MainActivity.prefs!!.getPref(bird)
            bird.seen = seen
        }
    }

    fun getBirdObject (taxonOrder: Int) : Bird {
        val birdToReturn = njBirds.filter {it.taxonOrder == taxonOrder}
        val index = njBirds.indexOf(birdToReturn[0])
        return njBirds[index]
    }

}