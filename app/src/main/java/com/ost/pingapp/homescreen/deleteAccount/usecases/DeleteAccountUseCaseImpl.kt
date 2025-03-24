package com.khrd.pingapp.homescreen.deleteAccount.usecases

import android.util.Log
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountState
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.authentication.ReauthenticationError
import com.khrd.pingapp.firebase.authentication.ReauthenticationFailure
import com.khrd.pingapp.firebase.authentication.ReauthenticationSuccess
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCase
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountError
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountFailure
import javax.inject.Inject

class DeleteAccountUseCaseImpl @Inject constructor(
    private val userRepository: UsersRepository,
    private val firebaseAuth: FirebaseAuthAPI,
    private val leaveGroupUseCase: LeaveGroupUseCase
) : IDeleteAccountUseCase {


    override fun deleteAccount(password: String, callback: (DeleteAccountState) -> Unit) {
        firebaseAuth.reauthenticate(password) { reauthState ->
            when (reauthState) {
                is ReauthenticationSuccess -> {
                    Log.i("***", "Reauthenticated successfully")

                    // getting  current user's group
                    val userId = firebaseAuth.currentUserId()
                    if (userId != null) {
                        userRepository.getUserGroups(userId,  DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { it ->
                            when (it) {
                                UserRequestState.UserRequestFail -> {
                                    Log.i("***", "Current group is empty")
                                }
                                is UserRequestState.UserRequestSuccess -> {
                                    val groupsIds = it.user?.groups?.values?.toList()
                                    groupsIds?.forEach { leaveGroupUseCase.leaveGroup(it) { group_state ->
                                        Log.i("***", "Current group = $it") //need to refactor
                                        if (group_state is GroupSuccess) {
                                                Log.i("***", "Left $groupsIds group.")
                                            }
                                    } }
                                }
                            }

                            // deleting current user from database
                            userRepository.deleteUser(userId) { user_state ->
                                if (user_state is UserRequestState.UserRequestSuccess) {
                                    Log.i("***", "User $userId deleted.")

                                    // deleting firebase account
                                    firebaseAuth.deleteAccount() { deletionState ->
                                        callback(deletionState)
                                    }
                                }
                            }
                        }
                    }
                }

                is ReauthenticationFailure -> {
                    Log.i("***", "Password doesn't match")
                    when (reauthState.error) {
                        ReauthenticationError.InvalidCredentials -> callback(DeleteAccountFailure(DeleteAccountError.PASSWORD_DOESNT_MATCH))
                        ReauthenticationError.NetworkFailure -> callback(DeleteAccountFailure(DeleteAccountError.CONNECTION_ERROR))
                        ReauthenticationError.TooManyRequests -> callback(DeleteAccountFailure(DeleteAccountError.TOO_MANY_REQUESTS))
                        ReauthenticationError.UnknownFailure -> callback(DeleteAccountFailure(DeleteAccountError.UNKNOWN_ERROR))
                    }

                }
            }
        }
    }
}
