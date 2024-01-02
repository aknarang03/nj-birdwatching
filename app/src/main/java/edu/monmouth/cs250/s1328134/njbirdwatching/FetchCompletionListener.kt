package edu.monmouth.cs250.s1328134.njbirdwatching

interface FetchCompletionListener {
    fun fetchCompleted (getBirds: String, completionResponse: Boolean, dataResponse: Boolean)
}