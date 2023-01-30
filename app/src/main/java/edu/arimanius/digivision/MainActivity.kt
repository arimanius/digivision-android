package edu.arimanius.digivision

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import edu.arimanius.digivision.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var isHome = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val appBarConfiguration = AppBarConfiguration(
            navController.graph, drawerLayout,
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.searchFragment)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isHome) {
            return super.onOptionsItemSelected(item)
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}