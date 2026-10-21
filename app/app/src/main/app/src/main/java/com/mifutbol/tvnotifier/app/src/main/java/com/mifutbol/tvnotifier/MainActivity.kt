package com.mifutbol.tvnotifier

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234)
        }

        val prefs = getSharedPreferences("TV_PREFS", Context.MODE_PRIVATE)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val titleText = TextView(this).apply {
            text = "⚙️ Menú de Configuración de Alertas"
            textSize = 22f
            setPadding(0, 0, 0, 20)
            gravity = Gravity.CENTER
        }

        val descText = TextView(this).apply {
            text = "Equipos, Ligas o Deportistas (separados por comas):"
            textSize = 14f
            setPadding(0, 10, 0, 10)
        }

        val savedFavorites = prefs.getString("favorites_list", "real madrid, alcaraz, alonso, zaragoza") ?: ""
        val inputFavorites = EditText(this).apply {
            setText(savedFavorites)
            textSize = 16f
            setPadding(20, 20, 20, 20)
        }

        // Configuración de duración en segundos
        val descDuration = TextView(this).apply {
            text = "Duración del aviso en pantalla (en segundos, ej: 5):"
            textSize = 14f
            setPadding(0, 20, 0, 10)
        }
        val savedDuration = prefs.getString("notification_duration", "5") ?: "5"
        val inputDuration = EditText(this).apply {
            setText(savedDuration)
            textSize = 16f
            setPadding(20, 20, 20, 20)
        }

        // Secciones de Checkboxes para tipos de alertas
        val titleAlerts = TextView(this).apply {
            text = "\nSELECCIONA QUÉ ALERTAS QUIERES RECIBIR:"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }

        val cbGoals = CheckBox(this).apply {
            text = "⚽ Goles y Marcador en vivo"
            isChecked = prefs.getBoolean("alert_goals", true)
        }
        val cbStartEnd = CheckBox(this).apply {
            text = "⏱️ Inicio y Fin de Partido / Carrera / Etapa"
            isChecked = prefs.getBoolean("alert_start_end", true)
        }
        val cbPenalties = CheckBox(this).apply {
            text = "🔴 Penaltis y Incidencias Clave"
            isChecked = prefs.getBoolean("alert_penalties", true)
        }
        val cbTennisSets = CheckBox(this).apply {
            text = "🎾 Fin de Set en Tenis"
            isChecked = prefs.getBoolean("alert_tennis_sets", true)
        }
        val cbWinners = CheckBox(this).apply {
            text = "🏆 Ganadores / Podio en F1 y Ciclismo"
            isChecked = prefs.getBoolean("alert_winners", true)
        }

        val btnSave = Button(this).apply {
            text = "GUARDAR Y ACTIVAR EN ESTA TV"
            textSize = 16f
            setPadding(20, 30, 20, 30)
            setOnClickListener {
                prefs.edit().apply {
                    putString("favorites_list", inputFavorites.text.toString())
                    putString("notification_duration", inputDuration.text.toString())
                    putBoolean("alert_goals", cbGoals.isChecked)
                    putBoolean("alert_start_end", cbStartEnd.isChecked)
                    putBoolean("alert_penalties", cbPenalties.isChecked)
                    putBoolean("alert_tennis_sets", cbTennisSets.isChecked)
                    putBoolean("alert_winners", cbWinners.isChecked)
                    apply()
                }

                val serviceIntent = Intent(this@MainActivity, MatchOverlayService::class.java)
                startService(serviceIntent)

                Toast.makeText(this@MainActivity, "¡Configuración guardada correctamente!", Toast.LENGTH_LONG).show()
            }
        }

        layout.addView(titleText)
        layout.addView(descText)
        layout.addView(inputFavorites)
        layout.addView(descDuration)
        layout.addView(inputDuration)
        layout.addView(titleAlerts)
        layout.addView(cbGoals)
        layout.addView(cbStartEnd)
        layout.addView(cbPenalties)
        layout.addView(cbTennisSets)
        layout.addView(cbWinners)
        layout.addView(btnSave)

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
