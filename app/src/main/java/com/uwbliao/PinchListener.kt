package com.uwbliao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.Toast

// bitmap pinch zoom gesture listener
class PinchListener(context: Context?, srcBitmap: Bitmap?, srcCanvas: Canvas?) :
    ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private var context: Context? = null
    private var srcBitmap: Bitmap? = null
    private var srcCanvas: Canvas? = null

    // When pinch zoom gesture occurred.
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (detector == null) {
            Log.e(TAG, "Pinch listener onScale detector parameter is null.")
            return true
        }
        if(context == null || srcBitmap == null || srcCanvas == null) {
            Toast.makeText(context, "\"context or srcBitmap or extraCanvas is null\"", Toast.LENGTH_SHORT).show()
            return true
        }
        val scaleFactor = detector.scaleFactor
        scaleImage(scaleFactor, scaleFactor)
        return true
    }

    //scale the image
    private fun scaleImage(xScale: Float, yScale: Float) {
//        //image width and height.
//        val srcImageWith = srcBitmap!!.width
//        val srcImageHeight = srcBitmap!!.height
//        //image config object.
//        val srcImageConfig = srcBitmap!!.config
//        // Create a new bitmap which has scaled width and height value from source bitmap.
//        val scaleBitmap = Bitmap.createBitmap(
//            (srcImageWith * xScale).toInt(),
//            (srcImageHeight * yScale).toInt(), srcImageConfig
//        )
//        // Create the scaled canvas.
//        val scaleCanvas = Canvas(scaleBitmap)
        // Create the Matrix object which will scale the source bitmap to target.
        val scaleMatrix = Matrix()
        // Set x y scale value.
        scaleMatrix.setScale(xScale, yScale)
        scale = (xScale + yScale)
        // Create a new paint object.
        val paint = Paint()
//        // Draw the new scaled bitmap in the canvas.
//        scaleCanvas.drawBitmap(srcBitmap!!, scaleMatrix, paint)
        srcCanvas!!.drawBitmap(srcBitmap!!, scaleMatrix, paint)
    }

    companion object {
        private val TAG = PinchListener::class.java.simpleName
        var scale = 0f
    }

   init {
        this.context = context
        this.srcBitmap = srcBitmap
        this.srcCanvas = srcCanvas
    }
}