package com.khrd.pingapp.homescreen.deleteAccount

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.ActivityDeleteAccountBinding
import com.khrd.pingapp.registration.RegistrationActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountActivity : PingAppBaseActivity() {
    private lateinit var binding: ActivityDeleteAccountBinding
    private val viewModel: DeleteAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
        initListeners()
    }

    fun initListeners() {
        binding.btnDeleteAccount.setOnClickListener {
            viewModel.deleteAccount(binding.tieDeleteAccountPassword.text.toString())
        }

        binding.btnDeleteAccountCancel.setOnClickListener {
            onBackPressed()
        }

        viewModel.deleteAccountStateLiveData.observe(this) {
            when (it) {
                is DeleteAccountFailure -> {
                    when (it.error) {
                        DeleteAccountError.EMPTY_FIELD -> binding.tilDeleteAccountPassword.error = getString(R.string.fillout_the_field)

                        DeleteAccountError.PASSWORD_IS_TOO_SHORT -> binding.tilDeleteAccountPassword.error =
                            getString(R.string.password_too_short)

                        DeleteAccountError.PASSWORD_DOESNT_MATCH -> binding.tilDeleteAccountPassword.error =
                            getString(R.string.current_password_doesnt_match)

                        DeleteAccountError.CONNECTION_ERROR -> Toast.makeText(
                            this,
                            getString(R.string.no_internet_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                        DeleteAccountError.TOO_MANY_REQUESTS -> Toast.makeText(
                            this,
                            getString(R.string.too_many_requests),
                            Toast.LENGTH_SHORT
                        ).show()
                        DeleteAccountError.UNKNOWN_ERROR -> Toast.makeText(
                            this,
                            getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                DeleteAccountSuccess -> {
                    toRegistrationActivity()
                }
            }
        }

        binding.tieDeleteAccountPassword.addTextChangedListener { if (!it.isNullOrBlank()) binding.tilDeleteAccountPassword.error = "" }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.tbDeleteAccount)
        binding.tbDeleteAccount.setNavigationOnClickListener { onBackPressed() }
    }

    private fun toRegistrationActivity() {
        val intent = Intent(this, RegistrationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}