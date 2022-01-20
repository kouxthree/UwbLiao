package com.uwbliao

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import com.uwbliao.databinding.RemoteDevDspBinding
import com.uwbliao.db.EntityDevice
import org.apache.commons.math3.distribution.UniformIntegerDistribution
import kotlin.math.cos
import kotlin.math.sin

class MainCanvasView(context: Context): View(context) {
    private lateinit var bitmapCompass: Bitmap
    init {
        initCompass()
    }
    private fun initCompass() {
        bitmapCompass = BitmapFactory.decodeResource(resources, R.drawable.compass_needle)
        bitmapCompass = Bitmap.createScaledBitmap(
            bitmapCompass, bitmapCompass.width/3, bitmapCompass.height/3, true
        )
    }
    private lateinit var dirSensor: DirSensor
    private var remoteDevs = mutableListOf<EntityDevice>()
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initRemoteDevs()
    }
    fun initRemoteDevs() {
        //direction sensor
        dirSensor = DirSensor(context)
        //remote dev init/using view width,height
        remoteDevs = mutableListOf()
        for(i in 0 until SettingActivity.scanRemoteNums) {
            val dev = EntityDevice()
            remoteDevs.add(dev)
            dev.deviceName = i.toString()
        }
        if(remoteDevs.size > 0) remoteDevs[0].nickname = "西施"
        if(remoteDevs.size > 2) remoteDevs[2].nickname = "貂蝉"
    }
    private var infoRefreshHandler: Handler = Handler(Looper.getMainLooper())//for info refreshing
    private var infoRefreshTask = object : Runnable {
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
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val remoteColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private var centerX = 0f
    private var centerY = 0f
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()
        extraCanvas.drawColor(backgroundColor)
        redrawBackground()
        infoRefreshHandler.removeCallbacks(infoRefreshTask)
        infoRefreshHandler.post(infoRefreshTask)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                var tappedI: Int? = null
                var preD = TAP_ACCURACY.toDouble()
                for(i in 0 until SettingActivity.scanRemoteNums) {
                    val d = Utils.distanceBetweenPoints(
                        remoteDevs[i].currentx.toInt(), remoteDevs[i].currenty.toInt(),
                        x, y)
                    if(d <= preD) {
                        if(tappedI == null) tappedI = i
                        else if(d <= preD) tappedI = i
                        preD = d
                    }
                }
                if(tappedI != null) displayRemoteDevice(tappedI)
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        when (visibility) {
            VISIBLE -> dirSensor.resume()
            INVISIBLE, GONE -> dirSensor.pause()
        }
    }
    //display remote device dialog
    private fun displayRemoteDevice(idx: Int) {
        val dsp = AlertDialog.Builder(context)
        //LayoutInflater.from(context).inflate(R.layout.remote_dev_dsp, null, false)
        val mBinding = RemoteDevDspBinding.inflate(LayoutInflater.from(context))
        dsp.setView(mBinding.root.rootView)
        val dlg = dsp.create()
        dlg.setCanceledOnTouchOutside(true)
        (idx.toString() + ":" + remoteDevs[idx].nickname.toString()).also { mBinding.txtNickname.text = it }
        (Utils.realDistanceFromCoordinateDistance(remoteDevs[idx].distance, myDistanceRatio())
            .formatDecimalPoint1() + "m").also { mBinding.txtDistance.text = it }
        //cancel button
        mBinding.btnCancel.setOnClickListener {
            dlg.dismiss()
        }
        //okay button
        mBinding.btnOkay.setOnClickListener {
            dlg.dismiss()
        }
        //text send action// combine with xml setting -> android:imeOptions="actionSend"
        mBinding.txtNickname.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                dlg.dismiss()
                handled = true
            }
            handled
        }
        dlg.show()
    }
    private val distancePaint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.distancePaint, null)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
        textSize = DISTANCE_TEXT_SIZE
    }
    private fun drawCompass() {
        val mymatrix = Matrix()
        mymatrix.postRotate(DirSensor.orientAngel)
        mymatrix.postTranslate(width-20-bitmapCompass.width.toFloat(), 20f)
        extraCanvas.drawBitmap(bitmapCompass, mymatrix, null)
    }
    private fun drawFar() {
        val r = (width*3f)/4f
        val paint = Paint().apply {
            color = backgroundColorFar
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        extraCanvas.drawCircle(centerX, centerY, r, paint)
        //distance mark
        val x = width/2f
        extraCanvas.drawText(FAR_DISTANCE.toString()+"m", x, centerY-r, distancePaint)
    }
    private fun drawMid() {
        val r = width/2f
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
        //distance mark
        val x = width/2f
        extraCanvas.drawText(MID_DISTANCE.toString()+"m", x, centerY-r, distancePaint)
    }
    private fun drawNear() {
        val r = width/4f
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
        //distance mark
        val x = width/2f
        extraCanvas.drawText(NEAR_DISTANCE.toString()+"m", x, centerY-r, distancePaint)
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
        //me
        paint.color = ResourcesCompat.getColor(resources, R.color.distancePaint, null)
        extraCanvas.drawText(context.getString(R.string.txt_me), centerX, centerY+5, distancePaint)
    }
    private fun redrawBackground() {
        drawFar()
        drawMid()
        drawNear()
        drawMe()
        drawCompass()
    }
    private fun drawRemote(i: Int, remoteDev: EntityDevice) {
        val theta = remoteDev.theta + Math.PI/2 - DirSensor.orientAngel
        remoteDev.currentx = centerX + remoteDev.distance!!*cos(theta).toFloat()
        remoteDev.currenty = centerY + remoteDev.distance!!*sin(theta).toFloat()
         //text
        var paint = Paint().apply {
            color = remoteColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
            textSize = REMOTE_TEXT_SIZE
        }
        extraCanvas.drawText(i.toString(), remoteDev.currentx, remoteDev.currenty, paint)
        //circle
        val r = REMOTE_RADIUS
        paint = Paint().apply {
            color = remoteColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        extraCanvas.drawCircle(remoteDev.currentx, remoteDev.currenty, r, paint)
    }
    private fun refreshInfo() {
        redrawBackground()
        for(i in 0..1) {
            if(i > remoteDevs.size-1) break
            updateRemoteLocation(remoteDevs[i])
            drawRemote(i, remoteDevs[i])
        }
        for(i in 2 until SettingActivity.scanRemoteNums) {
            if(i > remoteDevs.size-1) break
            if(remoteDevs[i].distance == null) updateRemoteLocation(remoteDevs[i])
            drawRemote(i, remoteDevs[i])
        }
    }
    private fun updateRemoteLocation(remoteDev: EntityDevice) {
//        val dx = UniformIntegerDistribution(20, width-20)
//        val dy = UniformIntegerDistribution(20, height-20)
//        remoteDev.currentx = dx.sample().toFloat()
//        remoteDev.currenty = dy.sample().toFloat()
//        val b = Random.nextFloat()
//        val theta = (2*Math.PI)/ 2.toDouble().pow(b.toDouble())//[0,2pi)
        val theta = Math.random() * 2*Math.PI//[0,2pi) random
        val r = UniformIntegerDistribution(20, width*3/4-20).sample()
        remoteDev.theta = theta
        remoteDev.distance = r
        remoteDev.currentx = centerX + r* cos(theta).toFloat()
        remoteDev.currenty = centerY + r* sin(theta).toFloat()
    }
    //utils
    private fun myDistanceRatio(): Float {
        return Utils.realDistanceRatio(MID_DISTANCE, width/2)
    }
}