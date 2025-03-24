package com.khrd.pingapp.homescreen.adapter

import android.content.Context
import android.text.Spannable.SPAN_EXCLUSIVE_INCLUSIVE
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.text.clearSpans
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.UserItemBinding
import com.khrd.pingapp.homescreen.fragments.MuteUserListener
import com.khrd.pingapp.homescreen.fragments.NoMatchesFoundListener
import com.khrd.pingapp.homescreen.fragments.SendPingToUserListener
import com.khrd.pingapp.utils.imageLoader.ImageLoader

const val ITEM_TYPE_USER = 1

class GroupAdapter(
    private val context: Context,
    private val imageLoader: ImageLoader,
    private val muteUserListener: MuteUserListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var _itemsFiltered: List<UserItem> = listOf()
    private var _items: MutableMap<String, List<UserItem>> = mutableMapOf()
    private var _currentPattern: CharSequence? = null
    private var _listener: SendPingToUserListener? = null
    private var _searchInAllGroups = false
    private var _currentGroupId: String = ""
    var listenerIsNoMatchesFound: NoMatchesFoundListener? = null
    private val _viewHolderList = mutableListOf<UserItemViewHolder>()

    fun passListener(listener: SendPingToUserListener, listenerIsNoMatchesFound: NoMatchesFoundListener) {
        this._listener = listener
        this.listenerIsNoMatchesFound = listenerIsNoMatchesFound
        _viewHolderList.forEach {
            it.setViewHolderListener(listener)
        }
    }

    fun setSearchAllGroups(searchInAll: Boolean) {
        _searchInAllGroups = searchInAll
        filter.filter(_currentPattern)
        _viewHolderList.forEach {
            it.setSearchInAllGroup(_searchInAllGroups)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_USER -> {
                val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserItemViewHolder(binding, _listener, _searchInAllGroups, imageLoader, muteUserListener).also {
                    _viewHolderList.add(it)
                }
            }

            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = _itemsFiltered.get(position)
        (holder as UserItemViewHolder).bind(currentItem)
    }

    override fun getItemViewType(position: Int): Int = ITEM_TYPE_USER

    override fun getItemCount(): Int = _itemsFiltered.size

    fun setData(groupId: String, items: List<UserItem>, currentGroup: Boolean) {
        if (currentGroup) {
            _currentGroupId = groupId
        }
        this._items[groupId] = alphabeticalSort(items)
        filter.filter(_currentPattern)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                _currentPattern = constraint
                val results = FilterResults()
                results.values = provideFilteredList(constraint)
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                handleResults(results)
                handleNoMatchesCase()
                notifyDataSetChanged()
            }

            private fun handleResults(results: FilterResults?) {
                results?.values?.let { filterResultsValue ->
                    _itemsFiltered = provideSortedItems(filterResultsValue)
                } ?: run {
                    _itemsFiltered
                }
            }
        }
    }

    private fun provideFilteredList(constraint: CharSequence?): Collection<UserItem>? {
        return if (constraint.isNullOrBlank()) {
            _items[_currentGroupId]
        } else {
            filteredUsers()
        }
    }

    private fun provideSortedItems(filterResultsValue: Any): List<UserItem> {
        val filteredUsers = (filterResultsValue as List<UserItem>)
        val spannedUsers = handleUsersSpans(filteredUsers)
        val sortedByAlphabetUsers = alphabeticalSort(spannedUsers)
        val sortedByRelevanceUsers = relevanceSort(sortedByAlphabetUsers)
        return sortedByRelevanceUsers
    }

    private fun handleNoMatchesCase() {
        val noMatches = (_itemsFiltered.isEmpty() && _currentPattern?.isNotEmpty() == true)
        listenerIsNoMatchesFound?.noMatchesFound(noMatches)
    }

    private fun filteredUsers(): Collection<UserItem> {
        val itemsForSearch = provideItemsForSearch()
        val uniqueUsers = removeDuplicates(itemsForSearch)
        return filterUsersBySearchPattern(uniqueUsers)
    }

    private fun provideItemsForSearch(): List<UserItem> {
        return if (_searchInAllGroups) {
            _items.values.flatten()
        } else {
            _items[_currentGroupId] ?: listOf()
        }
    }

    private fun removeDuplicates(users: List<UserItem>): List<UserItem> {
        val result: MutableList<UserItem> = mutableListOf()
        users.forEach { user ->
            val temp = users.filter { it.userId == user.userId }.sortedByDescending { it.isOnline?.timestamp }.first()
            result.add(temp)
        }
        return result
    }

    private fun filterUsersBySearchPattern(itemsForSearch: List<UserItem>): List<UserItem> {
        val filteredUserItems: HashSet<UserItem> = hashSetOf()
        itemsForSearch.forEach { userItem ->
            if (containsSearchPattern(userItem)) {
                filteredUserItems.add(userItem)
            }
        }
        return filteredUserItems.toList()
    }

    private fun containsSearchPattern(userItem: UserItem) =
        userItem.fullname?.toString()?.contains(_currentPattern.toString(), ignoreCase = true) ?: false

    private fun handleUsersSpans(users: List<UserItem>): List<UserItem> {
        return if (_currentPattern.isNullOrBlank()) {
            users.map { removeUserSpans(it) }
        } else {
            users.map { setUserSpans(it) }
        }
    }

    private fun setUserSpans(currentItem: UserItem): UserItem {
        val startPos = findSpanStartPosition(currentItem)
        val endPos = findSpanEndPosition(startPos)
        val color = context.getColor(R.color.intouch_accent)
        removeUserSpans(currentItem)
        return currentItem.apply { fullname?.setSpan(BackgroundColorSpan(color), startPos, endPos, SPAN_EXCLUSIVE_INCLUSIVE) }
    }

    private fun removeUserSpans(userItem: UserItem) = userItem.apply { this.fullname?.clearSpans() }

    private fun findSpanStartPosition(currentItem: UserItem): Int {
        val startPosition = currentItem.fullname?.toString()?.indexOf(_currentPattern.toString(), ignoreCase = true) ?: 0
        return if (startPosition < 0) 0 else startPosition
    }

    private fun findSpanEndPosition(startPos: Int) = startPos.plus(_currentPattern.toString().length)

    private fun alphabeticalSort(users: List<UserItem>): List<UserItem> {
        return users.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.fullname.toString() })
    }

    private fun relevanceSort(users: List<UserItem>): List<UserItem> {
        return users.sortedWith(compareBy { it.fullname?.indexOf(_currentPattern.toString(), ignoreCase = true) })
    }
}