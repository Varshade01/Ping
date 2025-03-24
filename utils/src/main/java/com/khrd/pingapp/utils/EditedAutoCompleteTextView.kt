package com.khrd.pingapp.utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

class EditedAppCompatAutoCompleteTextView(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
    AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing) {
            val inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputManager.hideSoftInputFromWindow(findFocus().windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                )
            ) {
                return true
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }
}