package com.khrd.pingapp.homescreen.sendping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes
import androidx.core.view.setPadding
import com.khrd.pingapp.R
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.databinding.ReceiverItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class ReceiversAdapter(
    private val myContext: Context,
    @LayoutRes private val layoutResource: Int,
    private var items: Array<DatabaseUser>,
    private val imageLoader: ImageLoader,
) :
    ArrayAdapter<DatabaseUser>(myContext, layoutResource, items) {
    private var receivers: List<DatabaseUser> = listOf()
    private var pattern: CharSequence? = ""

    init {
        receivers = items.asList()
    }

    fun setData(list: List<DatabaseUser>) {
        if (dataHasChanged(list)) {
            items = list.toTypedArray()
            filter.filter(pattern)
        }
    }

    private fun dataHasChanged(listOfGroups: List<DatabaseUser>) =
        items.size != listOfGroups.size || !items.toList().containsAll(listOfGroups)

    override fun getCount(): Int = receivers.size

    override fun getItem(position: Int): DatabaseUser = receivers[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = ReceiverItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val receiver = receivers[position]
        binding.tvName.text = receiver.username
        binding.ivProfilePicture.setPadding(0)
        binding.receiverItemInnerLinearLayout.setPadding(0)
        binding.tvName.setPadding(0)
        binding.tvJobPosition.setPadding(0)
        jobPositionVisibilityHandle(receiver, binding)
        imageLoader.loadImage(receiver.photoURL, binding.ivProfilePicture, R.drawable.ic_default_user_avatar)
        return binding.root
    }

    private fun jobPositionVisibilityHandle(
        receiver: DatabaseUser,
        binding: ReceiverItemBinding
    ) {
        if (receiver.job.isNullOrEmpty()) {
            binding.tvJobPosition.visibility = View.GONE
            binding.tvJobPosition.text = ""
        } else {
            binding.tvJobPosition.visibility = View.VISIBLE
            binding.tvJobPosition.text = receiver.job
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()?.lowercase()?.trim()
                pattern = queryString
                val filterResults = FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty()) {
                    items.toList()
                } else {
                    items.filter {
                        it.username?.contains(queryString, ignoreCase = true) == true
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                receivers = results.values as List<DatabaseUser>
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): String? {
                return (resultValue as DatabaseUser).username
            }
        }
    }
}