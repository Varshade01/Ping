package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrd.pingapp.databinding.PingStatusDialogBinding
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.utils.getRelativeDate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class PingStatusDialogFragment : DialogFragment() {
    private var binding by Delegates.notNull<PingStatusDialogBinding>()
    private lateinit var recyclerAdapter: PingStatusReceiversAdapter
    private val viewModel: PingStatusDialogViewModel by viewModels()
    private val args: PingStatusDialogFragmentArgs by navArgs()

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = PingStatusDialogBinding.inflate(inflater, container, false)
        recyclerAdapter = PingStatusReceiversAdapter(imageLoader)
        val pingItem = args.sentPingItem
        initView(pingItem.emoji, getRelativeDate(pingItem.date), pingItem.views, pingItem.groupId, pingItem.receiver)
        initRecyclerView()
        initListeners()
        return binding.root
    }

    private fun initRecyclerView() {
        binding.membersRecyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun initListeners() {
        viewModel.getReceiversStatusItemsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                GetReceiversStatusFailure -> {
                }
                is GetReceiversStatusSuccess -> {
                    recyclerAdapter.setData(it.items)
                }
            }
        }

        viewModel.groupNameLiveData.observe(viewLifecycleOwner) { groupName ->
            binding.tvSentToReceiver.text = groupName
        }

        binding.btnOk.setOnClickListener {
            dismiss()
        }
    }

    private fun initView(emoji: String?, date: String?, views: List<String>, groupId: String?, receivers: List<UserItem>) {
        binding.tvPingEmoji.text = emoji
        binding.tvPingDate.text = date
        viewModel.getPingReceivers(receivers, views)

        if (groupId.isNullOrBlank()) {
            binding.tvSentToHeader.isVisible = false
        } else {
            getGroupName()
        }
    }

    private fun getGroupName() {
        val groupId = args.sentPingItem.groupId
        groupId?.let { viewModel.getGroupNameById(it) }
    }
}