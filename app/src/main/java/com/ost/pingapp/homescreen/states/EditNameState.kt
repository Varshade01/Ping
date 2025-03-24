package com.khrd.pingapp.homescreen.states

sealed interface IEditNameState

sealed class EditNameState : IEditNameState

class EditNameFailure() : EditNameState()

class EditNameSuccess() : EditNameState()

class EditNameOffline(val name: String) : EditNameState()

data class NameSaveState(val error: UserNameValidationState) : EditNameState()

class NameEditState() : EditNameState()

enum class UserNameValidationState {
    VALID,
    TOO_LONG,
    EMPTY_FIELD,
    NAME_LETTERS_FAILURE,
}