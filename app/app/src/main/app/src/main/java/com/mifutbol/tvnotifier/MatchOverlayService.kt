package com.mifutbol.tvnotifier

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MatchOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val apiKey = "3"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startFastPolling()
        }
        return START_STICKY
    }

    private fun startFastPolling() {
        thread {
            while (isRunning) {
                try {
                    // Leer los favoritos guardados en la memoria de ESTA televisión
                    val prefs = getSharedPreferences("TV_PREFS", Context.MODE_PRIVATE)
                    val savedFavorites = prefs.getString("favorites_list", "real madrid, alcaraz, alonso") ?: ""
                    val targetKeywords = savedFavorites.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

                    if (targetKeywords.isNotEmpty()) {
                        val url = URL("https://www.thesportsdb.com/api/v1/json/$apiKey/latestsoccer.php")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.requestMethod = "GET"

                        if (connection.responseCode == 200) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            val json = JSONObject(response)
                            val events = json.optJSONArray("teams") ?: json.optJSONArray("events")

                            if (events != null) {
                                for (i in 0 until events.length()) {
                                    val event = events.getJSONObject(i)
                                    val homeTeam = event.optString("strHomeTeam", "").lowercase()
                                    val awayTeam = event.optString("strAwayTeam", "").lowercase()
                                    val eventName = event.optString("strEvent", "").lowercase()
                                    val progress = event.optString("strProgress", "")

                                    val isTracked = targetKeywords.any { keyword ->
                                        homeTeam.contains(keyword) || awayTeam.contains(keyword) || eventName.contains(keyword)
                                    }

                                    if (isTracked && (progress == "Goal" || progress == "Event")) {
                                        val title = event.optString("strEvent", "¡ALERTA EN DIRECTO!")
                                        val score = "${event.optString("intHomeScore", "0")} - ${event.optString("intAwayScore", "0")}"
                                        
                                        handler.post {
                                            showOverlay(title, score)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Consulta rápida cada 20 segundos para máxima velocidad
                Thread.sleep(20000)
            }
        }
    }

    private fun showOverlay(title: String, score: String) {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 60
            y = 60
        }

        val textView = TextView(this).apply {
            text = "⚡ GOL / EVENTO EN DIRECTO\n$title\nMarcador: $score"
            textSize = 18f
            setPadding(40, 30, 40, 30)
            setBackgroundColor(0xEE111111.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        overlayView = textView
        windowManager.addView(overlayView, params)

        handler.postDelayed({
            removeOverlay()
        }, 12000)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
