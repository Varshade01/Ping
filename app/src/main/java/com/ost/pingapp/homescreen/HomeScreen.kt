package com.khrd.pingapp.homescreen

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.constants.LocaleConstants
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.databinding.ActivityHomeScreenBinding
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.fragments.HomeScreenSharedViewModel
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.utils.HomeScreenViewState
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeScreen : PingAppBaseActivity() {
    private lateinit var navController: NavController

    private lateinit var binding: ActivityHomeScreenBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuthAPI

    @Inject
    lateinit var loadReceivedPingsUseCase: LoadReceivedPingsUseCase

    private val homeScreenSharedViewModel: HomeScreenSharedViewModel by viewModels()

    @Inject
    lateinit var homeScreenViewState: HomeScreenViewState

    private fun allPingsAreSeen(pings: List<DatabasePing>): Boolean {
        val newPings = pings.filter { !it.views.containsKey(firebaseAuth.currentUserId()) }
        return newPings.isEmpty()
    }

    override fun onResume() {
        super.onResume()
        initWithPingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeBottomNavigation()
        binding.sendPing.setOnClickListener {
            openSendPingDialog()
        }

        lifecycleScope.launch {
            loadReceivedPingsUseCase.loadReceivedPings().collect { pingsData ->
                if (pingsData == null) {
                    return@collect
                }
                val bottomNavBar: BottomNavigationView = binding.bottomNavBar
                if (allPingsAreSeen(pingsData.listOfPings)) {
                    bottomNavBar.removeBadge(R.id.pings_tab)
                } else {
                    bottomNavBar.getOrCreateBadge(R.id.pings_tab)
                }
            }
        }
        homeScreenSharedViewModel.eventLiveData.observe(this@HomeScreen) { event ->
            event?.getContentIfNotHandled()?.let {
                NavigationUI.onNavDestinationSelected(binding.bottomNavBar.menu.findItem(R.id.group_tab), navController)
            }
        }
        initWithProfileScreen()
    }

    private fun initWithPingsFragment() {
        if (intent.extras?.containsKey(Constants.OPEN_PINGS_INTENT_KEY) == true) {
            NavigationUI.onNavDestinationSelected(binding.bottomNavBar.menu.findItem(R.id.pings_tab), navController)
            intent.removeExtra(Constants.OPEN_PINGS_INTENT_KEY)
        }
    }

    private fun initWithProfileScreen() {
        if (intent.extras?.containsKey(LocaleConstants.OPEN_PROFILE_INTENT_KEY) == true) {
            binding.bottomNavBar.selectedItemId = R.id.profile_tab
            intent.removeExtra(LocaleConstants.OPEN_PROFILE_INTENT_KEY)
        }
    }

    override fun onRestart() {
        super.onRestart()
        this.intent.removeExtra(Constants.GROUP_ID_FROM_DEEP_LINKING)
    }

    private fun initializeBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavBar.setOnItemReselectedListener { item ->
            if (item.itemId != binding.bottomNavBar.selectedItemId)
                NavigationUI.onNavDestinationSelected(item, navController)
        }
        binding.bottomNavBar.setupWithNavController(navController)
    }

    fun openSendPingDialog(user: String? = null, group: String? = null) {
        val users = user?.let { arrayOf(it) }
        val action = HomescreenNavGraphDirections.openSendPingDialog(users = users, group = group)
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigateSafe(action)
    }
}