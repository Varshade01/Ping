package com.khrd.pingapp.homescreen.sendping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.pings.getResId
import com.khrd.pingapp.databinding.RecursionItemBinding

class RecursionAdapter(
    private val myContext: Context,
    @LayoutRes private val layoutResource: Int,
    private var items: List<Pair<RecurringTime, String?>>
) : ArrayAdapter<Pair<RecurringTime, String?>>(myContext, layoutResource) {
    private var selectedItem: RecurringTime = RecurringTime.NO_REPEAT

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Pair<RecurringTime, String?> {
        return items[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = RecursionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val item = items[position]
        binding.recursionTime.text = context.getString(item.first.getResId())
        binding.recursionTimeDetails.text = item.second
        if (item.second.isNullOrBlank()) {
            binding.recursionTimeDetails.visibility = View.GONE
        }
        if (item.first == selectedItem) {
            binding.selectedRecursion.visibility = View.VISIBLE
        }
        return binding.root
    }

    fun setData(newItems: List<Pair<RecurringTime, String?>>, chosenItem: RecurringTime) {
        selectedItem = chosenItem
        items = newItems
        notifyDataSetChanged()
    }
}
