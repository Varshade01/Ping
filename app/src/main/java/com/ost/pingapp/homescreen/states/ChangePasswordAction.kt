package com.khrd.pingapp.homescreen.states

sealed class ProfileAction

class ChangePasswordAction: ProfileAction()
class DeleteAccountAction: ProfileAction()