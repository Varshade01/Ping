package com.khrd.pingapp.groupmanagement

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.khrd.pingapp.BuildConfig
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupImageUpdateOfflineURI
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.databinding.ActivityGroupSetupBinding
import com.khrd.pingapp.groupmanagement.adapter.*
import com.khrd.pingapp.groupmanagement.listeners.*
import com.khrd.pingapp.groupmanagement.states.*
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.homescreen.fragments.GroupBottomSheetFragment.Companion.KEY_OPEN_JOIN_OR_CREATE
import com.khrd.pingapp.utils.SuccessMessageDialog
import com.khrd.pingapp.utils.WarningDialog
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.workmanager.PingAppWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class GroupSetupActivity : PingAppBaseActivity(), CreateGroupListener, RenameGroupListener, JoinGroupListener, LeaveGroupListener,
    ShareLinkGroupListener, UpdateGroupImageListener, MuteGroupListener {
    private var binding by Delegates.notNull<ActivityGroupSetupBinding>()
    private var groupSetupAdapter: GroupSetupAdapter? = null
    private val viewModel: GroupSetupViewModel by viewModels()
    private var _screenItems = mutableListOf<DisplayableItem>()

    private var tempImagePath: String? = null

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var workManager: PingAppWorkManager

    @Inject
    lateinit var groupImageOfflineUpdateHelper: GroupImageOfflineUpdateHelper

    @Inject
    lateinit var checkMutedItemsUseCase: CheckMutedItemsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSetupBinding.inflate(layoutInflater)
        initActionBar()
        val group = intent.getParcelableExtra<DatabaseGroup?>(Constants.GROUP)
        initAdapter()
        viewModel.initGroup(group)
        initRecyclerView(group)
        initListeners()
        setContentView(binding.root)
        val groupId = intent.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING)
        viewModel.setGroupIdFromDeepLink(groupId)
    }

    private fun initActionBar() {
        setSupportActionBar(binding.tbGroupSetup)
        binding.tbGroupSetup.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initAdapter() {
        groupSetupAdapter = GroupSetupAdapter(this, this, this, this, this, this, this, imageLoader)
    }

    private fun initRecyclerView(group: DatabaseGroup?) {
        setRecyclerViewParameters()
        if (isJoinOrCreate()) {
            setJoinOrCreateScreen()
        } else {
            setGroupSettingsScreen(group)
        }
    }

    private fun setRecyclerViewParameters() {
        binding.groupSetupRecyclerView.apply {
            adapter = groupSetupAdapter
            layoutManager = LinearLayoutManager(this@GroupSetupActivity)
            itemAnimator = null
            val itemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(getDrawable(R.drawable.divider)!!)
            addItemDecoration(itemDecoration)
        }
    }

    private fun isJoinOrCreate() = intent.getBooleanExtra(KEY_OPEN_JOIN_OR_CREATE, false)

    private fun setGroupSettingsScreen(group: DatabaseGroup?) {
        binding.tbGroupSetup.title = getString(R.string.group_settings)

        _screenItems = mutableListOf(
            UpdateGroupImageItem(groupId = group?.id, imageUrl = group?.photoURL),
            RenameGroupItem(name = group?.name),
            LeaveGroupItem(),
            ShareLinkItem(),
            MuteGroupItem(false)
        )
        groupSetupAdapter?.setData(_screenItems)
        viewModel.initMuteGroupItem()
    }

    private fun setJoinOrCreateScreen() {
        _screenItems = mutableListOf(JoinGroupItem(), CreateGroupItem())
        groupSetupAdapter?.setData(_screenItems)
    }

    override fun onCreateGroupNameChanged(name: String) {
        viewModel.onCreateGroupNameChanged(name)
    }

    override fun onCreateGroupAction(action: CreateGroupState) {
        viewModel.onCreateGroupAction(action)
    }

    override fun onRenameGroupAction(state: RenameGroupState) {
        viewModel.onRenameGroupAction(state)
    }

    override fun onLeaveGroupAction() {
        leaveGroupConfirmationDialog()
    }

    override fun onJoinGroupAction() {
        viewModel.onJoinGroupAction()
    }

    override fun onShareLinkClicked() {
        viewModel.onShareLinkClicked()
    }

    override fun onMuteGroupClicked(groupItem: MuteGroupItem) {
        viewModel.onMuteGroupClicked(groupItem)
    }

    override fun onJoinGroupLinkChanged(link: String) {
        viewModel.onJoinGroupLinkChanged(link)
    }

    private fun initListeners() {
        viewModel.createGroupStateLiveData.observe(this) {
            handleCreateGroupState(it)
        }

        viewModel.renameGroupStateLiveData.observe(this) { state ->
            if (state is RenameGroupSaveAction && state.validation == GroupNameValidationState.VALID) {
                Toast.makeText(this, getString(R.string.update_name_successful), Toast.LENGTH_SHORT).show()
                groupSetupAdapter?.onRenameGroupAction(state)
            } else if (state is RenameGroupOfflineSaveAction && state.validation == GroupNameValidationState.VALID) {
                Toast.makeText(this, getString(R.string.update_name_offline), Toast.LENGTH_SHORT).show()
                groupSetupAdapter?.onRenameGroupAction(RenameGroupSaveAction(state.validation, state.name))
                workManager.startUpdateGroupNameOfflineWorker(state.groupId, state.name)
            } else if (state is RenameGroupFailure) {
                handleRenameGroupError(state.error)
            } else {
                groupSetupAdapter?.onRenameGroupAction(state)
            }
        }

        viewModel.leaveGroupLiveData.observe(this) {
            when (it) {
                is LeaveGroupAction -> {
                    Toast.makeText(this, R.string.leave_group_success_message, Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                is LeaveGroupOffline -> {
                    Toast.makeText(this, R.string.leave_group_offline_message, Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                is LeaveGroupFailure -> {
                    handleLeaveGroupError(it.error)
                }
            }
        }

        viewModel.joinGroupStateLiveData.observe(this) {
            handleJoinGroupState(it)
        }

        viewModel.shareInvitationLinkLiveData.observe(this) { link ->
            link?.let { setupShareLink(it) }
        }

        viewModel.updateGroupImageStateLiveData.observe(this) { groupState ->
            when (groupState) {
                is GroupSuccess -> {
                    updateGroupImage(groupState.group.photoURL)
                    tempImagePath?.let {
                        groupImageOfflineUpdateHelper.removeTempImageFile(it)
                    }
                    Toast.makeText(this, getString(R.string.image_upload_success), Toast.LENGTH_SHORT).show()
                }
                is GroupImageUpdateOfflineURI -> {
                    workManager.startUpdateGroupImageOfflineWorker(groupState.groupId, groupState.uri)
                    Toast.makeText(this, getString(R.string.update_image_offline_message), Toast.LENGTH_SHORT).show()
                }
                is GroupFailure -> {
                    tempImagePath?.let {
                        groupImageOfflineUpdateHelper.removeTempImageFile(it)
                    }
                    Toast.makeText(this, getString(R.string.image_upload_fail), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.updateGroupImageLoadingStateLiveData.observe(this) { isLoading ->
            binding.groupSetupProgressFl.isVisible = isLoading
        }

        viewModel.updateGroupMuteButtonStateLiveData.observe(this) {
            when (it) {
                is MuteGroupSuccess -> {
                    groupSetupAdapter?.onMuteGroupActionChanged(it.isMuted)
                }
                is MuteGroupFailure -> {
                    handleMuteGroupError(it.error)
                }
            }
        }
    }

    private fun handleMuteGroupError(error: MuteGroupError) {
        when (error) {
            MuteGroupError.MUTE_GROUP_FAILED -> Toast.makeText(this, R.string.leave_group_failure_message, Toast.LENGTH_SHORT).show()
            MuteGroupError.NETWORK_ERROR -> Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLeaveGroupError(error: LeaveGroupError) {
        when (error) {
            LeaveGroupError.LEAVE_GROUP_FAILED -> Toast.makeText(this, R.string.leave_group_failure_message, Toast.LENGTH_SHORT).show()
            LeaveGroupError.NETWORK_ERROR -> Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRenameGroupError(error: RenameGroupError) {
        when (error) {
            RenameGroupError.NETWORK_ERROR -> Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
            RenameGroupError.RENAME_GROUP_FAILED -> Toast.makeText(this, getString(R.string.rename_group_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCreateGroupState(state: ICreateGroupState?) {
        when (state) {
            is CreateGroupCopyLinkAction -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Link", state.link)
                clipboard.setPrimaryClip(clip)
                SuccessMessageDialog.show(this, getString(R.string.copy_invitation_link), getString(R.string.lets_invite_others))
            }
            is CreateGroupDoneAction -> onBackPressed()
            is CreateGroupEditAction -> groupSetupAdapter?.onCreateGroupActionChanged(state)
            is CreateGroupSaveAction -> groupSetupAdapter?.onCreateGroupActionChanged(state)
            is CreateGroupConfirmationAction -> {
                createAnotherGroupConfirmationDialog()
            }
            is CreateGroupFailureState -> {
                handleCreateGroupError(state.error)
            }
        }
    }

    private fun handleCreateGroupError(error: CreateGroupActionError) {
        when (error) {
            CreateGroupActionError.CREATE_GROUP_FAILED -> {
                Toast.makeText(this, getString(R.string.creating_group_fail), Toast.LENGTH_SHORT).show()
            }
            CreateGroupActionError.GENERATE_LINK_FAILED -> {
                Toast.makeText(this, getString(R.string.generating_group_fail), Toast.LENGTH_SHORT).show()
            }
            CreateGroupActionError.RENAME_GROUP_FAILED -> {
                Toast.makeText(this, getString(R.string.rename_group_fail), Toast.LENGTH_SHORT).show()
            }
            CreateGroupActionError.LEAVE_PREVIOUS_GROUP_FAILED -> {
                Toast.makeText(this, getString(R.string.leave_group_failure_message), Toast.LENGTH_SHORT).show()
            }
            CreateGroupActionError.NETWORK_ERROR -> {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleJoinGroupState(state: JoinGroupState?) {
        when (state) {
            is JoinGroupAction -> {
                Toast.makeText(this, R.string.join_successful, Toast.LENGTH_SHORT).show()
                groupSetupAdapter?.onJoinGroupActionChanged(state)
                onBackPressed()
            }
            is JoinGroupFailure -> {
                groupSetupAdapter?.onJoinGroupActionChanged(state)
            }
            is JoinGroupWithConfirmationAction -> {
                joinAnotherGroupConfirmationDialog(state.name)
                groupSetupAdapter?.onJoinGroupActionChanged(state)
            }
            is JoinByOutsideLinkConfirmedState -> {
                joinAnotherGroupConfirmationDialog(state.name)
            }

        }
    }

    private fun setupShareLink(link: String) {
        val shareIntent = Intent().apply {
            this.action = Intent.ACTION_SEND
            this.putExtra(Intent.EXTRA_TEXT, link)
            this.type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_invitation_link_dialog_message)))
    }

    private fun leaveGroupConfirmationDialog() {
        val message = getString(R.string.leave_dialog_message)
        WarningDialog.show(this, getString(R.string.leave_the_group), message, getString(R.string.leave), true, null) {
            viewModel.onLeaveGroupAction()
        }
    }

    private fun joinAnotherGroupConfirmationDialog(name: String?) {
        val message =
            getString(R.string.join_another_group_dialog_message) + " \"${name}\" " + getString(R.string.join_another_group_dialog_group_word)
        WarningDialog.show(this, getString(R.string.join_another_group_dialog_title), message, getString(R.string.join), false, null) {
            viewModel.joinAnotherGroup()
        }
    }

    private fun createAnotherGroupConfirmationDialog() {
        viewModel.createAnotherGroup()
    }

    override fun onUpdateGroupImageAction() {
        startImageUploading()
    }

    private fun startImageUploading() {
        val cameraIntent = getCameraIntent()
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(pickIntent, getString(R.string.select_source))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent, galleryIntent))

        getImageResult.launch(chooserIntent)
    }

    private fun getCameraIntent(): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
        }
    }

    private val getImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUrlForLoading = provideImageUrlForLoading(result)
            loadImageByUri(imageUrlForLoading)
        } else {
            tempImagePath?.let{
                groupImageOfflineUpdateHelper.removeTempImageFile(it)
            }
            Toast.makeText(this, getString(R.string.image_wasnt_loaded_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun provideImageUrlForLoading(result: ActivityResult): Uri {
        val galleryImageUri = result.data?.data
        //selecting Gallery or Camera Uri for loading group image
        return if (galleryImageUri != null) {
            tempImagePath?.let {
                groupImageOfflineUpdateHelper.removeTempImageFile(it) //removing temp file
            }
            galleryImageUri
        } else {
            val cameraImageFile = File(tempImagePath)
            val cameraImageUri = Uri.fromFile(cameraImageFile)
            cameraImageUri
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            .apply {
                tempImagePath = absolutePath
            }
    }

    private fun loadImageByUri(imageUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .override(300, 300)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    setGroupImage(resource, imageUri)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun setGroupImage(resource: Bitmap, uri: Uri) {
        lifecycleScope.launch {
            val byteArray = convertToByteArray(resource)
            viewModel.updateGroupImage(byteArray, uri)
        }
    }

    private suspend fun convertToByteArray(resource: Bitmap): ByteArray {
        val result = lifecycleScope.async(Dispatchers.IO) {
            val bytes = ByteArrayOutputStream()
            resource.compress(Bitmap.CompressFormat.JPEG, 70, bytes)
            bytes.toByteArray()
        }.await()
        return result
    }

    private fun updateGroupImage(imageUrl: String?) {
        groupSetupAdapter?.setGroupImageUrl(imageUrl)
    }

}