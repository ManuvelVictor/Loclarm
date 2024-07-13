package com.victor.loclarm.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.victor.loclarm.R

class SearchResultsAdapter(
    private val onPlaceSelected: (String) -> Unit
) : ListAdapter<AutocompletePrediction, SearchResultsAdapter.SearchResultViewHolder>(PlaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val prediction = getItem(position)
        holder.bind(prediction)
        holder.itemView.setOnClickListener {
            onPlaceSelected(prediction.placeId)
        }
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeNameTextView: TextView = itemView.findViewById(R.id.place_name_text_view)

        fun bind(prediction: AutocompletePrediction) {
            placeNameTextView.text = prediction.getPrimaryText(null).toString()
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<AutocompletePrediction>() {
        override fun areItemsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
            return oldItem == newItem
        }
    }
}