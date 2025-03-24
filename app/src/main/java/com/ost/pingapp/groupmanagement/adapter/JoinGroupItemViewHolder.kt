package com.khrd.pingapp.groupmanagement.adapter

import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.JoinGroupItemBinding
import com.khrd.pingapp.groupmanagement.listeners.JoinGroupListener
import com.khrd.pingapp.groupmanagement.states.JoinGroupError.*

class JoinGroupItemViewHolder(binding: JoinGroupItemBinding, private val joinGroupListener: JoinGroupListener) :
    RecyclerView.ViewHolder(binding.root) {
    val linkInputLayout = binding.groupLinkInputLayout
    val joinButton = binding.groupJoinButton
    val view = binding.root

    init {
        joinButton.setOnClickListener {
            joinGroupListener.onJoinGroupAction()
        }

        linkInputLayout.editText?.addTextChangedListener {
            joinGroupListener.onJoinGroupLinkChanged(it.toString().trim())
            if (it.toString().isNotEmpty()) {
                joinButton.visibility = View.VISIBLE
            } else {
                joinButton.visibility = View.GONE
                linkInputLayout.error = null
            }
        }
    }

    fun bind(item: JoinGroupItem) {
        when (item.joinGroupError) {
            LINK_INVALID -> {
                linkInputLayout.error = view.context.getString(R.string.invalid_join_link_error)
            }
            EMPTY_FIELD -> {
                linkInputLayout.error = view.context.getString(R.string.fillout_the_field)
            }
            UNEXISTING_GROUP -> {
                linkInputLayout.error = view.context.getString(R.string.join_unexisting_group_error)
            }
            UNKNOWN_ERROR -> {
                linkInputLayout.error = view.context.getString(R.string.unknown_error)
            }
            SAME_GROUP_ERROR -> {
                linkInputLayout.error = view.context.getString(R.string.join_group_already_being_there_error)
            }
            LINK_VALID -> {
                linkInputLayout.error = null
            }
            OUTSIDE_LINK_SAME_GROUP_ERROR -> {
                Toast.makeText(view.context, R.string.join_group_already_being_there_error, Toast.LENGTH_LONG).show()
            }
            OUTSIDE_LINK_INVALID -> {
                Toast.makeText(view.context, R.string.join_unexisting_group_error, Toast.LENGTH_LONG).show()
            }
            NETWORK_ERROR -> {
                Toast.makeText(view.context, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            }

        }

    }
}