package com.khrd.pingapp.homescreen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.UserStatusDialogBinding
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.di.DefaultCoroutineScope
import com.khrd.pingapp.homescreen.adapter.UserGroupsListAdapter
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusFailure
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusSuccess
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.getRelativeDate
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.utils.showZoomedImageOnTouch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class UserStatusDialogFragment : DialogFragment() {

    private var binding by Delegates.notNull<UserStatusDialogBinding>()
    private val viewModel: UserStatusViewModel by viewModels()
    private val homeScreenSharedViewModel: HomeScreenSharedViewModel by activityViewModels()
    private val args: UserStatusDialogFragmentArgs by navArgs()
    private var nestedAdapter: UserGroupsListAdapter? = null

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    @DefaultCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = UserStatusDialogBinding.inflate(inflater, container, false)
        isCancelable = true
        nestedAdapter = UserGroupsListAdapter(imageLoader).apply {
            setOnGroupClickListener(onGroupClick)
        }
        viewModel.init(args.userItem, args.dataBaseGroup)
        initRecyclerView()
        initListeners()
        return binding.root
    }

    private fun initRecyclerView() {
        binding.userItemDialogRecyclerView.apply {
            adapter = nestedAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
        }
        nestedAdapter?.onGroupClick = { groupId ->
            this.dismiss()
            homeScreenSharedViewModel.navigateToGroupTab()
            setFragmentResult(
                GroupBottomSheetFragment.KEY_FOR_GROUP_FRAGMENT_RESULT,
                bundleOf(GroupBottomSheetFragment.KEY_FOR_GROUP_FRAGMENT_RESULT_BUNDLE to groupId)
            )
        }
    }

    private fun initListeners() {
        binding.muteUserButton.setOnClickListener {
            viewModel.muteUser()
        }

        binding.sendPingButton.setOnClickListener {
            viewModel.openSendPingDialog(args.userItem.userId, findNavController())
        }

        viewModel.isUserMutedLivedata.observe(viewLifecycleOwner) { state ->
            when (val content = state.getContentIfNotHandled()) {
                is GetUserMutedStatusSuccess -> {
                    if (content.isMuted) {
                        binding.muteUserButton.text = getString(R.string.unmute_user)
                        toastUtils.showShortToast(getString(R.string.user_is_muted))
                        binding.muteUserButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unmute_user_icon, 0, 0, 0)
                    } else {
                        binding.muteUserButton.text = getString(R.string.mute_user)
                        toastUtils.showShortToast(getString(R.string.user_is_unmuted))
                        binding.muteUserButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_muteusericon, 0, 0, 0)
                    }
                }

                is GetUserMutedStatusFailure -> {
                    toastUtils.showNetworkErrorToast()
                }

                else -> {}
            }

        }
        viewModel.userLivedata.observe(viewLifecycleOwner) {
            initView(it)
        }

    }

    private fun initView(userItem: UserItem) {
        imageLoader.loadImage(userItem.photoURL, binding.userItemImage, R.drawable.ic_default_user_avatar)
        binding.userItemFullName.text = userItem.fullname
        binding.userItemJobPosition.text = userItem.job

        binding.userItemJobPosition.isVisible = !userItem.job.isNullOrEmpty()


        if (userItem.muted) {
            binding.muteUserButton.text = getString(R.string.unmute_user)
            binding.muteUserButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unmute_user_icon, 0, 0, 0)
        } else {
            binding.muteUserButton.text = getString(R.string.mute_user)
            binding.muteUserButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_muteusericon, 0, 0, 0)
        }

        if (!userItem.isHide) {
            nestedAdapter?.setData(userItem.groups)
            binding.userItemLastSeen.text = lastSeenText(userItem)
            binding.userItemIsOnline.isVisible = userItem.isOnline?.status == true
        } else {
            binding.userItemLastSeen.text = getString(R.string.last_seen) + " " + getString(R.string.recently)
            binding.commonGroups.isVisible = false
        }
        userItem.photoURL?.let {
            binding.userItemImage.showZoomedImageOnTouch(
                findNavController(),
                it,
            )
        }
    }

    private fun lastSeenText(userItem: UserItem) = if (userItem.isOnline?.status == true) {
        getString(R.string.online)
    } else {
        userItem.isOnline?.timestamp?.let { it1 ->
            context?.getString(R.string.last_seen) + " " + getRelativeDate(it1)
        } ?: (context?.getString(R.string.last_seen) + " " + context?.getString(R.string.unknown))
    }
}