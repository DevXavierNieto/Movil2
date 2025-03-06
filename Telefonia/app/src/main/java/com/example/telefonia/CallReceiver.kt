package com.example.telefonia

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                // Verifica si el permiso READ_CALL_LOG está concedido antes de intentar leer el número
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                    if (!incomingNumber.isNullOrEmpty()) {
                        Toast.makeText(context, "Llamada entrante de: $incomingNumber", Toast.LENGTH_LONG).show()

                        // Verificar si se debe enviar SMS
                        sendAutoReplySMS(context, incomingNumber)
                    } else {
                        Toast.makeText(context, "Llamada entrante de: Número desconocido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Permiso de llamadas no concedido", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error en CallReceiver: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendAutoReplySMS(context: Context, phoneNumber: String) {
        try {
            // Verificar si el permiso SEND_SMS está concedido
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "No tienes permiso para enviar SMS", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener el mensaje guardado en SharedPreferences
            val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val savedNumber = sharedPref.getString("phone_number", "")
            val message = sharedPref.getString("sms_message", "Estoy ocupado, te llamo luego.")

            // Verificar si el número coincide con el guardado
            if (phoneNumber == savedNumber) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message ?: "Mensaje automático.", null, null)
                Toast.makeText(context, "SMS enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al enviar SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
