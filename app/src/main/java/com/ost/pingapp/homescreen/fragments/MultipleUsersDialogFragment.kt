package com.khrd.pingapp.homescreen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.databinding.FragmentMultipleUsersBinding
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.utils.showZoomedImageOnTouch
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.avatarview.coil.loadImage
import kotlin.properties.Delegates

@AndroidEntryPoint
class MultipleUsersDialogFragment : DialogFragment() {
    private var binding by Delegates.notNull<FragmentMultipleUsersBinding>()
    private val args: MultipleUsersDialogFragmentArgs by navArgs()
    private val viewModel: MultipleUsersViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMultipleUsersBinding.inflate(inflater, container, false)
        args.group?.let { viewModel.init(args.users.toList(), it) }
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.sendPingButton.setOnClickListener {
            viewModel.openSendPingDialog(findNavController())
        }
        viewModel.multipleUsersLivedata.observe(viewLifecycleOwner) {
            initView(it)
        }
    }

    private fun provideReceiversNames(items: List<UserItem>): String {
        val listReceivers = mutableListOf<String>()
        items.forEach { userItems ->
            if (userItems.isDeleted == true) {
                listReceivers.add(binding.root.resources.getString(R.string.deleted_user))
            } else {
                listReceivers.add(userItems.fullname?.toString() ?: "")
            }
        }
        return listReceivers.joinToString()
    }

    private fun initView(listUsers: List<UserItem>) {
        binding.conversationUsersTv.text = provideReceiversNames(listUsers)
        val defaultDrawableIcon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_default_user_avatar) }
        //set photo to the first four users, if the url is null, set default icon
        val photos = listUsers.take(4)
            .map { it.photoURL ?: Constants.USER_DEFAULT_AVATAR }

        binding.conversationIc.apply {
            errorPlaceholder = defaultDrawableIcon
            placeholder = defaultDrawableIcon
            loadImage(photos)
            showZoomedImageOnTouch(
                findNavController(),
                photoURLs = photos,
            )

        }
    }
}


