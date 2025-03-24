package com.khrd.pingapp.login

interface LoginUseCase {
    suspend fun login(email: String, password: String): LoginState
}