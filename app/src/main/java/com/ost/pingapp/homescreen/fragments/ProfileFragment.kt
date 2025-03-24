package com.khrd.pingapp.homescreen.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants.ALPHA_MULTIPLIER
import com.khrd.pingapp.constants.LocaleConstants
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailActivity
import com.khrd.pingapp.homescreen.changePassword.ChangePasswordActivity
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountActivity
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.logout.LogoutNetworkFailure
import com.khrd.pingapp.registration.verification.logout.LogoutSuccess
import com.khrd.pingapp.registration.verification.logout.LogoutUnknownFailure
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.WarningDialog
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.workmanager.PingAppWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var pingAppWorkManager: PingAppWorkManager

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    lateinit var imageLoader: ImageLoader

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        viewModel.getCurrentUser()
        initListeners()
        return ComposeView(requireContext()).apply {
            setContent {
                ShowProfileScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @ExperimentalComposeUiApi
    @Composable
    fun ShowProfileScreen() {
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
        )
        Surface(color = colorResource(id = R.color.intouch_background), modifier = Modifier.fillMaxHeight()) {
            ProfileContainerUI(bottomSheetScaffoldState = bottomSheetScaffoldState)
        }
    }

    @Composable
    private fun SetToolbar() {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.profile).uppercase(),
                    fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                    color = colorResource(R.color.intouch_primary_02),
                    fontSize = 10.sp,
                )
            },
            backgroundColor = colorResource(id = R.color.intouch_neutral_03),
            contentColor = colorResource(id = R.color.intouch_primary_01),
            elevation = 0.dp,
            actions = {
                IntouchTextButton(textButton = stringResource(id = R.string.delete_my_account)) {
                    toDeleteAccountActivity()
                }
            },
        )

    }

    @Composable
    private fun SetUploadChangePhoto(user: DatabaseUser) {
        requireActivity().findViewById<Button>(R.id.sendPing).visibility = View.GONE
        val updateState by viewModel.updateProfilePhotoStateLiveData.observeAsState()
        val deleteProfilePhotoState by viewModel.deleteProfilePhotoStateLiveData.observeAsState()
        var photoUrl by remember { mutableStateOf(user.photoURL) }
        val coroutineScope = rememberCoroutineScope()
        var isImagePressed by remember { mutableStateOf(false) }
        val imageScale by animateFloatAsState(
            if (isImagePressed) 1.1f else 1f,
            animationSpec = tween(
                durationMillis = 200
            )
        )
        updateState?.getContentIfNotHandled()?.let { state ->
            when (state) {
                is UpdateProfilePhotoSuccess -> {
                    photoUrl = state.urlPhoto
                    toastUtils.showShortToast(getString(R.string.image_upload_success))

                }

                is UpdateProfilePhotoFail -> {
                    toastUtils.showShortToast(getString(R.string.image_upload_fail))
                }
            }
        }
        deleteProfilePhotoState?.getContentIfNotHandled()?.let { state ->
            when (state) {
                is DeleteProfilePhotoStateSuccess -> {
                    photoUrl = null
                    toastUtils.showShortToast(getString(R.string.photo_delete_success))
                }

                is DeleteProfilePhotoStateFail -> {
                    toastUtils.showShortToast(getString(R.string.photo_delete_fail))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (!photoUrl.isNullOrEmpty()) {
                    rememberAsyncImagePainter(photoUrl)
                } else {
                    painterResource(R.drawable.ic_default_user_avatar)
                },
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .scale(imageScale)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        photoUrl?.let {
                            var job: Job?
                            var isDialogOpened = false
                            forEachGesture {
                                awaitPointerEventScope {

                                    awaitFirstDown()
                                    // ACTION_DOWN
                                    job = coroutineScope.launch(Dispatchers.Default) {
                                        isImagePressed = true
                                        delay(200)
                                        withContext(Dispatchers.Main) {
                                            val action = HomescreenNavGraphDirections.showZoomedUserIconDialog(photoURL = it)
                                            findNavController().navigateSafe(action)
                                            isDialogOpened = true
                                        }
                                    }

                                    while (awaitPointerEvent().changes.all { it.pressed }) {
                                        //Do nothing and wait for ACTION_UP
                                    }

                                    // ACTION_UP
                                    job?.cancel()
                                    isImagePressed = false
                                    if (isDialogOpened) {
                                        findNavController().popBackStack()
                                        isDialogOpened = false
                                    }
                                }
                            }
                        }
                    }
            )
            Image(
                painter = if (!photoUrl.isNullOrEmpty()) {
                    //show default
                    painterResource(id = R.drawable.ic_delete)
                } else {
                    //show disabled
                    painterResource(id = R.drawable.ic_delete_disabled)
                },
                contentDescription = "delete profile photo",
                contentScale = ContentScale.Crop,
                modifier = if (!photoUrl.isNullOrEmpty()) {
                    Modifier
                        .padding(start = 13.dp)
                        .clickable { showDeleteProfilePhotoDialog() }
                } else {
                    Modifier
                        .padding(start = 13.dp)
                }
            )
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                IntouchTextButton(textButton = stringResource(id = R.string.upload_change_photo)) {
                    startUploadingPhoto()
                }
            }
        }
        Divider(color = colorResource(R.color.intouch_neutral_03))
    }

    @ExperimentalComposeUiApi
    @Composable
    private fun SetUsernameField(user: DatabaseUser) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        var focus by remember { mutableStateOf(false) }
        var isError by rememberSaveable { mutableStateOf(false) }
        var text by rememberSaveable { mutableStateOf(user.username ?: "") }
        var textError by rememberSaveable { mutableStateOf("") }
        val editNameState by viewModel.editNameStateLiveData.observeAsState()
        editNameState?.getContentIfNotHandled()?.let { state ->
            when (state) {
                is EditNameSuccess -> {
                    isError = false
                    focusManager.clearFocus()
                    textError = ""
                    toastUtils.showShortToast(getString(R.string.user_name_updated))
                }

                is NameSaveState -> {
                    when (state.error) {
                        UserNameValidationState.TOO_LONG -> {
                            isError = true
                            textError = stringResource(R.string.input_is_more_than_max)
                        }

                        UserNameValidationState.EMPTY_FIELD -> {
                            isError = true
                            textError = stringResource(R.string.empty_field)
                        }

                        UserNameValidationState.NAME_LETTERS_FAILURE -> {
                            isError = true
                            textError = stringResource(R.string.name_validation_message)
                        }

                        UserNameValidationState.VALID -> {
                            isError = false
                            textError = ""
                        }
                    }
                }

                is EditNameFailure -> {
                    toastUtils.showToast(getString(R.string.unknown_error))
                }

                is EditNameOffline -> {
                    updateOfflineUsername(state.name)
                    toastUtils.showShortToast(getString(R.string.change_name_offline_message))
                    focusManager.clearFocus()
                    isError = false
                    textError = ""
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 24.dp)
        ) {
            Column(Modifier.weight(1f)) {
                TextField(
                    value = text,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.editUserName(text.trim())
                        }),
                    onValueChange = {
                        text = it
                        isError = false
                        textError = ""
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.fullname),
                            color = colorResource(R.color.intouch_neutral_01),
                            fontSize = 10.sp,
                            fontFamily = FontFamily(Font(R.font.poppins)),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = colorResource(R.color.intouch_background),
                        focusedIndicatorColor = colorResource(R.color.intouch_primary_01),
                        textColor = colorResource(R.color.intouch_text),
                        errorIndicatorColor = colorResource(R.color.intouch_error),
                        disabledTextColor = colorResource(R.color.intouch_text),
                        unfocusedIndicatorColor = colorResource(R.color.intouch_neutral_02),
                        disabledIndicatorColor = colorResource(R.color.intouch_neutral_02),
                    ),
                    isError = isError,
                    textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.poppins)), fontSize = 12.sp, textAlign = TextAlign.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            focus = it.isFocused
                        }
                )
                if (isError) {
                    SetupErrorText(textError)
                }
            }
            if (focus) {
                IntouchTextButton(textButton = stringResource(id = R.string.save)) {
                    viewModel.editUserName(text.trim())
                }
            }
        }
    }

    @Composable
    private fun SetupErrorText(error: String) {
        Text(
            text = error,
            color = colorResource(R.color.intouch_error),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
        )
    }

    @Composable
    private fun IntouchTextButton(textButton: String, modifier: Modifier? = null, function: () -> Unit) {
        TextButton(onClick = {
            function.invoke()
        }, modifier = modifier ?: Modifier) {
            Text(
                text = textButton.uppercase(),
                fontFamily = FontFamily(Font(R.font.poppins_medium)),
                color = colorResource(R.color.intouch_primary_01),
                modifier = Modifier.padding(12.dp, 7.5.dp),
                fontSize = 14.sp,
                maxLines = 1,
            )
        }
    }

    @ExperimentalComposeUiApi
    @Composable
    private fun SetJobPositionField(user: DatabaseUser) {
        val focusRequester = remember { FocusRequester() }
        var focus by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        var isError by rememberSaveable { mutableStateOf(false) }
        var text by rememberSaveable { mutableStateOf(user.job ?: "") }
        var textError by rememberSaveable { mutableStateOf("") }
        val editJobState by viewModel.editJobLiveData.observeAsState()

        editJobState?.getContentIfNotHandled()?.let { state ->
            when (state) {
                is JobPositionSaveState -> {
                    when (state.result) {
                        is JobPositionSaveSuccessState -> {
                            toastUtils.showShortToast(getString(R.string.job_position_updated))
                            focusManager.clearFocus()
                            focus = false
                            isError = false
                        }

                        is JobPositionSaveFailureState -> {
                            when (state.result.validationState) {
                                JobNameValidationState.VALID -> {
                                    isError = false
                                    focus = false
                                    textError = ""
                                }

                                JobNameValidationState.TOO_LONG -> {
                                    isError = true
                                    focus = true
                                    textError = stringResource(R.string.input_is_more_than_max)

                                }

                                JobNameValidationState.INVALID_CHARS -> {
                                    focus = true
                                    isError = true
                                    textError = stringResource(R.string.job_position_invalid_chars)
                                }
                            }
                        }

                        is JobPositionFailureState -> {
                            toastUtils.showShortToast(getString(R.string.job_position_fail))
                        }

                        is JobPositionOfflineUpdateState -> {
                            updateOfflineJobPositionWithWorker(state.result.name)
                            focusManager.clearFocus()
                            focus = false
                            toastUtils.showShortToast(getString(R.string.job_position_online_update))
                            isError = false
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 18.dp),
        )
        {
            Column(Modifier.weight(1F)) {
                TextField(
                    value = text,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.editJobPosition(text.trim()) }),
                    onValueChange = {
                        text = it.trimStart()
                        isError = false
                        textError = ""
                    },
                    label = {
                        Text(
                            color = colorResource(R.color.intouch_neutral_01),
                            fontSize = 10.sp,
                            fontFamily = FontFamily(Font(R.font.poppins)),
                            text = stringResource(R.string.job_position)
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = colorResource(R.color.intouch_background),
                        focusedIndicatorColor = colorResource(R.color.intouch_primary_01),
                        textColor = colorResource(R.color.intouch_text),
                        errorIndicatorColor = colorResource(R.color.intouch_error),
                        disabledTextColor = colorResource(R.color.intouch_text),
                        unfocusedIndicatorColor = colorResource(R.color.intouch_neutral_02),
                        disabledIndicatorColor = colorResource(R.color.intouch_neutral_02),
                    ),
                    isError = isError,
                    textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.poppins)), fontSize = 12.sp, textAlign = TextAlign.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            focus = it.isFocused
                        }
                )
                if (isError) {
                    SetupErrorText(textError)
                }
            }
            if (focus) {
                IntouchTextButton(textButton = stringResource(id = R.string.save)) {
                    viewModel.editJobPosition(text.trim())
                }
            }
        }
    }

    @Composable
    private fun SetUserEmail(user: DatabaseUser) {
        Column(Modifier.padding(top = 16.dp)) {
            Divider(color = colorResource(R.color.intouch_neutral_03))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    user.email ?: "",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 21.5.dp, bottom = 21.5.dp),
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    color = colorResource(R.color.intouch_text)
                )
                IntouchTextButton(textButton = stringResource(id = R.string.change), Modifier.align(Alignment.CenterVertically)) {
                    val intent = Intent(activity, ChangeEmailActivity::class.java)
                    startActivity(intent)
                }
            }
            Divider(color = colorResource(R.color.intouch_neutral_03))
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun SetMenuLanguage(bottomSheetScaffoldState: BottomSheetScaffoldState, coroutineScope: CoroutineScope) {
        Text(
            text = stringResource(R.string.interface_language),
            color = colorResource(R.color.intouch_neutral_01),
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            modifier = Modifier
                .padding(top = 7.5.dp, start = 16.dp)
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = colorResource(id = R.color.intouch_neutral_01)),
            ) {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }
            .padding(top = 9.5.dp, bottom = 4.dp)) {
            Column(
                modifier = Modifier.weight(1F)
            ) {
                Row {
                    Image(
                        painter = painterResource(R.drawable.ic_flag),
                        contentDescription = stringResource(id = R.string.currentLocale),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.currentLocale),
                        color = colorResource(id = R.color.intouch_text),
                        fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.poppins)),
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )

                }
                Divider(
                    color = colorResource(id = R.color.intouch_neutral_02), modifier = Modifier.padding(top = 9.5.dp, end = 8.dp)
                )
            }
            Image(
                painter = painterResource(R.drawable.arrow_down_profile_fragment),
                contentDescription = stringResource(id = R.string.arrow_down_icon_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(all = 8.dp),
                alignment = Alignment.Center
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ProfileContainerUI(
        bottomSheetScaffoldState: BottomSheetScaffoldState
    ) {
        val user by viewModel.currentUserLiveData.observeAsState()
        val coroutineScope = rememberCoroutineScope()
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            content = {
                Box {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        SetToolbar()
                        user?.let {
                            SetUploadChangePhoto(it)
                            SetUsernameField(it)
                            SetJobPositionField(it)
                            SetUserEmail(it)

                        }
                        SetMenuLanguage(bottomSheetScaffoldState, coroutineScope)

                        SetChangePassword(true)
                        SetLogOut(true)
                        user?.let {
                            SetHideUserInformation(it)
                        }
                    }
                    BottomSheetOverlay(bottomSheetScaffoldState, coroutineScope)
                }
            },
            backgroundColor = colorResource(id = R.color.intouch_background),
            sheetContent =
            {
                val items by viewModel.languagesLiveData.observeAsState()
                items?.let { languages ->
                    LazyColumn(modifier = Modifier.background(colorResource(id = R.color.intouch_background))) {
                        items(languages) { language ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = rememberRipple(color = colorResource(id = R.color.intouch_neutral_01)),
                                    ) {
                                        updateLocale(language.locale)
                                    }
                                    .padding(all = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(language.imageRes),
                                    contentDescription = getString(language.languageName),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(36.dp)
                                )
                                Text(
                                    text = getString(language.languageName),
                                    color = colorResource(id = R.color.intouch_text),
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.poppins)),
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f)
                                )
                                if (language.isSelected) {
                                    Text(
                                        text = stringResource(id = R.string.locale_selected).uppercase(),
                                        color = colorResource(id = R.color.intouch_text),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(Font(R.font.poppins)),
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            sheetPeekHeight = 0.dp,
        )
    }

    @Composable
    private fun SetHideUserInformation(user: DatabaseUser) {

        val hideUserInfoState by viewModel.hideUserInfoLiveData.observeAsState()

        hideUserInfoState?.getContentIfNotHandled()?.let { state ->
            when (state) {
                is HideUserInfoSuccess -> {
                    //TODO add toast
                }

                is HideUserInfoFailure -> {
                    toastUtils.showShortToast(getString(R.string.hide_user_info_fail))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, start = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.hide_online_status_and_group_list),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 21.5.dp, bottom = 21.5.dp),
                fontFamily = FontFamily(Font(R.font.poppins)),
                color = colorResource(R.color.intouch_text)
            )
            Switch(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = user.hideInfo,
                onCheckedChange = { viewModel.onHideClicked(it) })
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun BottomSheetOverlay(
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        coroutineScope: CoroutineScope
    ) {
        val tapModifier = if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = { coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() } })
            }
        } else Modifier
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Black.copy(
                        alpha = backgroundAlphaFun(bottomSheetScaffoldState.bottomSheetState) * ALPHA_MULTIPLIER
                    )
                )
                .then(tapModifier)
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun backgroundAlphaFun(bottomSheetScaffoldState: BottomSheetState): Float {
        if (bottomSheetScaffoldState.progress.to == BottomSheetValue.Expanded) {
            return bottomSheetScaffoldState.progress.fraction
        } else {
            return 1 - bottomSheetScaffoldState.progress.fraction
        }
    }

    private fun updateLocale(locale: String) {
        viewModel.updateLocale(locale)
        val intent = Intent(requireContext(), HomeScreen::class.java)
        intent.putExtra(LocaleConstants.OPEN_PROFILE_INTENT_KEY, true)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    @Composable
    private fun SetChangePassword(fillMaxWidth: Boolean) {
        if (fillMaxWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IntouchTextButton(textButton = stringResource(R.string.change_password)) {
                    viewModel.onActionChanged(ChangePasswordAction())
                }
            }
        } else {
            IntouchTextButton(textButton = stringResource(R.string.change_password)) {
                viewModel.onActionChanged(ChangePasswordAction())
            }
        }
    }

    @Composable
    private fun ColumnScope.SetLogOut(fillMaxWidth: Boolean) {
        if (fillMaxWidth) {
            IntouchTextButton(modifier = Modifier.align(CenterHorizontally), textButton = stringResource(R.string.logout)) {
                showLogOutDialog()
            }
        } else {
            IntouchTextButton(textButton = stringResource(R.string.logout)) {
                showLogOutDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
    }

    private fun initListeners() {
        viewModel.profileStateLiveData.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is ToChangePasswordActivity -> {
                        toChangePasswordActivity()
                    }

                    is ToDeleteAccount -> {
                        toDeleteAccountActivity()
                    }
                }
            }
        }
        viewModel.logoutStateLiveData.observe(viewLifecycleOwner) {
            handleLogoutState(it)
        }
    }

    private fun showDeleteProfilePhotoDialog() {
        context?.let { context ->
            WarningDialog.show(
                context = context,
                title = getString(R.string.delete_photo),
                message = getString(R.string.are_you_sure_you_want_to_delete_photo),
                confirmButtonText = getString(R.string.delete),
                onConfirmClickListener = { viewModel.deleteProfilePhoto() }
            )
        }
    }

    private fun toChangePasswordActivity() {
        val intent = Intent(activity, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun toDeleteAccountActivity() {
        val intent = Intent(activity, DeleteAccountActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogoutState(it: ILogoutState?) {
        when (it) {
            is LogoutSuccess -> {

                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            is LogoutNetworkFailure -> {
                toastUtils.showToast(getString(R.string.no_internet_connection))
            }

            is LogoutUnknownFailure -> {
                toastUtils.showToast(getString(R.string.unknown_error))
            }

            null -> {
            }
        }
    }

    private fun showLogOutDialog() {
        context?.let { context ->
            WarningDialog.show(
                context = context,
                title = getString(R.string.logout),
                message = getString(R.string.to_log_back_in),
                confirmButtonText = getString(R.string.logout),
                onConfirmClickListener = { viewModel.logout() }
            )
        }
    }

    private val getImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.extras?.get("data") != null) {                // FROM CAMERA
                compressBitmapToJPEG(data.extras!!.get("data") as Bitmap) { bitmap, byteArray ->
                    viewModel.updateProfilePhoto(byteArray)
                }
            } else if (data?.data != null) {                         // FROM GALLERY
                Glide.with(this)
                    .asBitmap()
                    .load(data.data)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            compressBitmapToJPEG(resource) { bitmap, byteArray ->
                                viewModel.updateProfilePhoto(byteArray)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }

    private fun startUploadingPhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent.type = "image/*"
        val chooserIntent = Intent.createChooser(pickIntent, getString(R.string.select_source))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent, galleryIntent))
        getImageResult.launch(chooserIntent)
    }

    private fun compressBitmapToJPEG(source: Bitmap, callback: (Bitmap, ByteArray) -> Unit) {
        val bytes = ByteArrayOutputStream()
        var bitmap: Bitmap

        Glide.with(this)
            .asBitmap()
            .load(source)
            .override(300, 300)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    callback(resource, bytes.toByteArray())
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun updateOfflineUsername(username: String) {
        pingAppWorkManager.startEditUserNameWorker(username)
    }

    private fun updateOfflineJobPositionWithWorker(jobPosition: String) {
        pingAppWorkManager.startEditJobPositionWorker(jobPosition)
    }
}
