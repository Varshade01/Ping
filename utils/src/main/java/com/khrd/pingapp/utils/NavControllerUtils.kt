package com.khrd.pingapp.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

object NavControllerUtils{
    fun NavController.navigateSafe(directions: NavDirections, navOptions: NavOptions? = null) {
        currentDestination?.getAction(directions.actionId)?.let {
            navigate(directions, navOptions)
        }
    }
}