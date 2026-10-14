package io.github.winzysss.fuckdpi.activities

import android.os.Bundle
import android.view.MenuItem
import io.github.winzysss.fuckdpi.R
import io.github.winzysss.fuckdpi.fragments.DomainListsFragment
import io.github.winzysss.fuckdpi.fragments.ProxyTestSettingsFragment

class TestSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_settings)
        setupToolbar()

        if (savedInstanceState == null) {
            val openFragment = intent.getStringExtra("open_fragment")

            when (openFragment) {
                "domain_lists" -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.test_settings, DomainListsFragment())
                        .commit()
                }
                else -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.test_settings, ProxyTestSettingsFragment())
                        .commit()
                }
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
