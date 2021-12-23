package com.uwbliao

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.res.ResourcesCompat
import org.apache.commons.math3.distribution.UniformIntegerDistribution

class MainCanvasView(context: Context): View(context) {
    private var infoRefreshHandler: Handler = Handler(Looper.getMainLooper())//for info refreshing
    private val infoRefreshTask =  object : Runnable {
        override fun run() {
            invalidate()
            infoRefreshHandler.postDelayed(this, INFO_REFRESH_RATE)
            refreshInfo()
        }
    }
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColorNear = ResourcesCompat.getColor(resources, R.color.colorBackgroundNear, null)
    private val backgroundColorMid = ResourcesCompat.getColor(resources, R.color.colorBackgroundMid, null)
    private val backgroundColorFar = ResourcesCompat.getColor(resources, R.color.colorBackgroundFar, null)
    private val remoteColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private var centerX = 0f
    private var centerY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var i = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()
        extraCanvas.drawColor(backgroundColorFar)
        redrawBackground()
        infoRefreshHandler.removeCallbacks(infoRefreshTask)
        infoRefreshHandler.post(infoRefreshTask)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
    private fun drawMid() {
        val r = (width+height)/4.toFloat()
        val paint = Paint().apply {
            color = backgroundColorMid
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        extraCanvas.drawCircle(centerX, centerY, r, paint)
    }
    private fun drawNear() {
        val r = (width/2).toFloat()
        val paint = Paint().apply {
            color = backgroundColorNear
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        extraCanvas.drawCircle(centerX, centerY, r, paint)
    }
    private fun drawMe() {
        val r = ME_RADIUS
        val paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        extraCanvas.drawCircle(centerX, centerY, r, paint)
    }
    private fun redrawBackground() {
        drawMid()
        drawNear()
        drawMe()
    }
    private fun drawRemote(i: Int) {
        val paint = Paint().apply {
            color = remoteColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
            textSize = REMOTE_TEXT_SIZE
        }
        extraCanvas.drawText(i.toString(), currentX, currentY, paint)
    }
    private fun refreshInfo() {
//        extraCanvas.drawColor(backgroundColorFar)
//        redrawBackground()
        updateRemoteLocation()
        drawRemote(i++)
    }
    private fun updateRemoteLocation() {
        var dx = UniformIntegerDistribution(20, width-20)
        var dy = UniformIntegerDistribution(20, height-20)
        currentX = dx.sample().toFloat()
        currentY = dy.sample().toFloat()
    }
}