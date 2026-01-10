package com.example.superfastbrowser.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object BookmarkContract {
    object BookmarkEntry : BaseColumns {
        const val TABLE_NAME = "bookmarks"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_URL = "url"
    }
}

object HistoryContract {
    object HistoryEntry : BaseColumns {
        const val TABLE_NAME = "history"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_TIMESTAMP = "timestamp"
    }
}

object PasswordContract {
    object PasswordEntry : BaseColumns {
        const val TABLE_NAME = "passwords"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_USERNAME = "username"
        const val COLUMN_NAME_PASSWORD = "password"
        const val COLUMN_NAME_IV = "iv"
    }
}

object VideoSubtitleContract {
    object VideoSubtitleEntry : BaseColumns {
        const val TABLE_NAME = "video_subtitles"
        const val COLUMN_NAME_VIDEO_DOWNLOAD_ID = "video_download_id"
        const val COLUMN_NAME_SUBTITLE_DOWNLOAD_ID = "subtitle_download_id"
    }
}

class BrowserDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_BOOKMARKS_TABLE = "CREATE TABLE ${BookmarkContract.BookmarkEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${BookmarkContract.BookmarkEntry.COLUMN_NAME_TITLE} TEXT," +
                "${BookmarkContract.BookmarkEntry.COLUMN_NAME_URL} TEXT)"
        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE)

        val SQL_CREATE_HISTORY_TABLE = "CREATE TABLE ${HistoryContract.HistoryEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${HistoryContract.HistoryEntry.COLUMN_NAME_TITLE} TEXT," +
                "${HistoryContract.HistoryEntry.COLUMN_NAME_URL} TEXT," +
                "${HistoryContract.HistoryEntry.COLUMN_NAME_TIMESTAMP} INTEGER)"
        db.execSQL(SQL_CREATE_HISTORY_TABLE)

        val SQL_CREATE_PASSWORDS_TABLE = "CREATE TABLE ${PasswordContract.PasswordEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${PasswordContract.PasswordEntry.COLUMN_NAME_URL} TEXT," +
                "${PasswordContract.PasswordEntry.COLUMN_NAME_USERNAME} TEXT," +
                "${PasswordContract.PasswordEntry.COLUMN_NAME_PASSWORD} BLOB," +
                "${PasswordContract.PasswordEntry.COLUMN_NAME_IV} BLOB)"
        db.execSQL(SQL_CREATE_PASSWORDS_TABLE)

        val SQL_CREATE_VIDEO_SUBTITLES_TABLE = "CREATE TABLE ${VideoSubtitleContract.VideoSubtitleEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_VIDEO_DOWNLOAD_ID} INTEGER," +
                "${VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_SUBTITLE_DOWNLOAD_ID} INTEGER)"
        db.execSQL(SQL_CREATE_VIDEO_SUBTITLES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE ${PasswordContract.PasswordEntry.TABLE_NAME} ADD COLUMN ${PasswordContract.PasswordEntry.COLUMN_NAME_IV} BLOB")
        }
        if (oldVersion < 4) {
            val SQL_CREATE_VIDEO_SUBTITLES_TABLE = "CREATE TABLE ${VideoSubtitleContract.VideoSubtitleEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_VIDEO_DOWNLOAD_ID} INTEGER," +
                    "${VideoSubtitleContract.VideoSubtitleEntry.COLUMN_NAME_SUBTITLE_DOWNLOAD_ID} INTEGER)"
            db.execSQL(SQL_CREATE_VIDEO_SUBTITLES_TABLE)
        }
    }

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "Browser.db"
    }
}
