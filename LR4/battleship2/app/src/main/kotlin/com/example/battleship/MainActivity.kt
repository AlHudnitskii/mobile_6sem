package com.example.battleship

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.battleship.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // Choose start destination
        val repo = (application as App).repository
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(
            if (repo.isLoggedIn) R.id.lobbyFragment else R.id.authFragment
        )
        navController.graph = graph

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, dest, _ ->
            binding.bottomNavigation.visibility =
                if (dest.id in listOf(R.id.lobbyFragment, R.id.profileFragment, R.id.statsFragment))
                    View.VISIBLE else View.GONE
        }
    }
}
