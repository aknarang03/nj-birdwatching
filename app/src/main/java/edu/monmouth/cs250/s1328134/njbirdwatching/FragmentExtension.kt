package edu.monmouth.cs250.s1328134.njbirdwatching

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setActivityTitle(title: String) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = title
}

fun Fragment.setBarColor(color: ColorDrawable) {
    (activity as AppCompatActivity?)?.supportActionBar?.setBackgroundDrawable(color)
}