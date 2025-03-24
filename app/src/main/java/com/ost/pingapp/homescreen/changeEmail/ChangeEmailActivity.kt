package com.khrd.pingapp.homescreen.changeEmail

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.text.bold
import androidx.core.widget.addTextChangedListener
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.ActivityChangeEmailBinding
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailError.*
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.utils.WarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class ChangeEmailActivity : PingAppBaseActivity() {
    private var binding by Delegates.notNull<ActivityChangeEmailBinding>()
    private val viewModel: ChangeEmailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
        initActionBar()
        initListeners()
        setContentView(binding.root)
    }


    private fun initListeners() {
        binding.btnChangeEmail.setOnClickListener {
            viewModel.validateCredentials(binding.password.editText?.text.toString())
        }

        binding.buttonCancel.setOnClickListener {
            onBackPressed()
        }

        binding.newEmail.editText?.addTextChangedListener {
            viewModel.onEmailChanged(it.toString().trim())
        }

        viewModel.changeEmailStateLiveData.observe(this) {
            when (it) {
                is ChangeEmailFailure -> {
                    when (it.error) {
                        EMPTY_EMAIL_FIELD -> binding.newEmail.error = getString(R.string.fillout_the_field)
                        INVALID_EMAIL -> binding.newEmail.error = getString(R.string.invalid_email)
                        SAME_EMAIL_ERROR -> binding.newEmail.error = getString(R.string.same_email_error)
                        EMAIL_ALREADY_REGISTERED -> binding.newEmail.error = getString(R.string.email_already_in_use)
                        TOO_MANY_REQUESTS -> Toast.makeText(this, R.string.too_many_requests, Toast.LENGTH_SHORT).show()
                        NETWORK_ERROR -> Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
                        UNKNOWN_ERROR -> Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                        INVALID_PASSWORD -> binding.password.error = getString(R.string.confirm_password_validation_message)
                        EMPTY_PASSWORD_FIELD -> binding.password.error = getString(R.string.fillout_the_field)
                    }
                }
                ChangeEmailSuccess -> {
                    val intent = Intent(this, VerificationActivity::class.java)
                    startActivity(intent)
                }
                is ChangeEmailValidationSuccess -> changeEmailConfirmationDialog(it.email)
            }
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun changeEmailConfirmationDialog(email: String) {
        val message = SpannableStringBuilder().append(getString(R.string.change_email_dialog_message)).bold { append(email) }
        WarningDialog.show(this, getString(R.string.change_email_dialog_header), message, getString(R.string.ok), false) {
            viewModel.changeEmail(binding.password.editText?.text.toString())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}