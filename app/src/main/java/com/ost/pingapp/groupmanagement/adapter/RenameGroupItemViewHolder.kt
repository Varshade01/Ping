package com.khrd.pingapp.groupmanagement.adapter

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.RenameGroupItemBinding
import com.khrd.pingapp.groupmanagement.listeners.RenameGroupListener
import com.khrd.pingapp.groupmanagement.states.GroupNameValidationState
import com.khrd.pingapp.groupmanagement.states.RenameGroupEditAction
import com.khrd.pingapp.groupmanagement.states.RenameGroupSaveAction

class RenameGroupItemViewHolder(
    private val binding: RenameGroupItemBinding,
    private val renameGroupListener: RenameGroupListener
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnRenameGroupEdit.setOnClickListener {
            renameGroupListener.onRenameGroupAction(RenameGroupEditAction)
        }
        binding.btnRenameGroupSave.setOnClickListener {
            renameGroupListener.onRenameGroupAction(RenameGroupSaveAction(name = binding.tilRenameGroup.editText?.text.toString()))
        }

        binding.tieRenameGroup.addTextChangedListener { if (!it.isNullOrBlank()) binding.tilRenameGroup.error = "" }
    }

    fun bind(item: RenameGroupItem) {
        val currentItemName = item.name

        when {
            item.isInEditState -> {
                binding.tilRenameGroup.isEnabled = true

                when (item.nameValidation) {
                    GroupNameValidationState.EMPTY_FIELD -> {
                        binding.tilRenameGroup.error = binding.root.context.getString(R.string.empty_field)
                    }
                    GroupNameValidationState.TOO_LONG -> {
                        binding.tilRenameGroup.error = binding.root.context.getString(R.string.create_group_name_too_long_message)
                    }
                    GroupNameValidationState.INVALID_CHARS -> {
                        binding.tilRenameGroup.error = binding.root.context.getString(R.string.name_validation_message)
                    }
                    GroupNameValidationState.VALID -> {
                    }
                }

                binding.btnRenameGroupEdit.visibility = View.INVISIBLE
                binding.btnRenameGroupSave.visibility = View.VISIBLE

            }
            item.isInSavingState -> {
                binding.tilRenameGroup.editText?.setText(currentItemName)
                binding.tilRenameGroup.isEnabled = false
                item.isInSavingState = false

                binding.btnRenameGroupEdit.visibility = View.VISIBLE
                binding.btnRenameGroupSave.visibility = View.INVISIBLE

            }
            else -> {
                binding.tilRenameGroup.editText?.setText(currentItemName)
                binding.tilRenameGroup.isEnabled = false

                binding.btnRenameGroupEdit.visibility = View.VISIBLE
                binding.btnRenameGroupSave.visibility = View.INVISIBLE
            }
        }
    }
}