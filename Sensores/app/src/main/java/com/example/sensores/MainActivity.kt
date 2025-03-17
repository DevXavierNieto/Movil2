package com.example.sensores

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensores.ui.theme.SensoresTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val sensorData = MutableStateFlow<Pair<Float, Float>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            SensoresTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(sensorData)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = -event.values[0]
            val ay = event.values[1]
            sensorData.value = Pair(ax, ay)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun GameScreen(sensorData: MutableStateFlow<Pair<Float, Float>?>) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx: Float
    val screenHeightPx: Float
    val ballRadiusPx: Float
    val goalWidth: Float
    val edgeBarrierHeight: Float

    with(density) {
        screenWidthPx = configuration.screenWidthDp.dp.toPx()
        screenHeightPx = configuration.screenHeightDp.dp.toPx()
        ballRadiusPx = 10.dp.toPx()
        goalWidth = 80.dp.toPx()
        edgeBarrierHeight = 60.dp.toPx()
    }

    var xPosition by remember { mutableStateOf(screenWidthPx / 2) }
    var yPosition by remember { mutableStateOf(screenHeightPx / 2) }
    var xVelocity by remember { mutableStateOf(0f) }
    var yVelocity by remember { mutableStateOf(0f) }

    var leftScore by remember { mutableStateOf(0) }
    var rightScore by remember { mutableStateOf(0) }

    val barriers = listOf(
        Rect(0f, 0f, (screenWidthPx - goalWidth) / 2, edgeBarrierHeight),
        Rect((screenWidthPx + goalWidth) / 2, 0f, screenWidthPx, edgeBarrierHeight),
        Rect(0f, screenHeightPx - edgeBarrierHeight, (screenWidthPx - goalWidth) / 2, screenHeightPx),
        Rect((screenWidthPx + goalWidth) / 2, screenHeightPx - edgeBarrierHeight, screenWidthPx, screenHeightPx)
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(sensorData) {
        sensorData.filterNotNull().collectLatest { (ax, ay) ->
            coroutineScope.launch {
                xVelocity += ax * 0.5f
                yVelocity += ay * 0.5f

                xPosition += xVelocity
                yPosition += yVelocity

                val gapLeft = (screenWidthPx - goalWidth) / 2
                val gapRight = (screenWidthPx + goalWidth) / 2

                if (yPosition - ballRadiusPx < 0 && xPosition in gapLeft..gapRight) {
                    leftScore++
                    xPosition = screenWidthPx / 2
                    yPosition = screenHeightPx / 2
                    xVelocity = 0f
                    yVelocity = 0f
                    delay(500L)
                    return@launch
                }
                if (yPosition + ballRadiusPx > screenHeightPx && xPosition in gapLeft..gapRight) {
                    rightScore++
                    xPosition = screenWidthPx / 2
                    yPosition = screenHeightPx / 2
                    xVelocity = 0f
                    yVelocity = 0f
                    delay(500L)
                    return@launch
                }

                val ballBounds = Rect(
                    xPosition - ballRadiusPx, yPosition - ballRadiusPx,
                    xPosition + ballRadiusPx, yPosition + ballRadiusPx
                )

                barriers.forEach { barrier ->
                    if (ballBounds.overlaps(barrier)) {
                        val ballCenter = Offset(xPosition, yPosition)

                        val collisionLeft = ballCenter.x + ballRadiusPx > barrier.left && ballCenter.x < barrier.left
                        val collisionRight = ballCenter.x - ballRadiusPx < barrier.right && ballCenter.x > barrier.right
                        val collisionTop = ballCenter.y + ballRadiusPx > barrier.top && ballCenter.y < barrier.top
                        val collisionBottom = ballCenter.y - ballRadiusPx < barrier.bottom && ballCenter.y > barrier.bottom

                        if (collisionLeft || collisionRight) {
                            xVelocity *= -0.8f
                            xPosition = if (collisionLeft) barrier.left - ballRadiusPx else barrier.right + ballRadiusPx
                        }
                        if (collisionTop || collisionBottom) {
                            yVelocity *= -0.8f
                            yPosition = if (collisionTop) barrier.top - ballRadiusPx else barrier.bottom + ballRadiusPx
                        }
                    }
                }

                if (xPosition - ballRadiusPx < 0) {
                    xVelocity *= -0.8f
                    xPosition = ballRadiusPx
                } else if (xPosition + ballRadiusPx > screenWidthPx) {
                    xVelocity *= -0.8f
                    xPosition = screenWidthPx - ballRadiusPx
                }

                if (yPosition - ballRadiusPx < 0) {
                    if (xPosition !in gapLeft..gapRight) {
                        yVelocity *= -0.8f
                        yPosition = ballRadiusPx
                    }
                } else if (yPosition + ballRadiusPx > screenHeightPx) {
                    if (xPosition !in gapLeft..gapRight) {
                        yVelocity *= -0.8f
                        yPosition = screenHeightPx - ballRadiusPx
                    }
                }

                xVelocity *= 0.98f
                yVelocity *= 0.98f

                delay(16L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Red, ballRadiusPx, Offset(xPosition, yPosition))
            barriers.forEach { barrier ->
                drawRect(
                    Color.Black,
                    Offset(barrier.left, barrier.top),
                    androidx.compose.ui.geometry.Size(barrier.width, barrier.height)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$leftScore",
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                modifier = Modifier.padding(start = 32.dp)
            )
            Text(
                text = "$rightScore",
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                modifier = Modifier.padding(end = 32.dp)
            )
        }
    }
}
