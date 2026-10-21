package com.mifutbol.tvnotifier

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
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
    private var lastHomeScore = -1
    private var lastAwayScore = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val apiKey = intent?.getStringExtra("API_KEY") ?: ""
        val teamId = intent?.getStringExtra("TEAM_ID") ?: ""

        if (apiKey.isNotEmpty() && teamId.isNotEmpty()) {
            startPolling(apiKey, teamId)
        }
        return START_STICKY
    }

    private fun startPolling(apiKey: String, teamId: String) {
        handler.post(object : Runnable {
            override fun run() {
                checkMatchScore(apiKey, teamId)
                handler.postDelayed(this, 30000)
            }
        })
    }

    private fun checkMatchScore(apiKey: String, teamId: String) {
        thread {
            try {
                val url = URL("https://api.football-data.org/v4/teams/$teamId/matches?status=IN_PLAY")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("X-Auth-Token", apiKey)
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)
                    val matches = json.getJSONArray("matches")

                    if (matches.length() > 0) {
                        val currentMatch = matches.getJSONObject(0)
                        val score = currentMatch.getJSONObject("score").getJSONObject("fullTime")
                        val homeScore = score.optInt("home", 0)
                        val awayScore = score.optInt("away", 0)

                        val homeTeam = currentMatch.getJSONObject("homeTeam").getString("name")
                        val awayTeam = currentMatch.getJSONObject("awayTeam").getString("name")

                        if (lastHomeScore != -1 && (homeScore > lastHomeScore || awayScore > lastAwayScore)) {
                            handler.post {
                                showGoalBanner("⚽ ¡GOL!\n$homeTeam $homeScore - $awayScore $awayTeam")
                            }
                        }
                        lastHomeScore = homeScore
                        lastAwayScore = awayScore
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showGoalBanner(message: String) {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 50
            y = 50
        }

        val textView = TextView(this).apply {
            text = message
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#E6000000"))
            setPadding(40, 25, 40, 25)
        }

        overlayView = textView
        windowManager.addView(overlayView, params)

        handler.postDelayed({
            if (overlayView != null) {
                windowManager.removeView(overlayView)
                overlayView = null
            }
        }, 8000)
    }
}
