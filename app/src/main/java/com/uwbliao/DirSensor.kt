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
    private var haveGravity = false
    private var haveRotationVectorSensor = false
    private var mRotationVectorSensor: Sensor by Delegates.notNull()
    private var mSensorGravity: Sensor by Delegates.notNull()
    private var mSensorAcc: Sensor by Delegates.notNull()
    private var mSensorMag: Sensor by Delegates.notNull()
    private var rotationVectorValues = FloatArray(AXIS_NUM)
    private var gValues = FloatArray(AXIS_NUM)
    private var magneticValues = FloatArray(AXIS_NUM)
    private var savedAcceleVal = FloatArray(AXIS_NUM)
//    private var _orientAngel = MutableLiveData<Float>().apply { value = 0f }
//    var orientAngel: LiveData<Float> = _orientAngel
    init {
        mManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mRotationVectorSensor = mManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        mSensorGravity = mManager.getDefaultSensor( Sensor.TYPE_GRAVITY );
        mSensorAcc = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorMag = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if(haveRotationVectorSensor && event!!.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            rotationVectorValues = event.values.clone()
            orientAngel = getAzimuthAngle(rotationVectorValues)
        } else {
            when (event!!.sensor.type) {
                Sensor.TYPE_GRAVITY -> gValues = event.values.clone()
                Sensor.TYPE_ACCELEROMETER -> {
                    gValues = event.values.clone()
                    lowPassFilter(gValues)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> magneticValues = event.values.clone()
            }
            // _orientAngel.apply { value = getAzimuthAngle(
            // accelerometerValues,
            // magneticValues
            // )}
            orientAngel = getAzimuthAngle(
                gValues,
                magneticValues
            )
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    fun pause() {
        mManager.unregisterListener(this)
    }
    fun resume() {
        //use RotationVectorSensor if available
        haveRotationVectorSensor = mManager.registerListener(this, mRotationVectorSensor, 10000);
        if(!haveRotationVectorSensor) {
            haveGravity =
                mManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_GAME);
            if (!haveGravity) mManager.registerListener(
                this,
                mSensorAcc,
                SensorManager.SENSOR_DELAY_UI
            )
            mManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_UI)
        }
    }

    //use rotate vector sensor when available
    private fun getAzimuthAngle(rValues: FloatArray): Float {
        val rotationMatrix = FloatArray(MATRIX_SIZE)
        val orientationValues = FloatArray(AXIS_NUM)
        // calculate th rotation matrix
        SensorManager.getRotationMatrixFromVector( rotationMatrix, rValues)
        // get the azimuth value (orientation[0])
        //mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
        SensorManager.getOrientation( rotationMatrix, orientationValues )
        return orientationValues[0]
    }
    //when gravity sensor is available, acceleration sensor is not needed
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
        gValues = outVal.clone()//write
    }
    private fun getAzimuthAngle(gValues: FloatArray, magValues: FloatArray): Float {
        val rotationMatrix = FloatArray(MATRIX_SIZE)
        val inclinationMatrix = FloatArray(MATRIX_SIZE)
        val remappedMatrix = FloatArray(MATRIX_SIZE)
        val orientationValues = FloatArray(AXIS_NUM)

        SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix, gValues, magValues
        )
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix
        )
        SensorManager.getOrientation(remappedMatrix, orientationValues)
        //return toOrientationDegrees(orientationValues[0])
        return orientationValues[0]
    }

    companion object {
        private val TAG = DirSensor::class.java.simpleName
        var orientAngel = 0f
    }
}
