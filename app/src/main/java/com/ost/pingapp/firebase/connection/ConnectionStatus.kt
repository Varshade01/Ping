package com.khrd.pingapp.firebase.connection

interface ConnectionStatus {
    fun getConnectionStatus(): Boolean
    fun retrieveConnectionStatus(callback: (Boolean) -> Unit)
    fun retrieveConnectionStatus(): Boolean
}