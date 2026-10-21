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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de superposición en pantalla (necesario para las alertas en Android TV)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
            gravity = Gravity.CENTER
        }

        val titleText = TextView(this).apply {
            text = "⚙️ Configuración de Alertas en esta TV"
            textSize = 22f
            setPadding(0, 0, 0, 30)
            gravity = Gravity.CENTER
        }

        val descriptionText = TextView(this).apply {
            text = "Escribe tus equipos, ligas o deportistas separados por comas:"
            textSize = 14f
            setPadding(0, 0, 0, 15)
        }

        // Cargar los favoritos guardados previamente en esta tele
        val prefs = getSharedPreferences("TV_PREFS", Context.MODE_PRIVATE)
        val savedFavorites = prefs.getString("favorites_list", "real madrid, alcaraz, alonso") ?: ""

        val inputFavorites = EditText(this).apply {
            setText(savedFavorites)
            textSize = 16f
            setPadding(20, 20, 20, 20)
        }

        val btnSaveAndStart = Button(this).apply {
            text = "GUARDAR Y ACTIVAR ALERTAS"
            textSize = 16f
            setPadding(20, 20, 20, 20)
            setOnClickListener {
                val newFavorites = inputFavorites.text.toString()
                
                // Guardar en la memoria local de esta televisión
                prefs.edit().putString("favorites_list", newFavorites).apply()

                // Iniciar el servicio en segundo plano
                val serviceIntent = Intent(this@MainActivity, MatchOverlayService::class.java)
                startService(serviceIntent)

                Toast.makeText(this@MainActivity, "¡Guardado e iniciado en esta TV!", Toast.LENGTH_LONG).show()
            }
        }

        layout.addView(titleText)
        layout.addView(descriptionText)
        layout.addView(inputFavorites)
        layout.addView(btnSaveAndStart)
        setContentView(layout)
    }
}
