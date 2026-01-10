package com.example.superfastbrowser

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class IncognitoActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: BrowserPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incognito)

        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = BrowserPagerAdapter(this)

        viewPager.adapter = pagerAdapter
        pagerAdapter.addFragment(BrowserFragment.newInstance(true))

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "Incognito Tab ${position + 1}"
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.incognito_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_tab -> {
                pagerAdapter.addFragment(BrowserFragment.newInstance(true))
                viewPager.currentItem = pagerAdapter.itemCount - 1
                true
            }
            R.id.action_close_tab -> {
                if (pagerAdapter.itemCount > 1) {
                    val currentPosition = viewPager.currentItem
                    pagerAdapter.removeFragment(currentPosition)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val currentFragment = pagerAdapter.getFragment(viewPager.currentItem)
        if (currentFragment != null && currentFragment.onBackPressed()) {
            // The fragment handled the back press
        } else {
            super.onBackPressed()
        }
    }
}
