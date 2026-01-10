package com.example.superfastbrowser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.superfastbrowser.db.BrowserDao
import com.example.superfastbrowser.db.EncryptionHelper

class PasswordsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwords)

        val listView = findViewById<ListView>(R.id.passwords_list)
        val browserDao = BrowserDao(this)
        val passwords = browserDao.getAllPasswords()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            passwords.map { "${it.url}\n${it.username}" }
        )

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val password = passwords[position]
            val decryptedPassword = EncryptionHelper.decrypt(password.passwordEncrypted, password.iv)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.password))
                .setMessage("${getString(R.string.username)}: ${password.username}\n${getString(R.string.password)}: $decryptedPassword")
                .setPositiveButton(getString(R.string.copy)) { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("password", decryptedPassword)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, getString(R.string.password_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.close), null)
                .show()
        }
    }
}
