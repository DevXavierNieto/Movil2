package com.example.mapas_geolocalizacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var homeAddress by mutableStateOf<LatLng?>(null)
    private var homeAddressText by mutableStateOf("")
    private var showDialog by mutableStateOf(false)
    private var routePolyline: Polyline? = null
    private var currentLocationMarker: Marker? = null
    private var homeMarker: Marker? = null

    // Configuración para la API de Directions
    private val geoApiContext by lazy {
        GeoApiContext.Builder()
            .apiKey("AIzaSyDrpvdKB9qrOpudtg8Y3V9sdK6vPo3GJZw")
            .build()
    }

    // Solicitud de permisos de ubicación
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar dirección inicial
        homeAddressText = "Universidad 78, La Joyita, 38983 Uriangato, Gto."
        homeAddress = LatLng(20.118726824911494, -101.1707240721873)

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar MapView
        mapView = MapView(this).apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MainActivity)
        }

        setContent {
            MapsApp()
        }

        // Verificar y solicitar permisos
        checkLocationPermissions()
    }

    @Composable
    fun MapsApp() {
        val context = LocalContext.current

        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mapa (ocupa el 85% de la pantalla)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.85f)
                    ) {
                        MapScreen()
                    }

                    // Controles (ocupan el 15% restante)
                    ControlsSection(
                        context = context,
                        homeAddress = homeAddressText,
                        homeLatLng = homeAddress,
                        onAddressChange = { homeAddressText = it },
                        onSetHomeAddress = {
                            setHomeAddressFromText(context, homeAddressText)
                            showDialog = false
                        },
                        onShowDialog = { showDialog = true }
                    )

                    // Dialogo para configurar dirección
                    if (showDialog) {
                        AddressDialog(
                            currentAddress = homeAddressText,
                            onAddressChange = { homeAddressText = it },
                            onConfirm = {
                                setHomeAddressFromText(context, homeAddressText)
                                showDialog = false
                            },
                            onDismiss = { showDialog = false }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MapScreen() {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun ControlsSection(
        context: Context,
        homeAddress: String,
        homeLatLng: LatLng?,
        onAddressChange: (String) -> Unit,
        onSetHomeAddress: () -> Unit,
        onShowDialog: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para configurar dirección de casa
            Button(
                onClick = onShowDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar dirección de casa")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar dirección actual configurada
            if (homeAddress.isNotEmpty()) {
                Text(
                    text = "Dirección actual: $homeAddress",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Botón para recalcular ruta
            Button(
                onClick = {
                    if (homeLatLng != null) {
                        // Obtener ubicación actual antes de recalcular
                        getCurrentLocation { current ->
                            calculateRoute(current, homeLatLng)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Configura una dirección primero",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = homeLatLng != null
            ) {
                Text("Recalcular Ruta")
            }
        }
    }

    @Composable
    fun AddressDialog(
        currentAddress: String,
        onAddressChange: (String) -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Configurar dirección destino") },
            text = {
                Column {
                    Text("Dirección por defecto: Universidad 78")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ingresa una nueva dirección completa:")
                    OutlinedTextField(
                        value = currentAddress,
                        onValueChange = onAddressChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Av. Siempre Viva 123, Ciudad, Estado") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Usar esta dirección")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onAddressChange("Universidad 78, La Joyita, 38983 Uriangato, Gto.")
                        onDismiss()
                    }
                ) {
                    Text("Usar Universidad 78")
                }
            }
        )
    }

    private fun setHomeAddressFromText(context: Context, address: String) {
        if (address.isNotEmpty()) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)
                addresses?.firstOrNull()?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    homeAddress = latLng
                    updateMapWithHomeAddress(latLng)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al convertir la dirección: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateMapWithHomeAddress(latLng: LatLng) {
        // icono personalizado para la casa
        val homeBitmap = getBitmapFromVector(R.drawable.ic_home)
        val homeIcon = homeBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }

        homeMarker?.remove()
        homeMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(if (latLng == LatLng(20.118726824911494, -101.1707240721873)) "Universidad 78" else "Mi destino")
                .icon(homeIcon)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Calcular ruta desde la ubicación actual
        getCurrentLocation { current ->
            calculateRoute(current, latLng)
        }
    }

    private fun getBitmapFromVector(vectorResId: Int): Bitmap? {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, vectorResId, null) ?: return null
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Mostrar dirección inicial
        homeAddress?.let {
            updateMapWithHomeAddress(it)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(onSuccess: ((LatLng) -> Unit)? = null) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        updateCurrentLocationMarker(currentLatLng)
                        onSuccess?.invoke(currentLatLng)
                    } ?: run {
                        Toast.makeText(
                            this,
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun updateCurrentLocationMarker(location: LatLng) {
        // Crear icono personalizado para la ubicación actual
        val locationBitmap = getBitmapFromVector(R.drawable.ic_my_location) // Asegúrate de tener este drawable
        val locationIcon = locationBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }

        currentLocationMarker?.remove() // Eliminar marcador anterior
        currentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Mi ubicación")
                .icon(locationIcon)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun calculateRoute(origin: LatLng, destination: LatLng) {
        val destinationName = if (destination.latitude == 20.118726824911494 &&
            destination.longitude == -101.1707240721873) {
            "Universidad 78"
        } else {
            "Mi destino"
        }
        Toast.makeText(this, "Calculando ruta a $destinationName...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .await()

                runOnUiThread {
                    routePolyline?.remove()

                    val route = directionsResult.routes.firstOrNull() ?: run {
                        Toast.makeText(this@MainActivity, "No se encontró ruta", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    val polylineOptions = PolylineOptions().apply {
                        addAll(route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) })
                        width(12f)
                        color(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark))
                    }

                    routePolyline = mMap.addPolyline(polylineOptions)

                    // Ajustar cámara para mostrar toda la ruta
                    val bounds = LatLngBounds.builder()
                        .include(origin)
                        .include(destination)
                        .build()

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message ?: "No se pudo calcular la ruta"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}