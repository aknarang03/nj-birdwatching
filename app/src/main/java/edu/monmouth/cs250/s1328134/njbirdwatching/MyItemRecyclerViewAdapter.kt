package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

import edu.monmouth.cs250.s1328134.njbirdwatching.databinding.FragmentItemBinding

class MyItemRecyclerViewAdapter(
    private val values: ArrayList<Bird>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>(){

    var holdParent : ViewGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        holdParent = parent // to use parent later
        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val bird = values[position]

        holder.birdIconImageView.setImageResource(bird.icon)
        holder.birdNameTextView.text = bird.comName

        // change card color depending on whether bird is seen
        if (bird.seen) {
            holder.layout.setCardBackgroundColor(Color.parseColor("#b6e5b0"))
        } else {
            holder.layout.setCardBackgroundColor(Color.parseColor("#e7e7e2"))
        }

        // start detail activity on click of a card
        holder.layout.setOnClickListener{
            Log.i("setOnClickListener","set on click listener")
            val intent = Intent (holdParent!!.context, BirdDetailActivity::class.java)
            val id = bird.taxonOrder
            intent.putExtra("id",id)
            holdParent!!.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val birdIconImageView: ImageView = binding.birdIcon
        val birdNameTextView: TextView = binding.birdNameText
        val layout = itemView.findViewById<CardView>(R.id.cardView)
        override fun toString(): String {
            return super.toString() + " '" + birdNameTextView.text
        }
    }

}