package com.example.superfastbrowser

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.superfastbrowser.db.BrowserDao

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : androidx.preference.PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val clearDataPreference: androidx.preference.Preference? = findPreference("clear_data")
            clearDataPreference?.setOnPreferenceClickListener {
                val browserDao = BrowserDao(requireContext())
                browserDao.clearHistory()
                browserDao.clearBookmarks()
                val webView = WebView(requireContext())
                webView.clearCache(true)
                CookieManager.getInstance().removeAllCookies(null)
                Toast.makeText(requireContext(), "Browsing data cleared", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}
