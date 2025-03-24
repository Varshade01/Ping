package com.khrd.pingapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.khrd.pingapp.utils.LocaleHelperEntryPoint
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

abstract class PingAppBaseActivity : AppCompatActivity() {

    @Inject
    lateinit var toastUtils: ToastUtils

    override fun onResume() {
        super.onResume()
        toastUtils.setContext(this)
    }

    override fun onPause() {
        super.onPause()
        toastUtils.removeContext()
    }

    override fun attachBaseContext(newBase: Context) {
        val localeHelper = EntryPointAccessors.fromApplication(newBase, LocaleHelperEntryPoint::class.java).localeHelper
        super.attachBaseContext(localeHelper.initLocale(newBase))
    }
}