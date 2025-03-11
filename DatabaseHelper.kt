package com.example.studentdatabase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudentDatabase.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "students"
        const val COLUMN_ID = "id"
        const val COLUMN_SURNAME = "surname"
        const val COLUMN_NAME = "name"
        const val COLUMN_CLASS = "class"
        const val COLUMN_HAS_IUP = "has_iup"
        const val COLUMN_IUP_INFO = "iup_info"
        const val COLUMN_PHOTO = "photo"
        const val COLUMN_COMMENT = "comment"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_SURNAME TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_CLASS TEXT,
                $COLUMN_HAS_IUP INTEGER,
                $COLUMN_IUP_INFO TEXT,
                $COLUMN_PHOTO BLOB,
                $COLUMN_COMMENT TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertStudent(
        id: String,
        surname: String,
        name: String,
        className: String,
        hasIUP: Boolean,
        iupInfo: String,
        photo: ByteArray?,
        comment: String
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_SURNAME, surname)
            put(COLUMN_NAME, name)
            put(COLUMN_CLASS, className)
            put(COLUMN_HAS_IUP, if (hasIUP) 1 else 0)
            put(COLUMN_IUP_INFO, iupInfo)
            put(COLUMN_PHOTO, photo)
            put(COLUMN_COMMENT, comment)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun getStudentById(id: String): Cursor {
        val db = this.readableDatabase
        return db.query(TABLE_NAME, null, "$COLUMN_ID=?", arrayOf(id), null, null, null)
    }

    fun generateUniqueId(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
    }
}