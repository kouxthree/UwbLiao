package com.uwbliao

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.uwbliao.db.EntityDevice
import org.apache.commons.math3.distribution.UniformIntegerDistribution
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uwbliao.databinding.RemoteDevDspBinding
import com.uwbliao.db.Gender
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

class MainCanvasView(context: Context): View(context) {
    private var remoteDevs = mutableListOf<EntityDevice>()
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initRemoteDevs()
    }
    fun initRemoteDevs() {
        //remote dev init/using view width,height
//        for(i in 0..SettingActivity.scanRemoteNums) {
        for(i in 0 until 9) {
            var dev = EntityDevice()
            remoteDevs.add(dev)
            dev.deviceName = i.toString()
            updateRemoteLocation(dev)
        }
        remoteDevs[0].nickname = "貂蝉"
        remoteDevs[3].nickname = "西施"
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
        extraCanvas.drawColor(backgroundColorFar)
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
        var x1 = x - TAP_ACCURACY
        var x2 = x + TAP_ACCURACY
        var y1 = y - TAP_ACCURACY
        var y2 = y + TAP_ACCURACY
        if(x1<0) x1 = 0
        if(y1<0) y1 = 0
        if(x2>width) x1 = width
        if(y2>height) y2 = height
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for(i in 0 until SettingActivity.scanRemoteNums) {
                    if(x1<=remoteDevs[i].currentx && remoteDevs[i].currentx<=x2
                        && y1<=remoteDevs[i].currenty && remoteDevs[i].currentx<=y2) {
                        displayRemoteDevice(i)
                        break
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }
    //display remote device dialog
    private fun displayRemoteDevice(idx: Int) {
        val dsp = AlertDialog.Builder(context)
        //LayoutInflater.from(context).inflate(R.layout.remote_dev_dsp, null, false)
        val mBinding = RemoteDevDspBinding.inflate(LayoutInflater.from(context))
        dsp.setView(mBinding.root.rootView)
        val dlg = dsp.create()
        dlg.setCanceledOnTouchOutside(true)
        mBinding.txtNickname.setText(remoteDevs[idx].nickname.toString())
        mBinding.txtDistance.setText(remoteDevs[idx].distance.toString())
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
    private fun drawMid() {
        val r = (width)/2.toFloat()
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
        val x = (width)/2.toFloat()
        extraCanvas.drawText(MID_DISTANCE.toString()+"m", x, centerY-r, distancePaint)
//        extraCanvas.drawText(MID_DISTANCE.toString()+"m", x, centerY+r, distancePaint)
    }
    private fun drawNear() {
        val r = (width/4).toFloat()
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
        val x = (width)/2.toFloat()
        extraCanvas.drawText(NEAR_DISTANCE.toString()+"m", x, centerY-r, distancePaint)
//        extraCanvas.drawText(NEAR_DISTANCE.toString()+"m", x, centerY+r, distancePaint)
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
    private fun drawRemote(i: Int, remoteDev: EntityDevice) {
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
        extraCanvas.drawText(i.toString(), remoteDev.currentx, remoteDev.currenty, paint)
    }
    private fun refreshInfo() {
        extraCanvas.drawColor(backgroundColorFar)
        redrawBackground()
        if(remoteDevs == null) return
        for(i in 0..1) {
            if(i > remoteDevs.size-1) break
            updateRemoteLocation(remoteDevs[i])
            drawRemote(i, remoteDevs[i])
        }
        for(i in 2 until SettingActivity.scanRemoteNums) {
            if(i > remoteDevs.size-1) break
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
        val r = UniformIntegerDistribution(20, width/2-20).sample()
        remoteDev.theta = theta
        remoteDev.distance = r
        remoteDev.currentx = centerX + r* cos(theta).toFloat()
        remoteDev.currenty = centerY + r* sin(theta).toFloat()
    }
}