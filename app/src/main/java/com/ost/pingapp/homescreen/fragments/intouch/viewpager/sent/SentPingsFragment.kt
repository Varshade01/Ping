package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.R
import com.khrd.pingapp.alarmManager.ScheduledPingsReceiver
import com.khrd.pingapp.databinding.FragmentSentPingsBinding
import com.khrd.pingapp.homescreen.fragments.intouch.PingsFragmentDirections
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.PingsRecyclerAdapter
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledHeader
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.isGroupPing
import com.khrd.pingapp.homescreen.listeners.OnSeenClickedListener
import com.khrd.pingapp.homescreen.sendping.BaseSentPingsError.ScheduledPingsError
import com.khrd.pingapp.homescreen.sendping.BaseSentPingsError.SentPingsError
import com.khrd.pingapp.homescreen.sendping.SentPingsState
import com.khrd.pingapp.homescreen.states.CancelPingFailure
import com.khrd.pingapp.homescreen.states.CancelPingListener
import com.khrd.pingapp.homescreen.states.CancelPingOffline
import com.khrd.pingapp.homescreen.states.CancelPingSuccess
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.workmanager.PingAppWorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SentPingsFragment : Fragment(), CancelPingListener, ScheduledPingsHeaderListener, OnSeenClickedListener {

    private val viewModel by viewModels<SentPingsViewModel>()
    private lateinit var binding: FragmentSentPingsBinding
    private var recyclerAdapter: PingsRecyclerAdapter? = null

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    lateinit var pingAppWorkManager: PingAppWorkManager

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSentPingsBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        cancelPingStateObserver()
        errorEffectLiveDataObserver()
        sentPingStateObserver()
    }

    private fun cancelPingStateObserver() {
        viewModel.cancelPingStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is CancelPingSuccess -> {
                    cancelAlarm(it.pingId, it.time)
                }

                is CancelPingFailure -> {
                    toastUtils.showToast(getString(R.string.ping_cancelling_failed))
                }

                is CancelPingOffline -> {
                    toastUtils.showToast(getString(R.string.cancel_ping_offline))
                    cancelAlarm(it.pingId, it.time)
                    cancelScheduledPing(it.pingId)
                }

                else -> {}
            }
        }
    }

    private fun errorEffectLiveDataObserver() {
        viewModel.errorEffectLiveData.observe(viewLifecycleOwner) { errorEvent ->
            errorEvent.getContentIfNotHandled()?.let { effect ->
                when (effect) {
                    SentPingsError -> toastUtils.showToast(R.string.failed_to_load_sent_pings)
                    ScheduledPingsError -> toastUtils.showToast(R.string.failed_to_load_scheduled_pings)
                }
            }
        }
    }

    private fun sentPingStateObserver() {
        viewModel.sentPingsStateLiveData.observe(viewLifecycleOwner) { state ->
            binding.emptySentPingsMessage.isVisible = state.sentItems.isEmpty()
            updateData(state)
        }
        viewModel.openGroupStatusLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                if (state.sentPingItem.isGroupPing()) {
                    state.sentPingItem.groupFrom?.let {
                        view?.let { view ->
                            Navigation.findNavController(view).navigateSafe(
                                PingsFragmentDirections.showGroupUsersStatusDialog(it, state.dataBaseUser)
                            )
                        }
                    }
                } else if (state.sentPingItem.receiver.size == 1 &&
                    state.sentPingItem.receiver.first().isDeleted != true
                ) {
                    view?.let { view ->
                        state.sentPingItem.groupFrom?.let { dataBaseGroup ->
                            HomescreenNavGraphDirections.showUserStatusDialog(
                                state.sentPingItem.receiver.first(),
                                dataBaseGroup
                            )
                        }?.let { navDirections ->
                            Navigation.findNavController(view)
                                .navigateSafe(navDirections)
                        }
                    }
                } else if (state.sentPingItem.receiver.size > 1) {
                    state.sentPingItem.receiver.let {
                        view?.let { view ->
                            state.sentPingItem.groupFrom?.let { dataBaseGroup ->
                                PingsFragmentDirections.showMultipleUsersDialogFragment(
                                    dataBaseGroup,
                                    it.toTypedArray(),
                                )
                            }?.let { navDirections ->
                                Navigation.findNavController(view).navigateSafe(
                                    navDirections
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    private fun updateData(state: SentPingsState) {
        recyclerAdapter?.setData(state.sentItems, state.scheduledItems, state.showScheduled) ?: run {
            recyclerAdapter = PingsRecyclerAdapter(this, this, this, onPhotoClickListener = {
                viewModel.onPhotoClicked(it)
            })
            recyclerAdapter?.setData(state.sentItems, state.scheduledItems, state.showScheduled)
            initRecyclerView()
        }
    }

    private fun initRecyclerView() {
        binding.sentPingsRecyclerView.apply {
            adapter = recyclerAdapter
            itemAnimator = null

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    Log.d("Index", lastVisibleItemPosition.toString())
                    val adapter = recyclerView.adapter as PingsRecyclerAdapter
                    when (adapter.currentList[lastVisibleItemPosition]) {
                        is ReceivedPingItem -> {
                        }

                        is SentPingScheduledHeader -> {
                        }

                        is SentPingScheduledItem -> {
                            checkScheduledPingsPagination(lastVisibleItemPosition, adapter)
                        }

                        is SentPingItem -> {
                            checkPingsPagination(lastVisibleItemPosition, adapter)
                        }

                        else -> {}
                    }
                }
            })
        }
    }

    private fun checkPingsPagination(
        lastVisibleItemPosition: Int,
        adapter: PingsRecyclerAdapter
    ) {
        if (lastVisibleItemPosition == adapter.itemCount - 1) {
            viewModel.onRecyclerViewScrolled(LoadSentPingsAction)
            Log.d("Loading", "Regular pings")
        }
    }

    private fun checkScheduledPingsPagination(
        lastVisibleItemPosition: Int,
        adapter: PingsRecyclerAdapter
    ) {
        val nextInvisibleItemPosition = lastVisibleItemPosition + 1
        val nextPlusOneInvisibleItemPosition = lastVisibleItemPosition + 2
        val nextItemExists = nextInvisibleItemPosition < adapter.itemCount
        var nextItemIsScheduled = false
        if (nextItemExists)
            nextItemIsScheduled = adapter.currentList[nextInvisibleItemPosition] is SentPingScheduledItem
        val nextPlusOneItemIsLast = nextPlusOneInvisibleItemPosition == adapter.itemCount - 1
        val nextPlusOneItemExists = nextPlusOneInvisibleItemPosition < adapter.itemCount
        var nextPlusOneItemIsUnScheduled = false
        if (nextPlusOneItemExists)
            nextPlusOneItemIsUnScheduled = adapter.currentList[nextPlusOneInvisibleItemPosition] is SentPingItem
        if (nextItemExists && nextItemIsScheduled && (nextPlusOneItemIsLast || nextPlusOneItemIsUnScheduled)) {
            viewModel.onRecyclerViewScrolled(LoadScheduledPingsAction)
            Log.d("Loading", "Scheduled pings")
        }
    }

    override fun onHeaderClicked() {
        viewModel.onHeaderClicked()
    }

    private fun cancelAlarm(pingId: String, time: Long) {
        val alarmManager = requireActivity().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireActivity().applicationContext, ScheduledPingsReceiver::class.java)
        intent.action = pingId
        val pendingIntent = PendingIntent.getBroadcast(
            requireActivity().applicationContext, time.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onCancelPing(pingId: String, time: Long) {
        viewModel.onCancelPing(pingId, time)
    }

    override fun onSeenClicked(sentPingItem: SentPingItem) {
        val action = SentPingsFragmentDirections.showPingStatusDialog(sentPingItem)
        findNavController().navigateSafe(action)
    }

    private fun cancelScheduledPing(pingId: String) {
        pingAppWorkManager.startCancelScheduledPingOfflineWorker(pingId)
    }
}