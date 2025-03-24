package com.khrd.pingapp.homescreen.states

sealed class EditJobPositionState
sealed class EditJobPositionSaveResultState

data class JobPositionSaveState(
    val result: EditJobPositionSaveResultState = JobPositionFailureState
) : EditJobPositionState()

object JobPositionEditState : EditJobPositionState()

data class JobPositionSaveSuccessState(
    val name: String
) : EditJobPositionSaveResultState()

data class JobPositionSaveFailureState(
    val validationState: JobNameValidationState
) : EditJobPositionSaveResultState()

object JobPositionFailureState : EditJobPositionSaveResultState()

data class JobPositionOfflineUpdateState(
    val name: String
) : EditJobPositionSaveResultState()

enum class JobNameValidationState {
    VALID,
    TOO_LONG,
    INVALID_CHARS
}