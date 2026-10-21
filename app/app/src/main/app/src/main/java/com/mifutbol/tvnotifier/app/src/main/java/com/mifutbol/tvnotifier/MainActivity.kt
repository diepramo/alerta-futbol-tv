package com.mifutbol.tvnotifier

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Diccionario de sugerencias rápidas para autocompletar en Android TV
    private val sportsSuggestions = arrayOf(
        // Fútbol - Equipos y Jugadores
        "Real Madrid (Fútbol)", "FC Barcelona (Fútbol)", "Atlético de Madrid (Fútbol)", 
        "Sevilla FC (Fútbol)", "Real Betis (Fútbol)", "Athletic Club (Fútbol)",
        "Girona FC (Fútbol)", "Real Zaragoza (Fútbol)", "Manchester City (Fútbol)",
        "Liverpool (Fútbol)", "PSG (Fútbol)", "Bayern Munich (Fútbol)",
        "Kylian Mbappé (Fútbol)", "Vinicius Jr (Fútbol)", "Lamine Yamal (Fútbol)",
        "Jude Bellingham (Fútbol)", "Cristiano Ronaldo (Fútbol)", "Lionel Messi (Fútbol)",
        
        // Tenis
        "Carlos Alcaraz (Tenis)", "Rafael Nadal (Tenis)", "Jannik Sinner (Tenis)",
        "Novak Djokovic (Tenis)", "Alexander Zverev (Tenis)", "Daniil Medvedev (Tenis)",
        
        // Motor / F1 / MotoGP
        "Fernando Alonso (F1)", "Carlos Sainz (F1)", "Max Verstappen (F1)",
        "Lewis Hamilton (F1)", "Marc Márquez (MotoGP)", "Jorge Martín (MotoGP)",
        
        // Ciclismo
        "Tadej Pogačar (Ciclismo)", "Jonas Vingegaard (Ciclismo)", "Remco Evenepoel (Ciclismo)",
        "Enric Mas (Ciclismo)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Comprobar permiso de ventana flotante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        setContentView(R.layout.activity_main)

        val autoSearch = findViewById<AutoCompleteTextView>(R.id.autoCompleteFavorites)
        val editDuration = findViewById<EditText>(R.id.editDuration)
        
        val chkGoals = findViewById<CheckBox>(R.id.chkGoals)
        val chkStartEnd = findViewById<CheckBox>(R.id.chkStartEnd)
        val chkPenalties = findViewById<CheckBox>(R.id.chkPenalties)
        val chkTennisSets = findViewById<CheckBox>(R.id.chkTennisSets)
        val chkWinners = findViewById<CheckBox>(R.id.chkWinners)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Configurar adaptador de autocompletado inteligente
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sportsSuggestions)
        autoSearch.setAdapter(adapter)
        autoSearch.threshold = 1 // Empieza a sugerir desde la primera letra

        val prefs = getSharedPreferences("TV_PREFS", Context.MODE_PRIVATE)
        autoSearch.setText(prefs.getString("favorites_list", "Real Madrid, Carlos Alcaraz, Fernando Alonso"))
        editDuration.setText(prefs.getString("notification_duration", "5"))

        chkGoals.isChecked = prefs.getBoolean("alert_goals", true)
        chkStartEnd.isChecked = prefs.getBoolean("alert_start_end", true)
        chkPenalties.isChecked = prefs.getBoolean("alert_penalties", true)
        chkTennisSets.isChecked = prefs.getBoolean("alert_tennis_sets", true)
        chkWinners.isChecked = prefs.getBoolean("alert_winners", true)

        btnSave.setOnClickListener {
            val rawInput = autoSearch.text.toString()
            
            // Limpiar la etiqueta del deporte antes de guardar (ej. "Carlos Alcaraz (Tenis)" -> "Carlos Alcaraz")
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

            // Iniciar o actualizar el servicio
            val serviceIntent = Intent(this, MatchOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            Toast.makeText(this, "Configuración guardada activada", Toast.LENGTH_SHORT).show()
        }
    }
}
