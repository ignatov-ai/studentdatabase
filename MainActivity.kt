package com.example.studentdatabase

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var studentPhoto: ImageView
    private lateinit var studentInfo: TextView
    private var photoBytes: ByteArray? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                studentPhoto.setImageBitmap(bitmap)
                photoBytes = getBytesFromBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            studentPhoto.setImageBitmap(it)
            photoBytes = getBytesFromBitmap(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        studentPhoto = findViewById(R.id.studentPhoto)
        studentInfo = findViewById(R.id.studentInfo)

        findViewById<Button>(R.id.loadDataButton).setOnClickListener { loadDataFromExcel() }
        findViewById<Button>(R.id.scanCardButton).setOnClickListener { scanCard() }
        findViewById<Button>(R.id.addPhotoButton).setOnClickListener { pickImageLauncher.launch("image/*") }
        findViewById<Button>(R.id.takePhotoButton).setOnClickListener { takePhotoLauncher.launch() }

        findViewById<Button>(R.id.btnAddStudent).setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDataFromExcel() {
        // Реализация загрузки данных из Excel
        Toast.makeText(this, "Загрузка данных из Excel", Toast.LENGTH_SHORT).show()
    }

    private fun scanCard() {
        val cardId = "123456789" // Пример ID карты
        val cursor = dbHelper.getStudentById(cardId)
        if (cursor.moveToFirst()) {
            val surname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SURNAME))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
            val className = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS))
            val hasIUP = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HAS_IUP)) == 1
            val iupInfo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IUP_INFO))
            val photo = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHOTO))
            val comment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENT))

            if (hasIUP) {
                studentInfo.text = "Фамилия: $surname\nИмя: $name\nКласс: $className\nИУП: $iupInfo\nКомментарий: $comment"
                if (photo != null) {
                    val bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.size)
                    studentPhoto.setImageBitmap(bitmap)
                }
            } else {
                studentInfo.text = "НЕТ ИУП"
            }
        } else {
            studentInfo.text = "Учащийся не найден"
        }
        cursor.close()
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
