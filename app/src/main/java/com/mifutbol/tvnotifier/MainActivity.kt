package com.mifutbol.tvnotifier

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Usamos EditText normal para evitar cierres forzosos (crashes) en Android TV
        val editFavorites = findViewById<EditText>(R.id.editFavorites)
        val editDuration = findViewById<EditText>(R.id.editDuration)
        
        val chkGoals = findViewById<CheckBox>(R.id.chkGoals)
        val chkStartEnd = findViewById<CheckBox>(R.id.chkStartEnd)
        val chkPenalties = findViewById<CheckBox>(R.id.chkPenalties)
        val chkTennisSets = findViewById<CheckBox>(R.id.chkTennisSets)
        val chkWinners = findViewById<CheckBox>(R.id.chkWinners)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val prefs = getSharedPreferences("TV_PREFS", Context.MODE_PRIVATE)
        editFavorites.setText(prefs.getString("favorites_list", "Real Madrid, Carlos Alcaraz, Fernando Alonso"))
        editDuration.setText(prefs.getString("notification_duration", "5"))

        chkGoals.isChecked = prefs.getBoolean("alert_goals", true)
        chkStartEnd.isChecked = prefs.getBoolean("alert_start_end", true)
        chkPenalties.isChecked = prefs.getBoolean("alert_penalties", true)
        chkTennisSets.isChecked = prefs.getBoolean("alert_tennis_sets", true)
        chkWinners.isChecked = prefs.getBoolean("alert_winners", true)

        btnSave.setOnClickListener {
            val rawInput = editFavorites.text.toString()
            val cleanFavorites = rawInput.split(",")
                .map { item -> item.replace(Regex("\\s*\\([^)]*\\)"), "").trim() }
                .filter { it.isNotEmpty() }
                .joinToString(", ")

            val durationInput = editDuration.text.toString().ifEmpty { "5" }

            prefs.edit().apply {
                putString("favorites_list", cleanFavorites)
                putString("notification_duration", durationInput)
                putBoolean("alert_goals", chkGoals.isChecked)
                putBoolean("alert_start_end", chkStartEnd.isChecked)
                putBoolean("alert_penalties", chkPenalties.isChecked)
                putBoolean("alert_tennis_sets", chkTennisSets.isChecked)
                putBoolean("alert_winners", chkWinners.isChecked)
                apply()
            }

            // Iniciar servicio flotante de forma segura
            val serviceIntent = Intent(this, MatchOverlayService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "Guardado y Activado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
