package com.example.superfastbrowser

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.superfastbrowser.util.AdblockerManager
import kotlinx.coroutines.launch

class AdblockerSettingsActivity : AppCompatActivity() {

    private lateinit var adblockerManager: AdblockerManager
    private lateinit var popularLists: Map<String, String>
    private val enabledLists = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adblocker_settings)

        popularLists = mapOf(
            getString(R.string.easylist) to "https://easylist.to/easylist/easylist.txt",
            getString(R.string.easyprivacy) to "https://easylist.to/easylist/easyprivacy.txt",
            getString(R.string.ublock_filters) to "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters.txt"
        )

        adblockerManager = AdblockerManager(this)

        val sharedPrefs = getSharedPreferences("adblocker_settings", MODE_PRIVATE)
        enabledLists.addAll(sharedPrefs.getStringSet("enabled_lists", emptySet()) ?: emptySet())

        val popularListsRecyclerView = findViewById<RecyclerView>(R.id.popular_lists_recyclerview)
        popularListsRecyclerView.layoutManager = LinearLayoutManager(this)
        popularListsRecyclerView.adapter = PopularListsAdapter(popularLists, enabledLists)

        val customRulesEditText = findViewById<EditText>(R.id.custom_rules_edittext)
        customRulesEditText.setText(sharedPrefs.getString("custom_rules", ""))

        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            lifecycleScope.launch {
                val editor = sharedPrefs.edit()
                editor.putStringSet("enabled_lists", enabledLists)
                editor.putString("custom_rules", customRulesEditText.text.toString())
                editor.apply()

                enabledLists.forEach { listName ->
                    val url = popularLists[listName]
                    if (url != null) {
                        adblockerManager.downloadAndStoreList(url, "$listName.txt")
                    }
                }
                val popularListFiles = enabledLists.map { "$it.txt" }
                adblockerManager.mergeAndLoadRules(popularListFiles, customRulesEditText.text.toString())
            }
        }
    }
}
