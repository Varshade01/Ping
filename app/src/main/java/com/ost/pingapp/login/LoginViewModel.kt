package com.khrd.pingapp.login

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    val loginStateLiveData: MutableLiveData<LoginState> = MutableLiveData<LoginState>()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginStateLiveData.value = loginUseCase.login(email, password)
        }
    }
}