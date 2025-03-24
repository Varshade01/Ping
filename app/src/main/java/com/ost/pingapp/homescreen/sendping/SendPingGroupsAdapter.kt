package com.khrd.pingapp.homescreen.sendping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes
import com.khrd.pingapp.R
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.databinding.SenderItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class SendPingGroupsAdapter(
    private val myContext: Context,
    @LayoutRes private val layoutResource: Int,
    private var items: Array<DatabaseGroup>,
    private var imageLoader: ImageLoader
) :
    ArrayAdapter<DatabaseGroup>(myContext, layoutResource, items) {
    private var groups: List<DatabaseGroup> = listOf()
    private var pattern: CharSequence? = ""

    override fun getCount(): Int = groups.size

    override fun getItem(position: Int): DatabaseGroup = groups[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = SenderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val sender = groups[position]
        binding.tvSenderGroupName.text = sender.name
        imageLoader.loadImage(sender.photoURL, binding.ivGroupPhoto, R.drawable.ic_default_group_avatar)
        return binding.root
    }

    fun showAll() {
        groups = items.toList()
    }

    fun setData(list: List<DatabaseGroup>) {
        if (dataHasChanged(list)) {
            items = list.toTypedArray()
            filter.filter(pattern)
        }
    }

    private fun dataHasChanged(listOfGroups: List<DatabaseGroup>) =
        items.size != listOfGroups.size || !items.toList().containsAll(listOfGroups)

    fun getItems() = items.toList()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()?.lowercase()?.trim()
                pattern = queryString
                val filterResults = FilterResults()
                filterResults.values = if (queryString.isNullOrEmpty()) {
                    items.toList()
                } else {
                    items.filter {
                        it.name?.lowercase()?.contains(queryString)!!
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                groups = results.values as List<DatabaseGroup>
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): String? {
                return (resultValue as DatabaseGroup).name
            }
        }
    }
}