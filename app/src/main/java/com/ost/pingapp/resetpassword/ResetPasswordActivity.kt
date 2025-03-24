package com.khrd.pingapp.resetpassword

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.ActivityResetPasswordBinding
import com.khrd.pingapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class ResetPasswordActivity : PingAppBaseActivity() {

    @Inject
    lateinit var dialogUtils: DialogUtils

    private var binding by Delegates.notNull<ActivityResetPasswordBinding>()
    private val viewModel by viewModels<ResetPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
        initListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun initActionBar() {
        setSupportActionBar(binding.tbResetPassword)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.reset_password_screen_title)
        }
    }

    private fun initListeners() {
        viewModel.emailIsValid.observe(this, {
            binding.btnResetPassword.isEnabled = it
        })

        viewModel.resetPasswordStateLiveData.observe(this, {
            when (it) {
                ResetPasswordState.ResetPasswordSuccess -> {
                    dialogUtils.showSuccessDialog(
                        context = this,
                        getString(R.string.check_your_inbox),
                        getString(R.string.password_reset_link)
                    ) {
                        onBackPressed()
                    }
                }
                ResetPasswordState.InvalidEmail ->
                    toastUtils.showToast(R.string.please_provide_a_valid_email)
                ResetPasswordState.NetworkFailure ->
                    toastUtils.showNetworkErrorToast()
                ResetPasswordState.TooManyRequests ->
                    toastUtils.showTooManyRequestToast()
                ResetPasswordState.UnknownFailure ->
                    toastUtils.showUnknownErrorToast()
            }
        })

        binding.btnResetPassword.setOnClickListener {
            viewModel.onResetPasswordButtonClick(binding.tieResetPasswordEmail.text.toString())
        }

        binding.tieResetPasswordEmail.doOnTextChanged { text, _, _, _ ->
            text?.let {
                viewModel.onEditTextEmailAddressTextChange(it)
            }
        }

        binding.btnResetPasswordCancel.setOnClickListener {
            onBackPressed()
        }
    }
}