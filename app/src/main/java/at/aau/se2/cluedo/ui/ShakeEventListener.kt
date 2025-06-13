package at.aau.se2.cluedo.ui

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeEventListener : SensorEventListener {
    private var shakeTimestamp: Long = 0
    private var shakeListener: (() -> Unit)? = null

    fun setOnShakeListener(listener: () -> Unit) {
        shakeListener = listener
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > 1.5f) {
            val now = System.currentTimeMillis()
            if (shakeTimestamp + 1000 > now) return
            shakeTimestamp = now
            shakeListener?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}