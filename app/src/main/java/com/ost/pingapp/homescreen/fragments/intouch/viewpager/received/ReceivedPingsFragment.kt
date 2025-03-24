package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.received

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.R
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsFailure
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsSuccess
import com.khrd.pingapp.utils.FontSize
import com.khrd.pingapp.utils.LocalFontSize
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.getRelativeDate
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.khrd.pingapp.utils.viewstate.ViewState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReceivedPingsFragment : Fragment() {

    private val viewModel: ReceivedPingsViewModel by viewModels()

    @Inject
    lateinit var appViewState: AppViewState

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PreviewListOfReceivers(viewModel)
            }
        }
    }

    @Composable
    fun EmptyPingsScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(id = R.drawable.ic_group_placeholder), contentDescription = null)
            TextViewEmptyMessage()
        }
    }

    @Composable
    fun TextViewEmptyMessage() {
        CompositionLocalProvider(LocalFontSize provides FontSize()) {
            Text(
                text = stringResource(id = R.string.no_received_pings),
                color = colorResource(id = R.color.intouch_text),
                fontStyle = FontStyle(R.font.poppins_semibold),
                fontSize = LocalFontSize.current.small,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 19.dp),
            )
        }
    }

    @Composable
    fun ReceiversListItem(receiver: ReceivedPingItem, modifier: Modifier) {
        val borderColorSeenStatus = if (receiver.seen) R.color.intouch_neutral_02 else R.color.intouch_primary_02
        val backgroundColorSeenStatus = if (receiver.seen) R.color.intouch_background else R.color.intouch_primary_03
        Column(
            modifier = Modifier
                .border(
                    border = BorderStroke(2.dp, color = colorResource(id = borderColorSeenStatus)),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                )
                .fillMaxWidth()
                .background(colorResource(id = backgroundColorSeenStatus))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    if (receiver.userItem?.isDeleted == true) {
                        Modifier
                    } else {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(color = colorResource(id = R.color.transparent)),
                        ) {
                            receiver.userItem?.let {
                                val action = receiver.groupFrom?.let { dataBaseGroup ->
                                    HomescreenNavGraphDirections.showUserStatusDialog(
                                        userItem = it,
                                        dataBaseGroup
                                    )
                                }
                                action?.let { navDirections -> findNavController().navigateSafe(navDirections) }
                            }
                        }
                    }) {
                    Image(
                        painter = if (receiver.userItem?.photoURL.isNullOrEmpty()) {
                            painterResource(R.drawable.ic_default_user_avatar)
                        } else rememberAsyncImagePainter(receiver.userItem?.photoURL),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 2.dp, top = 2.dp)
                            .size(width = 36.dp, height = 36.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterStart)
                    )
                    if (receiver.userItem?.isOnline?.status == true && !receiver.userItem.isHide) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_online),
                            contentDescription = "",
                            modifier = Modifier
                                .size(width = 10.dp, height = 10.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provideUserName(receiver),
                        fontSize = LocalFontSize.current.large,
                        modifier = Modifier.padding(bottom = 3.dp),
                        color = colorResource(id = R.color.intouch_text),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = provideGroupName(receiver),
                        fontStyle = FontStyle(R.font.poppins),
                        fontSize = LocalFontSize.current.small,
                        color = colorResource(id = R.color.intouch_text),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = receiver.emoji,
                    fontSize = LocalFontSize.current.extraLarge,
                    textAlign = TextAlign.End,
                    color = colorResource(id = R.color.black)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            SetVisibleGroup(receiver)
        }
    }

    @Composable
    private fun provideGroupName(receiver: ReceivedPingItem): String {
        return if (receiver.groupFrom?.name.isNullOrBlank()) {
            resources.getString(R.string.deleted_group)
        } else {
            receiver.groupFrom?.name.toString()
        }
    }

    @Composable
    private fun provideUserName(receiver: ReceivedPingItem): String {
        return if (receiver.userItem?.isDeleted == true) {
            resources.getString(R.string.deleted_user)
        } else {
            receiver.userItem?.fullname.toString()
        }
    }

    @Composable
    fun SetVisibleGroup(receiver: ReceivedPingItem) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = getRelativeDate(receiver.date),
                fontStyle = FontStyle(R.font.poppins),
                fontSize = LocalFontSize.current.small,
                color = colorResource(id = R.color.intouch_text),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            if (receiver.isGroupPing) {
                Text(
                    text = stringResource(R.string.group_ping),
                    fontStyle = FontStyle(R.font.poppins),
                    modifier = Modifier.weight(1f),
                    fontSize = LocalFontSize.current.extraSmall,
                    color = colorResource(id = R.color.intouch_text),
                    textAlign = TextAlign.End,
                )
            } else ""
        }
    }

    override fun onPause() {
        super.onPause()
        appViewState.viewState = ViewState.DefaultViewState
    }

    @Composable
    fun PreviewListOfReceivers(viewmodel: ReceivedPingsViewModel) {

        val receiversState by viewmodel.receivedPingsLiveData.observeAsState()

        when (receiversState) {
            is LoadReceivedPingsSuccess -> {
                ListOfReceivers((receiversState as LoadReceivedPingsSuccess).pings)
            }

            is LoadReceivedPingsFailure -> {
                ListOfReceivers((listOf()))
                Toast.makeText(context, getString(R.string.load_received_pings_failure), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun LazyListState.isScrolledToTheEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

    @Composable
    fun ListOfReceivers(
        receivers: List<ReceivedPingItem>,
    ) {
        if (receivers.isNotEmpty()) {
            val listState = rememberLazyListState()

            val isScrolledToTheEnd by remember { derivedStateOf { listState.isScrolledToTheEnd() } }

            // LazyColumn -> equivalent of the RecyclerView
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.background(color = colorResource(id = R.color.intouch_background))
            ) {
                items(receivers) { receiverItem ->
                    ReceiversListItem(
                        receiver = receiverItem,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (isScrolledToTheEnd) {
                viewModel.onRecyclerViewScrolledToLast()
            }

            handleRecyclerScroll(listState, receivers)

        } else {
            EmptyPingsScreen()
        }
    }

    @Composable
    private fun handleRecyclerScroll(
        listState: LazyListState,
        receivers: List<ReceivedPingItem>
    ) {
        // derivedStateOf - prevents infinite recomposition
        val visibleItemsCount by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.size } }
        val firstVisibleItemIndex = listState.firstVisibleItemIndex
        val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?: -1

        // AppViewState
        if (firstVisibleItemIndex == 0) {
            appViewState.viewState = ViewState.ReceivedPingsViewState(firstVisibleItemIndex)
        } else {
            appViewState.viewState = ViewState.DefaultViewState
        }

        // Seen status check
        if (firstVisibleItemIndex != -1 && lastVisibleItemIndex != -1) {
            try {
                viewModel.onRecyclerViewScrolledSetViewed(
                    receivers.subList(
                        firstVisibleItemIndex,
                        lastVisibleItemIndex + 1
                    )
                )
            } catch (e: IllegalArgumentException) {
                // do nothing
            }
        }
    }
}