package com.UniaccStressMonitor.presentation.sensors

import android.app.*
import android.util.Log
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.UniaccStressMonitor.R
import com.UniaccStressMonitor.data.remote.AuthService
import com.UniaccStressMonitor.domain.model.StressSession
import com.UniaccStressMonitor.domain.usecase.DetectStressUseCase
import com.UniaccStressMonitor.domain.usecase.SaveStressSessionUseCase
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SensorDataCollector : Service(), SensorEventListener {

    private val detectStressUseCase: DetectStressUseCase by inject()
    private val saveStressSessionUseCase: SaveStressSessionUseCase by inject()
    private val authService: AuthService by inject()
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Buffers for 30 seconds (~150 samples at 200ms delay)
    private val accelX = mutableListOf<Float>()
    private val accelY = mutableListOf<Float>()
    private val accelZ = mutableListOf<Float>()
    private val gyroX = mutableListOf<Float>()
    private val gyroY = mutableListOf<Float>()
    private val gyroZ = mutableListOf<Float>()

    private val WINDOW_SIZE = 150 

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UniaccStressMonitor")
            .setContentText("Monitoreando niveles de estrés...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        registerSensors()
        return START_STICKY
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private val TAG = "SensorDataCollector"

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelX.add(event.values[0])
                accelY.add(event.values[1])
                accelZ.add(event.values[2])
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroX.add(event.values[0])
                gyroY.add(event.values[1])
                gyroZ.add(event.values[2])
            }
        }

        if (accelX.size % 50 == 0) {
            Log.d(TAG, "Buffer status: Accel size=${accelX.size}, Gyro size=${gyroX.size}")
        }

        if (accelX.size >= WINDOW_SIZE && gyroX.size >= WINDOW_SIZE) {
            Log.d(TAG, "Window full ($WINDOW_SIZE samples). Processing...")
            processWindow()
        }
    }

    private fun processWindow() {
        val aX = accelX.toFloatArray()
        val aY = accelY.toFloatArray()
        val aZ = accelZ.toFloatArray()
        val gX = gyroX.toFloatArray()
        val gY = gyroY.toFloatArray()
        val gZ = gyroZ.toFloatArray()

        clearBuffers()

        serviceScope.launch {
            val userId = authService.getCurrentUserId() ?: "anonymous"
            val level = detectStressUseCase(aX, aY, aZ, gX, gY, gZ)
            Log.d(TAG, "Stress level detected: $level for user: $userId")
            saveStressSessionUseCase(StressSession(userId = userId, stressLevel = level))
            Log.d(TAG, "Session saved to local database")
        }
    }

    private fun clearBuffers() {
        accelX.clear(); accelY.clear(); accelZ.clear()
        gyroX.clear(); gyroY.clear(); gyroZ.clear()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stress Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "StressMonitorChannel"
        private const val NOTIFICATION_ID = 1
    }
}
