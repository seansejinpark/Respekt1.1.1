package com.respekt.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.respekt.R
import com.respekt.models.DataItem
import com.respekt.models.PlayListResponse
import java.util.*
import kotlin.collections.ArrayList

class PlayListAdapter(
    private var context: Context,
    private var items: MutableList<DataItem>,
    private val listener: (DataItem) -> Unit
) :
    RecyclerView.Adapter<PlayListAdapter.ViewHolder>(), Filterable {

    var filterList: MutableList<DataItem>? = null

    init {
        filterList = items
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = filterList?.get(position)
        holder.tvTitle?.text = playlist?.musicTitle
        holder.tvDuration?.text = playlist?.musicDuration
        holder.itemView.setOnClickListener { playlist?.let { it1 -> listener(it1) } }

        Glide.with(context)
            .load(playlist?.coverPhoto)
            .apply(RequestOptions())
            .fitCenter()
            .error(R.drawable.rounded_pink_layout)
            .placeholder(R.drawable.rounded_pink_layout)
            .into(holder.ivMusic!!)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.list_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = filterList?.size ?: 0

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var tvTitle: TextView? = null
        var ivMusic: ImageView? = null
        var tvDuration: TextView? = null

        init {
            this.tvTitle = row.findViewById<TextView>(R.id.tvTitle)
            this.tvDuration = row.findViewById<TextView>(R.id.tvDuration)
            this.ivMusic = row.findViewById<ImageView>(R.id.ivMusic)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filterList = items
                } else {
                    var resultList: MutableList<DataItem>? = mutableListOf()
                    for (row in items) {
                        if (row.subCategory?.toLowerCase(Locale.ROOT)
                                ?.contains(charSearch.toLowerCase(Locale.ROOT))!!
                        ) {
                            resultList?.add(row)
                        }
                    }
                    filterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterList = results?.values as MutableList<DataItem>?
                notifyDataSetChanged()
            }

        }
    }


}