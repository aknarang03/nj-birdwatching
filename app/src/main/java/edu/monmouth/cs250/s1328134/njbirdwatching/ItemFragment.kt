package edu.monmouth.cs250.s1328134.njbirdwatching

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.google.android.flexbox.*

class ItemFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val spinnerValues = BirdFamilyNames.birdFamilyNames
    lateinit var selected : String
    lateinit var recycler: RecyclerView
    lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        setActivityTitle("NJ Birds")
        setBarColor(ColorDrawable(Color.parseColor("#b6e5b0")))
        recycler = view!!.findViewById<RecyclerView>(R.id.list)
        spinner = view!!.findViewById<Spinner>(R.id.familyNameDropdown)

        // set up recycler view
        val recycleLayoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        if (recycler is RecyclerView) {
            with(recycler) {
                layoutManager = recycleLayoutManager
                adapter = MyItemRecyclerViewAdapter(NJBirdModel.njBirds)
            }
        }

        // set up spinner
        val arrayAdapter = ArrayAdapter<String>(view.context, androidx.transition.R.layout.support_simple_spinner_dropdown_item,spinnerValues)
        if (spinner is Spinner) {
            with(spinner) {
                adapter = arrayAdapter
                onItemSelectedListener = this@ItemFragment
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (BirdDetailActivity.toMap) {
            RecentSightingsService.resetBirds()
            BirdDetailActivity.toMap = false
        }
        recycler.adapter!!.notifyDataSetChanged() // to immediately show changes in card color
    }

    // spinner functionality
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val text = parent?.getChildAt(0) as TextView
        text.setTextColor(Color.BLACK)

        selected = spinnerValues[position]
        Log.i("onItemSelected","selected " + position + " " + selected)

        var filteredBirds = NJBirdModel.njBirds
        if (selected!="All") {
            filteredBirds =
                NJBirdModel.njBirds.filter { it.familyComName == selected } as ArrayList<Bird>
        }
        Log.i("onItemSelected",filteredBirds.toString())

        requireActivity().runOnUiThread {
            recycler.adapter = MyItemRecyclerViewAdapter(filteredBirds)
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.i("onNothingSelected","nothing selected")
    }

}