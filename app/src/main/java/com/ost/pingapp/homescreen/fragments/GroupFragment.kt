package com.khrd.pingapp.homescreen.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants.GROUP
import com.khrd.pingapp.constants.Constants.GROUP_ID_FROM_DEEP_LINKING
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.databinding.FragmentGroupBinding
import com.khrd.pingapp.groupmanagement.GroupSetupActivity
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.homescreen.adapter.GroupAdapter
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.GroupBottomSheetFragment.Companion.KEY_FOR_GROUP_FRAGMENT_RESULT
import com.khrd.pingapp.homescreen.fragments.GroupBottomSheetFragment.Companion.KEY_FOR_GROUP_FRAGMENT_RESULT_BUNDLE
import com.khrd.pingapp.homescreen.fragments.GroupBottomSheetFragment.Companion.KEY_OPEN_JOIN_OR_CREATE
import com.khrd.pingapp.homescreen.states.GroupScreenState
import com.khrd.pingapp.homescreen.states.JoinByOutsideLinkError
import com.khrd.pingapp.homescreen.states.JoinByOutsideLinkFailure
import com.khrd.pingapp.homescreen.states.JoinByOutsideLinkSuccess
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.SwipeController
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class GroupFragment : Fragment(), SendPingToUserListener, NoMatchesFoundListener, MuteUserListener {
    private var binding by Delegates.notNull<FragmentGroupBinding>()
    private val viewModel: GroupViewModel by viewModels()
    private var groupAdapter: GroupAdapter? = null
    private val swipeController = SwipeController()

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var toastUtils: ToastUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("******** ${this.hashCode()}", "onCreateView")
        setHasOptionsMenu(true)
        binding = initialBinding(inflater, container)
        initActionBar()
        initRecyclerView()
        initListeners()
        initObservers()
        return binding.root
    }

    private fun setGroupAdapter(): GroupAdapter {
        val adapter = GroupAdapter(requireContext(), imageLoader, this).apply {
            this.passListener(this@GroupFragment, this@GroupFragment)
        }
        return adapter
    }

    private fun initialBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGroupBinding {
        return FragmentGroupBinding.inflate(inflater, container, false)
    }

    private fun initRecyclerView() {
        binding.groupRecyclerView.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }
    }

    private fun initListeners() {
        setAllGroupsSearchListener()
        groupChangeListener()
        handleJoiningByDeepLink()
        handleItemSwipe()

        binding.groupName.setOnClickListener {
            viewModel.clickOpenBottomSheet()
        }
        binding.startButton.setOnClickListener {
            setupJoinOrCreateScreen()
        }
        binding.shareLinkGroupButton.setOnClickListener {
            viewModel.invitationLinkClicked()
        }
    }

    private fun initObservers() {
        groupListDialogObserver()
        invitationLinkObserver()
        observeUsersLiveData()
        joinGroupObserver()
        sendPingObserver()
        searchInAllGroupsLiveDataObserver()
        groupScreenStateObserver()
    }

    private fun sendPingObserver() {
        viewModel.sendPingLiveData.observe(viewLifecycleOwner) { state ->
            state.getContentIfNotHandled()?.let {
                (activity as HomeScreen).openSendPingDialog(it.user, it.group)
            }
        }
    }

    private fun joinGroupObserver() {
        viewModel.joinGroupLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is JoinByOutsideLinkFailure -> {
                    when (it.error) {
                        JoinByOutsideLinkError.UNEXISTING_GROUP -> {
                            activity?.intent?.removeExtra(GROUP_ID_FROM_DEEP_LINKING)
                            toastUtils.showShortToast(R.string.join_unexisting_group_error)
                        }

                        JoinByOutsideLinkError.UNKNOWN_ERROR -> {
                            activity?.intent?.removeExtra(GROUP_ID_FROM_DEEP_LINKING)
                            toastUtils.showShortToast(R.string.unknown_error)
                        }
                    }
                }

                JoinByOutsideLinkSuccess -> {
                    activity?.intent?.removeExtra(GROUP_ID_FROM_DEEP_LINKING)
                    toastUtils.showShortToast(R.string.join_successful)
                }
            }
        }
    }

    private fun groupListDialogObserver() {
        viewModel.openGroupListDialogLiveData.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { list ->
                val action = GroupFragmentDirections.showBottomSheetDialog(list.first.toTypedArray(), list.second)
                findNavController().navigateSafe(action)
            }
        }
    }

    private fun invitationLinkObserver() {
        viewModel.shareInvitationLinkLiveData.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { link ->
                setupShareLink(link)
            }
        }
    }

    private fun searchInAllGroupsLiveDataObserver() {
        viewModel.searchInAllGroupsLiveData.observe(viewLifecycleOwner) {
            groupAdapter?.setSearchAllGroups(it)
        }
    }

    private fun observeUsersLiveData() {
        viewModel.usersLiveData.observe(viewLifecycleOwner) { usersData ->
            usersData?.let { updateData(it) }
        }
    }

    private fun groupScreenStateObserver() {
        viewModel.groupStateLiveData.observe(viewLifecycleOwner) { state ->
            setGroupState(state)
        }
    }

    private fun updateData(usersData: UsersItemsData) {
        groupAdapter?.setData(usersData.groupId, usersData.users, usersData.currentGroup) ?: run {
            groupAdapter = setGroupAdapter()
            groupAdapter?.setData(usersData.groupId, usersData.users, usersData.currentGroup)
            initRecyclerView()
        }
    }

    private fun groupChangeListener() {
        parentFragmentManager.setFragmentResultListener(KEY_FOR_GROUP_FRAGMENT_RESULT, viewLifecycleOwner) { _, bundle ->
            val result = bundle.getString(KEY_FOR_GROUP_FRAGMENT_RESULT_BUNDLE)
            viewModel.newGroupSelected(result)
        }
    }

    private fun setGroupState(screenState: GroupScreenState) {
        val groupExist = screenState.databaseGroup != null
        val groupLoaded = screenState.groupLoaded

        showProgressBar(groupLoaded)
        showFirstLoginScreen(groupExist, groupLoaded)
        showEmptyGroupMessage(screenState)
        showSendPingButton(groupExist)
        showSearchInAllGroups(screenState)
        showToolbar(groupExist)
        handleToolbarState(screenState)
    }

    private fun showProgressBar(groupLoaded: Boolean) {
        binding.groupLoadingProgress.isVisible = !groupLoaded
    }

    private fun showFirstLoginScreen(groupExist: Boolean, groupLoaded: Boolean) {
        binding.firstLoginScreen.isVisible = !groupExist && groupLoaded
        binding.groupRecyclerView.isVisible = groupExist
    }

    private fun showEmptyGroupMessage(screenState: GroupScreenState) {
        val groupExist = screenState.databaseGroup != null
        val groupLoaded = screenState.groupLoaded
        binding.groupViewNoUsers.isVisible = (screenState.isGroupEmpty && screenState.isSearchCollapsed && groupExist && groupLoaded)
    }

    private fun showSendPingButton(exist: Boolean) {
        requireActivity().findViewById<Button>(R.id.sendPing).isVisible = exist
    }

    private fun showSearchInAllGroups(screenState: GroupScreenState) {
        val searchInAllGroupsVisibility = !screenState.userHasOneGroup && !screenState.isSearchCollapsed
        binding.searchAllGroupsView.isVisible = searchInAllGroupsVisibility
    }

    private fun showToolbar(groupExist: Boolean) {
        binding.groupToolbar.isVisible = groupExist
    }

    private fun handleToolbarState(screenState: GroupScreenState) {
        val groupExist = screenState.databaseGroup != null
        if (groupExist && screenState.isSearchCollapsed) {
            showGroupName(screenState)
        } else {
            setToolbarState(false)
        }
    }

    private fun showGroupName(screenState: GroupScreenState) {
        binding.groupName.text = screenState.databaseGroup?.name
        binding.muteGroupItem.isVisible = screenState.isMuted
        setGroupImage(screenState.databaseGroup?.photoURL)
        setToolbarState(screenState.isSearchCollapsed)
    }

    private fun setGroupImage(imageUrl: String?) {
        imageLoader.loadImage(imageUrl, binding.groupImage, R.drawable.ic_default_group_avatar)
    }

    private fun setToolbarState(visible: Boolean) {
        binding.clickableAreaGroupName.isVisible = visible
        binding.groupToolbar.menu.findItem(R.id.action_search)?.isVisible = visible
        binding.groupToolbar.menu.findItem(R.id.settings)?.isVisible = visible
    }

    private fun setAllGroupsSearchListener() {
        binding.switchAllGroupsSearch.setOnCheckedChangeListener { compoundButton, isChecked -> viewModel.searchInAllGroups(isChecked) }
    }

    private fun setupJoinOrCreateScreen() {
        val intent = Intent(activity, GroupSetupActivity::class.java)
        intent.putExtra(KEY_OPEN_JOIN_OR_CREATE, true)
        startActivity(intent)
    }

    private fun setupShareLink(link: String) {
        val shareIntent = Intent().apply {
            this.action = Intent.ACTION_SEND
            this.putExtra(Intent.EXTRA_TEXT, link)
            this.type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_invitation_link_dialog_message)))
    }

    private fun initActionBar() {
        val toolbar = binding.groupToolbar
        toolbar.inflateMenu(R.menu.menu)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        initSearch()
        initSettings()
    }

    private fun initSearch() {
        val searchItem = binding.groupToolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        setOnQueryTextListener(searchView)
        setSearchViewParams(searchView)
        setOnQueryTextListener(searchView)
        setOnActionExpandListener(searchItem)
    }

    private fun initSettings() {
        val settingsItem = binding.groupToolbar.menu.findItem(R.id.settings)
        settingsItem.setOnMenuItemClickListener {
            openGroupSettings(viewModel.getCurrentGroup())
            true
        }
    }

    private fun openGroupSettings(currentGroup: DatabaseGroup?) {
        val intent = Intent(activity, GroupSetupActivity::class.java)
        intent.putExtra(GROUP, currentGroup)
        startActivity(intent)
    }

    private fun handleJoiningByDeepLink() {
        val groupId = activity?.intent?.getStringExtra(GROUP_ID_FROM_DEEP_LINKING)
        if (!groupId.isNullOrEmpty()) {
            val intent = Intent(activity, GroupSetupActivity::class.java)
            intent.putExtra(GROUP_ID_FROM_DEEP_LINKING, groupId)
            activity?.intent?.removeExtra(GROUP_ID_FROM_DEEP_LINKING)
            startActivity(intent)
        }
    }

    private fun setOnActionExpandListener(searchItem: MenuItem) {
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.setSearchState(collapsed = false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                groupAdapter?.filter?.filter("")
                viewModel.setSearchState(collapsed = true)
                return true
            }
        })
    }

    private fun setOnQueryTextListener(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                groupAdapter?.filter?.filter(newText)
                return false
            }
        })
    }

    private fun setSearchViewParams(searchView: SearchView) {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        val searchViewTextColor = ContextCompat.getColor(requireContext(), R.color.intouch_text)
        val hintTextColor = ContextCompat.getColor(requireContext(), R.color.intouch_neutral_01)
        searchEditText.setTextColor(searchViewTextColor)
        searchEditText.setHintTextColor(hintTextColor)
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
        searchEditText.filters = arrayOf<InputFilter>(LengthFilter(64))
        searchEditText.setPadding(0, 0, 0, 0)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //since onPrepareOptionsMenu can be called after received state we check this state
        val currentGroupExists = viewModel.groupStateLiveData.value?.databaseGroup != null
        val searchCollapsed = viewModel.groupStateLiveData.value?.isSearchCollapsed
        searchCollapsed?.let { setToolbarState(it && currentGroupExists) }
        Log.d("******** ${this.hashCode()}", "onPrepareOptionsMenu")
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadGroups()
    }

    override fun onSendPingToUserClicked(user: UserItem) {
        viewModel.onSendPingToUserClicked(user)
    }

    override fun noMatchesFound(noMatchesFound: Boolean) {
        binding.noUserFoundTextView.isVisible = noMatchesFound
    }

    fun SpannableStringBuilder.drawable(
        tv: TextView,
        @DrawableRes drawable: Int,
    ): SpannableStringBuilder {
        val icon = ContextCompat.getDrawable(tv.context, drawable)!!
        icon.setBounds(0, 0, tv.lineHeight, tv.lineHeight)
        return inSpans(ImageSpan(icon, DynamicDrawableSpan.ALIGN_BOTTOM)) { append("$drawable") }
    }

    private fun handleItemSwipe() {
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.groupRecyclerView)
    }

    override fun onMuteUserClicked(user: UserItem) {
        viewModel.onMuteUserClicked(user)
        swipeController.collapse()
    }
}