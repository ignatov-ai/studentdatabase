// AddStudentActivity.kt
package com.example.studentdatabase

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class AddStudentActivity : AppCompatActivity() {

    private lateinit var etSurname: EditText
    private lateinit var etName: EditText
    private lateinit var etClass: EditText
    private lateinit var cbHasIUP: CheckBox
    private lateinit var etIUPInfo: EditText
    private lateinit var etComment: EditText
    private lateinit var btnSave: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView

    private var imageCapture: ImageCapture? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        etSurname = findViewById(R.id.etSurname)
        etName = findViewById(R.id.etName)
        etClass = findViewById(R.id.etClass)
        cbHasIUP = findViewById(R.id.cbHasIUP)
        etIUPInfo = findViewById(R.id.etIUPInfo)
        etComment = findViewById(R.id.etComment)
        btnSave = findViewById(R.id.btnSave)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        previewView = findViewById(R.id.previewView)
        imageView = findViewById(R.id.imageView)

        // Показываем поле для информации по ИУП, если checkbox отмечен
        cbHasIUP.setOnCheckedChangeListener { _, isChecked ->
            etIUPInfo.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Кнопка для съемки фото
        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            }
        }

        // Кнопка для сохранения данных
        btnSave.setOnClickListener {
            saveStudent()
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    imageView.setImageURI(savedUri)
                    Toast.makeText(this@AddStudentActivity, "Фото сохранено", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@AddStudentActivity, "Ошибка при съемке фото", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun saveStudent() {
        val surname = etSurname.text.toString()
        val name = etName.text.toString()
        val className = etClass.text.toString()
        val hasIUP = cbHasIUP.isChecked
        val iupInfo = etIUPInfo.text.toString()
        val comment = etComment.text.toString()

        if (surname.isEmpty() || name.isEmpty() || className.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val id = dbHelper.generateUniqueId()

        // Преобразуем фото в ByteArray
        val photoBytes = photoFile?.readBytes()

        // Сохраняем данные в базу
        val success = dbHelper.insertStudent(id, surname, name, className, hasIUP, iupInfo, photoBytes, comment)
        if (success) {
            Toast.makeText(this, "Учащийся добавлен", Toast.LENGTH_SHORT).show()
            finish() // Закрываем активность после сохранения
        } else {
            Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
        }
    }
}