package com.khrd.pingapp.groupmanagement.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.CreateGroupItemBinding
import com.khrd.pingapp.groupmanagement.listeners.CreateGroupListener
import com.khrd.pingapp.groupmanagement.states.*

class CreateGroupItemViewHolder(binding: CreateGroupItemBinding, private val createGroupListener: CreateGroupListener) :
    RecyclerView.ViewHolder(binding.root) {
    val textInputLayout = binding.groupNameInputLayout
    val linkTextView = binding.linkTextView
    val copyButton = binding.copyLinkButton
    val doneButton = binding.doneButton
    val createButton = binding.createButton
    val view = binding.root

    init {
        textInputLayout.editText?.addTextChangedListener {
            createGroupListener.onCreateGroupNameChanged(it.toString().trim())
            if (it.toString().isNotEmpty()) {
                createButton.visibility = View.VISIBLE
            } else {
                createButton.visibility = View.GONE
                textInputLayout.isErrorEnabled = false
            }
        }
        createButton.setOnClickListener {
            createGroupListener.onCreateGroupAction(CreateGroupSaveAction)
        }
        textInputLayout.editText?.setOnClickListener {
            createGroupListener.onCreateGroupAction(CreateGroupEditAction(GroupNameValidationState.VALID, ""))
        }
        doneButton.setOnClickListener {
            createGroupListener.onCreateGroupAction(CreateGroupDoneAction)
        }
        copyButton.setOnClickListener {
            createGroupListener.onCreateGroupAction(CreateGroupCopyLinkAction(""))
        }
    }

    fun bind(item: CreateGroupItem) {
        if (item.nameValidationError == GroupNameValidationState.VALID) {
            when {
                item.isInactive -> {
                    item.isInactive = false
                    textInputLayout.isEnabled = true
                    textInputLayout.isErrorEnabled = false
                }
                item.isNameSaved -> {
                    item.name = textInputLayout.editText?.text.toString()
                    doneButton.visibility = Button.VISIBLE
                    linkTextView.visibility = TextView.VISIBLE
                    copyButton.visibility = Button.VISIBLE
                    createButton.visibility = Button.GONE
                    linkTextView.text = item.link
                    textInputLayout.isEnabled = false
                    textInputLayout.isErrorEnabled = false
                }
                else -> {
                    textInputLayout.isEnabled = true
                }
            }
        } else if (item.nameValidationError == GroupNameValidationState.EMPTY_FIELD) {
            textInputLayout.error = view.context.getString(R.string.empty_field)
        } else if (item.nameValidationError == GroupNameValidationState.TOO_LONG) {
            textInputLayout.error = view.context.getString(R.string.create_group_name_too_long_message)
        } else if (item.nameValidationError == GroupNameValidationState.INVALID_CHARS) {
            textInputLayout.error = view.context.getString(R.string.create_group_name_invalid_symbols)
        } else {
            textInputLayout.error = view.context.getString(R.string.unknown_error)
        }
    }
}

