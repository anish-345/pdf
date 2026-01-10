package com.example.superfastbrowser

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: BrowserPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdBlocker.loadBlocklistFromAssets(this)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = BrowserPagerAdapter(this)

        viewPager.adapter = pagerAdapter
        pagerAdapter.addFragment(BrowserFragment())

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(R.string.tab_text, position + 1)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_tab -> {
                pagerAdapter.addFragment(BrowserFragment())
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
            R.id.action_add_bookmark -> {
                val currentFragment = pagerAdapter.getFragment(viewPager.currentItem)
                val url = currentFragment?.getCurrentUrl()
                val title = currentFragment?.getCurrentTitle()
                if (url != null && title != null) {
                    val browserDao = BrowserDao(this)
                    browserDao.addBookmark(title, url)
                }
                true
            }
            R.id.action_bookmarks -> {
                val intent = Intent(this, BookmarksActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_downloads -> {
                val intent = Intent(this, DownloadsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_incognito -> {
                val intent = Intent(this, IncognitoActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_save_password -> {
                val currentFragment = pagerAdapter.getFragment(viewPager.currentItem)
                val url = currentFragment?.getCurrentUrl()
                if (url != null) {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_save_password, null)
                    val usernameEditText = dialogView.findViewById<EditText>(R.id.username)
                    val passwordEditText = dialogView.findViewById<EditText>(R.id.password)

                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.save_password))
                        .setView(dialogView)
                        .setPositiveButton(getString(R.string.save)) { _, _ ->
                            val username = usernameEditText.text.toString()
                            val password = passwordEditText.text.toString()
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                val browserDao = BrowserDao(this)
                                browserDao.addPassword(url, username, password)
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                true
            }
            R.id.action_passwords -> {
                val intent = Intent(this, PasswordsActivity::class.java)
                startActivity(intent)
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
