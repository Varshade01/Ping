package com.khrd.pingapp.homescreen.sendping

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.khrd.pingapp.R
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.pings.getResId
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.databinding.BottomSheetLayoutBinding
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.*
import com.khrd.pingapp.homescreen.sendping.SendPingDialogSideEffect.*
import com.khrd.pingapp.utils.*
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.workmanager.PingAppWorkManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.SingleEmojiTrait
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SendPingDialogFragment : DialogFragment() {
    private val viewModel by viewModels<SendPingDialogViewModel>()
    private var binding by Delegates.notNull<BottomSheetLayoutBinding>()
    private val calendar = Calendar.getInstance()
    private var day = calendar.get(Calendar.DAY_OF_MONTH)
    private var month = calendar.get(Calendar.MONTH)
    private var year = calendar.get(Calendar.YEAR)
    private var hour = calendar.get(Calendar.HOUR_OF_DAY)
    private var minute = calendar.get(Calendar.MINUTE)
    private lateinit var emojiKeyboard: EmojiPopup
    private var isScheduledTimePicked = false
    private var groupsAdapter: SendPingGroupsAdapter? = null
    private var receiversAdapter: ReceiversAdapter? = null
    private var recursionAdapter: RecursionAdapter? = null
    private var dismissTimeStamp = System.currentTimeMillis()

    private val args: SendPingDialogFragmentArgs by navArgs()

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    lateinit var pingsAlarmManager: PingsAlarmManager

    @Inject
    lateinit var pingAppWorkManager: PingAppWorkManager

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        initEmojiKeyboard()
        initListeners()
        setOnGroupEndIconClickListener()
        setOnRecursionEndIconClickListener()
        initAdapters()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.isCancelable = false
        viewModel.init(args.group, args.users?.toList(), args.sentToEveryone)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        observeDialogState()
    }

    private fun observeDialogState() {
        viewModel.sendPingDialogStateLiveData.observe(viewLifecycleOwner) { sendPingDialogState ->
            val currentChips = getCurrentChips()
            handleChips(sendPingDialogState.listOfChips, currentChips)

            binding.switchSendToGroup.isChecked = sendPingDialogState.isGroupChecked
            binding.switchSchedulePing.isChecked = sendPingDialogState.isScheduleChecked

            binding.actvFromGroup.setText(sendPingDialogState.groupName)
            binding.actvFromGroup.setSelection(sendPingDialogState.groupName.length)

            binding.etEmoji.setText(sendPingDialogState.emoji)
            binding.btnClearEmoji.visibility = if (sendPingDialogState.emoji.isNotBlank()) View.VISIBLE else View.INVISIBLE
            binding.btnAddEmoji.visibility = if (sendPingDialogState.emoji.isNotBlank()) View.INVISIBLE else View.VISIBLE

            binding.progressFl.isVisible = sendPingDialogState.isLoading
            binding.btnCancel.elevation = if (sendPingDialogState.isLoading) 2.dp() else 0.dp()

            binding.actvRecursion.setText(
                getString(sendPingDialogState.recursion.getResId())
            )

            receiversAdapter?.setData(sendPingDialogState.usersInGroup)
            groupsAdapter?.setData(sendPingDialogState.listOfGroups)
            recursionAdapter?.setData(generateListOfRecursionItems(), sendPingDialogState.recursion)
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
    }

    private fun initAdapters() {
        receiversAdapter = activity?.let { ReceiversAdapter(it, R.layout.receiver_item, arrayOf(), imageLoader) }
        binding.actvReceiver.setAdapter(receiversAdapter)
        groupsAdapter = activity?.let { SendPingGroupsAdapter(it, R.layout.sender_item, arrayOf(), imageLoader) }
        binding.actvFromGroup.setAdapter(groupsAdapter)
        recursionAdapter = activity?.let { RecursionAdapter(it, R.layout.recursion_item, listOf()) }
        binding.actvRecursion.setAdapter(recursionAdapter)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun initListeners() {
        observeSendPingState()
        addSetEmojiListener()
        addDeleteEmojiListener()
        initAdapter()
        addGroupCheckedListener()
        addScheduleCheckedListener()
        addDatePickerClickListener()
        addTimePickerClickListener()
        addCancelClickListener()
        addSendClickListener()
        addGroupArrowDownOnDismissListener()
        addRecursionArrowDownOnDismissListener()
    }

    private fun addSendClickListener() {
        binding.btnSend.setOnClickListener {
            binding.btnSend.isEnabled = false
            viewModel.onSendPingIntent(SendPingIntent)
        }
    }

    private fun addCancelClickListener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun addTimePickerClickListener() {
        binding.btnTime.editText?.setOnClickListener {
            openTimePicker()
        }
    }

    private fun addDatePickerClickListener() {
        binding.btnDate.editText?.setOnClickListener {
            openDatePicker()
        }
    }

    private fun addSetEmojiListener() {
        binding.btnAddEmoji.setOnClickListener {
            if (!emojiKeyboard.isShowing) {
                binding.etEmoji.isEnabled = true
                emojiKeyboard.toggle()
            }
        }
    }

    private fun addScheduleCheckedListener() {
        binding.switchSchedulePing.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                expand(binding.btnDate)
                expand(binding.btnTime)
                expand(binding.btnRecursion)
                if (!isScheduledTimePicked) {
                    setTimeOneMinuteAhead()
                }
                setButtons()
                viewModel.onSendPingIntent(SendPingTimePickedIntent(hour, minute))
                binding.btnSend.text = activity?.getString(R.string.schedule)
            } else {
                collapse(binding.btnDate)
                collapse(binding.btnTime)
                collapse(binding.btnRecursion)
                viewModel.onSendPingIntent(UnschedulePingIntent)
                binding.btnSend.text = activity?.getString(R.string.send)
            }
        }
    }

    private fun addDeleteEmojiListener() {
        binding.btnClearEmoji.setOnClickListener {
            viewModel.onSendPingIntent(SendPingEmojiPickedIntent(""))
        }
    }

    private fun setOnGroupEndIconClickListener() {
        binding.tilFromGroup.setEndIconOnClickListener {
            if (isShowPopup()) {
                groupsAdapter?.showAll()
                binding.actvFromGroup.showDropDown()
                binding.tilFromGroup.setEndIconDrawable(R.drawable.ic_arrow_up_fromgrup)
            }
        }
    }

    private fun isShowPopup() = System.currentTimeMillis() - dismissTimeStamp > 200

    private fun addGroupArrowDownOnDismissListener() {
        binding.actvFromGroup.setOnDismissListener {
            binding.tilFromGroup.setEndIconDrawable(R.drawable.ic_arrow_down_sendping)
            dismissTimeStamp = System.currentTimeMillis()
        }
    }

    private fun setOnRecursionEndIconClickListener() {
        binding.btnRecursion.editText?.setOnClickListener {
            if (isShowPopup()) {
                binding.actvRecursion.showDropDown()
                binding.btnRecursion.setEndIconDrawable(R.drawable.ic_arrow_up_fromgrup)
            }
        }
    }

    private fun addRecursionArrowDownOnDismissListener() {
        binding.actvRecursion.setOnDismissListener {
            binding.btnRecursion.setEndIconDrawable(R.drawable.ic_arrow_down_sendping)
            dismissTimeStamp = System.currentTimeMillis()
        }
    }

    private fun generateListOfRecursionItems() = listOf(
        Pair(RecurringTime.NO_REPEAT, null),
        Pair(
            RecurringTime.DAY, String.format(
                getString(R.string.repeat_daily_details),
                hour,
                formatMinuteWithLeadingZeroes(minute)
            )
        ),
        Pair(
            RecurringTime.WEEK, String.format(
                getString(R.string.repeat_weekly_details),
                getCurrentDay()?.let { dayNameResId ->
                    getString(dayNameResId)
                },
                hour,
                formatMinuteWithLeadingZeroes(minute)
            )
        ),
        Pair(
            RecurringTime.MONTH, String.format(
                getString(R.string.repeat_monthly_details),
                day,
                hour,
                formatMinuteWithLeadingZeroes(minute)
            )
        ),
        Pair(
            RecurringTime.YEAR, String.format(
                getString(R.string.repeat_yearly_details),
                DateFormat.format("MMMM", calendar.time),
                day,
                hour,
                formatMinuteWithLeadingZeroes(minute)
            )
        )
    )

    private fun formatMinuteWithLeadingZeroes(minute: Int): String = String.format("%02d", minute)

    private fun getCurrentDay(): Int? {
        val daysOfWeek = mapOf(
            Calendar.SUNDAY to R.string.every_sunday,
            Calendar.MONDAY to R.string.every_monday,
            Calendar.TUESDAY to R.string.every_tuesday,
            Calendar.WEDNESDAY to R.string.every_wednesday,
            Calendar.THURSDAY to R.string.every_thursday,
            Calendar.FRIDAY to R.string.every_friday,
            Calendar.SATURDAY to R.string.every_saturday
        )

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return daysOfWeek[currentDayOfWeek]
    }


    private fun addGroupCheckedListener() {
        binding.switchSendToGroup.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.actvReceiver.isEnabled = !isChecked
            binding.tilReceiver.isEnabled = !isChecked
            viewModel.onSendPingIntent(SendPingReceiverAsGroupIntent(isChecked))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAdapter() {
        binding.actvReceiver.threshold = 1
        binding.actvFromGroup.threshold = 1

        binding.actvReceiver.setOnItemClickListener { parent, view, position, id ->
            val currentItem = parent.adapter.getItem(position) as DatabaseUser
            currentItem.id?.let {
                viewModel.onSendPingIntent(SendPingReceiverAddedIntent(listOf(it)))
            }
            binding.tilReceiver.error = null
        }
        binding.actvReceiver.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tvInvisible.text = p0
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.actvFromGroup.setOnItemClickListener { parent, view, position, id ->
            val currentItem = parent.adapter.getItem(position) as DatabaseGroup
            viewModel.onSendPingIntent(GroupChangedIntent(currentItem.id))
            binding.actvReceiver.requestFocus()
        }
        binding.actvReceiver.setOnTouchListener { _, _ ->
            if (binding.actvReceiver.adapter.count != 0) {
                binding.actvReceiver.showDropDown()
            }
            false
        }
        binding.actvRecursion.setOnItemClickListener { adapterView, _, position, _ ->
            val currentItem = adapterView.adapter.getItem(position) as Pair<RecurringTime, String?>
            binding.actvRecursion.setText(currentItem.first.getResId())
            viewModel.onSendPingIntent(SendPingRecurringTimePickedIntent(currentItem.first))
        }
    }

    private fun observeSendPingState() {
        viewModel.sendPingDialogSideEffectLiveData.observe(viewLifecycleOwner) { newEvent ->
            newEvent.getContentIfNotHandled()?.let { sendPingEffect ->
                when (sendPingEffect) {
                    is SendPingFailureEffect -> {
                        binding.btnSend.isEnabled = true
                        when (sendPingEffect.error) {
                            SendPingError.NO_RECEIVER -> binding.tilReceiver.error = getString(R.string.no_receiver_selected)
                            SendPingError.PING_CREATION_FAILURE -> toastUtils.showShortToast(R.string.ping_creation_failed)
                            SendPingError.SCHEDULED_FOR_PAST -> toastUtils.showShortToast(R.string.scheduled_for_past_time_error)
                        }
                    }

                    is SendPingSuccessEffect -> {
                        toastUtils.showToast(R.string.ping_sent_message)
                        dismiss()
                    }

                    is SchedulePingEffect -> {
                        pingsAlarmManager.setAlarm(
                            requireActivity().applicationContext,
                            sendPingEffect.scheduledTime,
                            sendPingEffect.pingId
                        )
                        if (sendPingEffect.recurringTime == RecurringTime.NO_REPEAT) {
                            toastUtils.showToast(
                                String.format(
                                    getString(R.string.schedule_ping_message),
                                    getDate(sendPingEffect.scheduledTime)
                                )
                            )
                        } else {
                            toastUtils.showToast(
                                String.format(
                                    getString(R.string.schedule_recurring_ping_message),
                                    getDate(sendPingEffect.scheduledTime),
                                    context?.let { context ->
                                        getString(sendPingEffect.recurringTime.getResId())
                                    }
                                )
                            )
                        }
                        dismiss()
                    }

                    is SendPingOfflineEffect -> {
                        toastUtils.showShortToast(R.string.ping_send_offline_message)
                        sendOfflinePing(sendPingEffect.pingData)
                        dismiss()
                    }
                }
            }
        }
    }


    private fun handleChips(
        receivers: List<ReceiverChip>,
        currentChips: MutableList<String>
    ) {
        receivers.forEach {
            if (it.id !in currentChips) {
                addChip(it.name, it.url, it.id)
            }
        }
        currentChips.forEach { currentChip ->
            if (currentChip !in receivers.map { it.id }) {
                removeChip(currentChip)
            }
        }
    }

    private fun getCurrentChips(): MutableList<String> {
        val chipGroup = binding.cgReceiver
        val currentChips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            if (chipGroup.getChildAt(i) is Chip) {
                val chip = chipGroup.getChildAt(i) as Chip
                currentChips.add(chip.tag as String)
            }
        }
        return currentChips
    }

    private fun Int.dp(): Float {
        return (this * Resources.getSystem().displayMetrics.density + 0.5f)
    }

    private fun sendOfflinePing(pingData: PingData) {
        pingAppWorkManager.startSendPingOfflineWorker(pingData)
    }

    private fun getIndexOfChip(chipId: Any?): Int {
        var index = 0
        val chipGroup = binding.cgReceiver
        for (i in 0 until chipGroup.childCount) {
            if (chipGroup.getChildAt(i) is Chip) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.tag === chipId) {
                    return index
                }
                index++
            }
        }
        return -1
    }

    private fun addChip(chipText: String?, url: String?, id: String?) {
        val chip = Chip(activity)
        chip.tag = id
        val chipGroup = binding.cgReceiver
        if (chipText != null) {
            chip.setChipView(chipText, url)
        }
        chip.setOnCloseIconClickListener {
            removeChip(it.tag as String)
        }
        val tilReceiver = binding.tilReceiver
        chipGroup.addView(chip, 0)
        chipGroup.visibility = View.VISIBLE
        binding.actvReceiver.setText(" ")
        binding.tvInvisible.text = ""
        tilReceiver.isEnabled = true
    }

    private fun removeChip(currentChip: String) {
        val chipGroup = binding.cgReceiver
        val tilReceiver = binding.tilReceiver
        tilReceiver.isEnabled = true
        tilReceiver.requestFocus()
        chipGroup.removeViewAt(getIndexOfChip(currentChip))
        viewModel.onSendPingIntent(SendPingReceiverRemovedIntent(currentChip))
        if (binding.switchSendToGroup.isChecked) {
            binding.switchSendToGroup.isChecked = false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setButtons() {
        calendar.set(year, month, day, hour, minute)
        val time = SimpleDateFormat("HH:mm").format(calendar.time)
        val date = SimpleDateFormat("dd.MM.yyyy").format(calendar.time)
        binding.btnTime.editText?.setText(time)
        binding.btnDate.editText?.setText(date)

    }

    private fun openDatePicker() {
        // Create calendar object and set the date to be that returned from selection
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.getDefault())
        val currentDate = calendar.timeInMillis
        val datePicker = setDatePicker(currentDate)
        datePicker.show(parentFragmentManager, "tag")
        handlePositiveButtonClick(datePicker, calendar)
    }

    private fun setConstraints(currentDate: Long) = CalendarConstraints.Builder()
        .setOpenAt(currentDate)
        .setValidator(DateValidatorPointForward.now())

    private fun setDatePicker(currentDate: Long): MaterialDatePicker<Long> {
        return MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.date_picker_title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(setConstraints(currentDate).build())
            .setTheme(R.style.MaterialCalendarTheme)
            .build()
    }

    private fun handlePositiveButtonClick(datePicker: MaterialDatePicker<Long>, calendar: Calendar) {
        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            //getting selected date
            calendar.time = Date(selectedDate)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            this.day = day
            this.month = month
            this.year = year
            setButtons()
            isScheduledTimePicked = true

            viewModel.onSendPingIntent(SendPingDatePickedIntent(year, month, day))
        }
    }

    private fun openTimePicker() {
        val setListener = TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
            this.hour = hourOfDay
            this.minute = minute
            setButtons()
            isScheduledTimePicked = true
            viewModel.onSendPingIntent(SendPingTimePickedIntent(hourOfDay, minute))
        }

        val timePickerDialog = this.activity?.let { activity ->
            TimePickerDialog(
                activity, R.style.PickerDialogTheme, setListener, hour, minute, true
            )
        }
        timePickerDialog?.show()
    }

    private fun initEmojiKeyboard() {
        emojiKeyboard = EmojiPopup.Builder.fromRootView(binding.root)
            .setOnEmojiClickListener { _, _ ->
                binding.etEmoji.isEnabled = false
                emojiKeyboard.dismiss()
                viewModel.onSendPingIntent(SendPingEmojiPickedIntent(binding.etEmoji.text.toString()))
            }
            .build(binding.etEmoji)
        SingleEmojiTrait.install(binding.etEmoji)
        binding.etEmoji.showSoftInputOnFocus = false
    }

    private fun setTimeOneMinuteAhead() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    private fun Chip.getUserProfileIcon(url: String?) {
        if (url.isNullOrBlank()) {
            setChipIconResource(R.drawable.ic_default_user_avatar)
        } else {
            activity?.let {
                Glide.with(it)
                    .load(url)
                    .circleCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            chipIcon = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            chipIcon = placeholder
                        }
                    })
            }
        }
    }

    private fun Chip.setChipView(text: String, url: String?) {
        this.setCloseIconResource(R.drawable.ic_chip_close)
        setCloseIconTintResource(R.color.intouch_primary_01)
        setCloseIconSizeResource(R.dimen.chip_close_icon_size)
        setChipBackgroundColorResource(R.color.intouch_neutral_03)
        getUserProfileIcon(url)
        this.text = text
        isCloseIconVisible = true
        isChipIconVisible = true
    }
}