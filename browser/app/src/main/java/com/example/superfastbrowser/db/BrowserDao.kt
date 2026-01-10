package com.example.superfastbrowser.db

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns

class BrowserDao(context: Context) {
    private val dbHelper = BrowserDbHelper(context)

    fun addBookmark(title: String, url: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BookmarkContract.BookmarkEntry.COLUMN_NAME_TITLE, title)
            put(BookmarkContract.BookmarkEntry.COLUMN_NAME_URL, url)
        }
        db.insert(BookmarkContract.BookmarkEntry.TABLE_NAME, null, values)
    }

    fun getAllBookmarks(): List<Bookmark> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            BookmarkContract.BookmarkEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        val bookmarks = mutableListOf<Bookmark>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val title = getString(getColumnIndexOrThrow(BookmarkContract.BookmarkEntry.COLUMN_NAME_TITLE))
                val url = getString(getColumnIndexOrThrow(BookmarkContract.BookmarkEntry.COLUMN_NAME_URL))
                bookmarks.add(Bookmark(id, title, url))
            }
        }
        cursor.close()
        return bookmarks
    }

    fun deleteBookmark(id: Long) {
        val db = dbHelper.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(BookmarkContract.BookmarkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun addHistory(title: String, url: String, timestamp: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(HistoryContract.HistoryEntry.COLUMN_NAME_TITLE, title)
            put(HistoryContract.HistoryEntry.COLUMN_NAME_URL, url)
            put(HistoryContract.HistoryEntry.COLUMN_NAME_TIMESTAMP, timestamp)
        }
        db.insert(HistoryContract.HistoryEntry.TABLE_NAME, null, values)
    }

    fun getAllHistory(): List<History> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            HistoryContract.HistoryEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${HistoryContract.HistoryEntry.COLUMN_NAME_TIMESTAMP} DESC"
        )
        val history = mutableListOf<History>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val title = getString(getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_NAME_TITLE))
                val url = getString(getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_NAME_URL))
                val timestamp = getLong(getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_NAME_TIMESTAMP))
                history.add(History(id, title, url, timestamp))
            }
        }
        cursor.close()
        return history
    }

    fun clearHistory() {
        val db = dbHelper.writableDatabase
        db.delete(HistoryContract.HistoryEntry.TABLE_NAME, null, null)
    }

    fun clearBookmarks() {
        val db = dbHelper.writableDatabase
        db.delete(BookmarkContract.BookmarkEntry.TABLE_NAME, null, null)
    }

    fun addPassword(url: String, username: String, password: String) {
        val db = dbHelper.writableDatabase
        val encryptedData = EncryptionHelper.encrypt(password)
        val values = ContentValues().apply {
            put(PasswordContract.PasswordEntry.COLUMN_NAME_URL, url)
            put(PasswordContract.PasswordEntry.COLUMN_NAME_USERNAME, username)
            put(PasswordContract.PasswordEntry.COLUMN_NAME_PASSWORD, encryptedData.data)
            put(PasswordContract.PasswordEntry.COLUMN_NAME_IV, encryptedData.iv)
        }
        db.insert(PasswordContract.PasswordEntry.TABLE_NAME, null, values)
    }

    fun getPasswordsForUrl(url: String): List<Password> {
        val db = dbHelper.readableDatabase
        val selection = "${PasswordContract.PasswordEntry.COLUMN_NAME_URL} = ?"
        val selectionArgs = arrayOf(url)
        val cursor = db.query(
            PasswordContract.PasswordEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val passwords = mutableListOf<Password>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val username = getString(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_USERNAME))
                val encryptedPassword = getBlob(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_PASSWORD))
                val iv = getBlob(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_IV))
                passwords.add(Password(id, url, username, encryptedPassword, iv))
            }
        }
        cursor.close()
        return passwords
    }

    fun getAllPasswords(): List<Password> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            PasswordContract.PasswordEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        val passwords = mutableListOf<Password>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val url = getString(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_URL))
                val username = getString(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_USERNAME))
                val encryptedPassword = getBlob(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_PASSWORD))
                val iv = getBlob(getColumnIndexOrThrow(PasswordContract.PasswordEntry.COLUMN_NAME_IV))
                passwords.add(Password(id, url, username, encryptedPassword, iv))
            }
        }
        cursor.close()
        return passwords
    }

    fun addVideoSubtitle(videoDownloadId: Long, subtitleDownloadId: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_VIDEO_DOWNLOAD_ID, videoDownloadId)
            put(VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_SUBTITLE_DOWNLOAD_ID, subtitleDownloadId)
        }
        db.insert(VideoSubtitleContract.VideoSubtitleEntry.TABLE_NAME, null, values)
    }

    fun getSubtitleDownloadId(videoDownloadId: Long): Long? {
        val db = dbHelper.readableDatabase
        val selection = "${VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_VIDEO_DOWNLOAD_ID} = ?"
        val selectionArgs = arrayOf(videoDownloadId.toString())
        val cursor = db.query(
            VideoSubtitleContract.VideoSubtitleEntry.TABLE_NAME,
            arrayOf(VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_SUBTITLE_DOWNLOAD_ID),
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var subtitleDownloadId: Long? = null
        with(cursor) {
            if (moveToFirst()) {
                subtitleDownloadId = getLong(getColumnIndexOrThrow(VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_SUBTITLE_DOWNLOAD_ID))
            }
        }
        cursor.close()
        return subtitleDownloadId
    }
}
