package com.khrd.pingapp.homescreen.deleteAccount.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountState

interface IDeleteAccountUseCase {
    fun deleteAccount(password: String, callback: (DeleteAccountState) -> Unit)
}