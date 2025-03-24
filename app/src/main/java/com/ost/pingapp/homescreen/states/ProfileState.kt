package com.khrd.pingapp.homescreen.states

sealed class ProfileState

class ToChangePasswordActivity : ProfileState()
class ToDeleteAccount : ProfileState()