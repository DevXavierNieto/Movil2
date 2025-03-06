package com.example.telefonia

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.telefonia.ui.theme.TelefoniaTheme

class MainActivity : ComponentActivity() {
    private lateinit var callReceiver: CallReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        // Registrar dinámicamente el BroadcastReceiver
        callReceiver = CallReceiver()
        val filter = IntentFilter("android.intent.action.PHONE_STATE")
        registerReceiver(callReceiver, filter)

        setContent {
            TelefoniaTheme {
                MainScreen(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callReceiver) // Evitar fugas de memoria
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 1)
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    var phoneNumber by remember { mutableStateOf(sharedPref.getString("phone_number", "") ?: "") }
    var smsMessage by remember { mutableStateOf(sharedPref.getString("sms_message", "") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = smsMessage,
            onValueChange = { smsMessage = it },
            label = { Text("Mensaje de respuesta automática") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("phone_number", phoneNumber)
                editor.putString("sms_message", smsMessage)
                editor.apply()
                Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
