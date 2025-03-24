package com.khrd.pingapp.registration

import android.util.Log
import androidx.lifecycle.*
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.useradministration.usecases.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationUseCase: RegistrationUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val firebaseAuth: FirebaseAuthAPI
) : ViewModel() {

    private val _registrationStateLiveData = MutableLiveData<IRegistrationState>()
    val registrationStateLiveData: LiveData<IRegistrationState> get() = _registrationStateLiveData

    fun registerUser(fields: RegistrationFields) {
        viewModelScope.launch {
            val registerUserState = registrationUseCase.registerUser(fields)
            Log.i("RegistrationViewModel", "user registered")
            if (registerUserState is RegistrationSuccess) {
                Log.i("RegistrationViewModel", "user created")
                // registration is successful, creating user
                firebaseAuth.currentUserId()?.let { userId ->
                    val createUserState = createUserUseCase.createUser(userId, fields.email, fields.name)
                    _registrationStateLiveData.postValue(createUserState)
                }
            } else {
                _registrationStateLiveData.postValue(registerUserState)
            }
        }
    }

    fun createUserViaSocialAccount(email: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.currentUserId()?.let { userId ->
                val createUserState = createUserUseCase.createUser(userId, email, name)
                _registrationStateLiveData.postValue(createUserState)
            }
        }
    }

}

