package com.uwbliao

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.properties.Delegates

class DirSensor(context: Context): SensorEventListener {
    private val AXIS_NUM = 3
    private val MATRIX_SIZE = 16
    private var mManager: SensorManager by Delegates.notNull()
    private var mSensorAcc: Sensor by Delegates.notNull()
    private var mSensorMag: Sensor by Delegates.notNull()
    private var accelerometerValues = FloatArray(AXIS_NUM)
    private var magneticValues = FloatArray(AXIS_NUM)
    private var savedAcceleVal = FloatArray(AXIS_NUM)
    init {
        mManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAcc = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorMag = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        when (event!!.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values.clone()
                lowPassFilter(accelerometerValues)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> magneticValues = event.values.clone()
        }
        orientAngel = getAzimuthAngle(
            accelerometerValues,
            magneticValues
        )
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    fun pause() {
        mManager.unregisterListener(this)
    }
    fun resume() {
        mManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI)
        mManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_UI)
    }

    private fun lowPassFilter(target: FloatArray) {
        val FILTER_VAL = 0.8f
        val outVal = FloatArray(AXIS_NUM)
        outVal[0] = (savedAcceleVal[0] * FILTER_VAL
                + target[0] * (1 - FILTER_VAL))
        outVal[1] = (savedAcceleVal[1] * FILTER_VAL
                + target[1] * (1 - FILTER_VAL))
        outVal[2] = (savedAcceleVal[2] * FILTER_VAL
                + target[2] * (1 - FILTER_VAL))
        savedAcceleVal = target.clone()//for next computation
        accelerometerValues = outVal.clone()//write
    }
    private fun getAzimuthAngle(accValues: FloatArray, magValues: FloatArray): Float {
        val rotationMatrix = FloatArray(MATRIX_SIZE)
        val inclinationMatrix = FloatArray(MATRIX_SIZE)
        val remappedMatrix = FloatArray(MATRIX_SIZE)
        val orientationValues = FloatArray(AXIS_NUM)

        SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix, accValues, magValues
        )
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix
        )
        SensorManager.getOrientation(remappedMatrix, orientationValues)
        //return toOrientationDegrees(orientationValues[0])
        return orientationValues[0]
    }
    /*
    private fun toOrientationDegrees(angle: Float): Int {
        return Math.floor(
            if (angle >= 0) Math.toDegrees(angle.toDouble()) else 360 + Math.toDegrees(
                angle.toDouble()
            )
        ).toInt()
    }*/

    companion object {
        private val TAG = DirSensor::class.java.simpleName
        var orientAngel: Float = 0f
    }
}
