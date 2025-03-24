package com.khrd.pingapp.homescreen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.khrd.pingapp.R
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.databinding.FragmentGroupStatusDialogBinding
import com.khrd.pingapp.di.DefaultCoroutineScope
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusFailure
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusSuccess
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.utils.showZoomedImageOnTouch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class GroupStatusDialogFragment : DialogFragment() {

    private var binding by Delegates.notNull<FragmentGroupStatusDialogBinding>()

    private val viewModel: GroupStatusDialogViewModel by viewModels()
    private val args: GroupStatusDialogFragmentArgs by navArgs()

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    @DefaultCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupStatusDialogBinding.inflate(inflater, container, false)
        initListeners()

        viewModel.init(args.dataBaseGroup, args.dataBaseUser)
        return binding.root
    }

    private fun initListeners() {
        binding.muteGroupButton.setOnClickListener {
            viewModel.muteGroup()
        }

        binding.sendPingButton.setOnClickListener {
            viewModel.openSendPingDialog(args.dataBaseGroup.id, findNavController(), sentToEveryone = true)
        }

        viewModel.isUserMutedLivedata.observe(viewLifecycleOwner) { state ->
            when (val content = state.getContentIfNotHandled()) {
                is GetUserMutedStatusSuccess -> {
                    if (content.isMuted) {
                        binding.muteGroupButton.text = getString(R.string.unmute_group)
                        toastUtils.showShortToast(getString(R.string.muted_group))
                        binding.muteGroupButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unmute_user_icon, 0, 0, 0)
                    } else {
                        binding.muteGroupButton.text = getString(R.string.mute_group)
                        toastUtils.showShortToast(getString(R.string.unmuted_group))
                        binding.muteGroupButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_muteusericon, 0, 0, 0)
                    }
                }

                is GetUserMutedStatusFailure -> {
                    toastUtils.showNetworkErrorToast()
                }

                else -> {}
            }

        }
        viewModel.allUsersLivedata.observe(viewLifecycleOwner) {
            initView(it.first, it.second)
        }
    }

    private fun initView(databaseGroup: DatabaseGroup, databaseUser: DatabaseUser) {
        imageLoader.loadImage(databaseGroup.photoURL, binding.groupItemImage, R.drawable.ic_default_group_avatar)
        binding.groupName.text = databaseGroup.name
        val muted = databaseUser.mutedItems?.containsKey(databaseGroup.id) == true
        if (muted) {
            binding.muteGroupButton.text = getString(R.string.unmute_group)
            binding.muteGroupButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unmute_user_icon, 0, 0, 0)
        } else {
            binding.muteGroupButton.text = getString(R.string.mute_group)
            binding.muteGroupButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_muteusericon, 0, 0, 0)
        }
        databaseGroup.photoURL?.let {
            binding.groupItemImage.showZoomedImageOnTouch(
                findNavController(),
                it,
            )
        }
    }
}