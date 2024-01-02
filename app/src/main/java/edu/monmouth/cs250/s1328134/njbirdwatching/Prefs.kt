package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context){

    private val preferencesFile = "edu.monmouth.cs250.s1328134.njbirdwatching.sightings_file"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferencesFile,Context.MODE_PRIVATE)
    private val editor : SharedPreferences.Editor = sharedPreferences.edit()

    fun putPref (bird: Bird) {
        editor.putBoolean(bird.comName,bird.seen)
        editor.apply()
        editor.commit()
    }

    fun getPref(bird: Bird): Boolean {
        return sharedPreferences.getBoolean(bird.comName, false)
    }

}