package com.khrd.pingapp.homescreen.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.databinding.BottomSheetGroupBinding
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.groupmanagement.GroupSetupActivity
import com.khrd.pingapp.groupmanagement.states.MuteGroupFailure
import com.khrd.pingapp.groupmanagement.states.MuteGroupSuccess
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.GroupBottomSheetAdapter
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.SwipeController
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class GroupBottomSheetFragment : BottomSheetDialogFragment(), BottomSheetListener {

    private var binding by Delegates.notNull<BottomSheetGroupBinding>()
    private val args: GroupBottomSheetFragmentArgs by navArgs()
    private val viewModel: GroupBottomSheetViewModel by viewModels()
    private var groupBottomSheetAdapter: GroupBottomSheetAdapter? = null
    private var swipeController = SwipeController()

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val coordinator = (it as BottomSheetDialog).findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
            val containerLayout = it.findViewById<FrameLayout>(com.google.android.material.R.id.container)
            val textView = bottomSheetDialog.layoutInflater.inflate(R.layout.sticky_layout_for_dialog, null)
            coordinator!!.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet).apply {
            }
            textView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
            containerLayout!!.addView(textView)

            /*
            * Dynamically update bottom sheet containerLayout bottom margin to buttons view height
            * */
            textView.post {
                (coordinator.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    textView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    this.bottomMargin = textView.measuredHeight
                    containerLayout.requestLayout()
                }
            }
            textView.findViewById<TextView>(R.id.join_or_create_group).setOnClickListener {
                this.dismiss()
                setupJoinOrCreateScreen()
            }
        }
        return bottomSheetDialog
    }

    private fun setupJoinOrCreateScreen() {
        val intent = Intent(activity, GroupSetupActivity::class.java)
        intent.putExtra(KEY_OPEN_JOIN_OR_CREATE, true)
        val groupId = activity?.intent?.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING)
        intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, groupId)
        activity?.intent?.removeExtra(Constants.GROUP_ID_FROM_DEEP_LINKING)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = binding.bottomSheetGroupRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        lifecycleScope.launch() {
            dataStoreManager.getCurrentGroup().let {
                groupBottomSheetAdapter =
                    GroupBottomSheetAdapter(it, args.listOfGroups, args.user, imageLoader, this@GroupBottomSheetFragment)
                recyclerView.itemAnimator = null
                recyclerView.adapter = groupBottomSheetAdapter
            }
        }
        handleItemSwipe()
        initMuteGroupButtonListener()
    }

    private fun handleItemSwipe() {
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.bottomSheetGroupRecyclerView)
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun openSendPingDialog(groupId: String, isGroupPing: Boolean) {
        val action = GroupBottomSheetFragmentDirections.openSendPingDialog(group = groupId, sentToEveryone = isGroupPing)
        findNavController().navigateSafe(action)
    }

    override fun passGroupId(groupId: String) {
        setFragmentResult(KEY_FOR_GROUP_FRAGMENT_RESULT, bundleOf(KEY_FOR_GROUP_FRAGMENT_RESULT_BUNDLE to groupId))
        this.dismiss()
    }

    override fun muteGroup(groupId: String, isMuted: Boolean) {
        viewModel.onMuteGroupClicked(groupId, isMuted)
        swipeController.collapse()
    }

    private fun initMuteGroupButtonListener() {
        viewModel.updateMuteGroupButtonStateLiveData.observe(this) {
            when (it) {
                is MuteGroupSuccess -> {
                    groupBottomSheetAdapter?.onMuteGroupActionChanged(groupId = it.groupId, state = it.isMuted)
                }

                is MuteGroupFailure -> {}
            }
        }
    }

    companion object {
        const val TAG = "GroupBottomSheet"
        const val KEY_FOR_GROUP_LIST = "group"
        const val KEY_OPEN_JOIN_OR_CREATE = "keyForGroupSetupIntent"
        const val ITEM_TYPE_GROUP = 1
        const val KEY_FOR_GROUP_FRAGMENT_RESULT = "requestKeyGroupAdapter"
        const val KEY_FOR_GROUP_FRAGMENT_RESULT_BUNDLE = "keyForGroupFragmentResultBundle"
    }
}


