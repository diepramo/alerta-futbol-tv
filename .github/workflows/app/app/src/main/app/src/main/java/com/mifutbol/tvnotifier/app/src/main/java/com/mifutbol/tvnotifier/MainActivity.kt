package com.mifutbol.tvnotifier

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        val apiKeyInput = EditText(this).apply {
            hint = "Introduce tu API Key de Football-Data.org"
            textSize = 18f
        }

        val teamIdInput = EditText(this).apply {
            hint = "ID de tu equipo (ej: 86 para Real Madrid, 81 para Barça)"
            textSize = 18f
        }

        val startButton = Button(this).apply {
            text = "ACTIVAR ALERTAS EN SEGUNDO PLANO"
            textSize = 18f
            setOnClickListener {
                val apiKey = apiKeyInput.text.toString().trim()
                val teamId = teamIdInput.text.toString().trim()

                if (apiKey.isEmpty() || teamId.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    checkOverlayPermission()
                    val serviceIntent = Intent(this@MainActivity, MatchOverlayService::class.java).apply {
                        putExtra("API_KEY", apiKey)
                        putExtra("TEAM_ID", teamId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    Toast.makeText(this@MainActivity, "Servicio de Alertas Activado", Toast.LENGTH_LONG).show()
                }
            }
        }

        layout.addView(apiKeyInput)
        layout.addView(teamIdInput)
        layout.addView(startButton)

        setContentView(layout)
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}
