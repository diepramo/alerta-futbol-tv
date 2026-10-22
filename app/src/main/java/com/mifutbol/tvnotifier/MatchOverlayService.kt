package com.mifutbol.tvnotifier

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class MatchOverlayService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Servicio en segundo plano para alertas flotantes universal
        Toast.makeText(this, "Servicio de Alertas de Fútbol Activo", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }
}
