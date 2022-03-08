package com.uwbliao

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uwbliao.databinding.BlacklistDspBinding
import com.uwbliao.databinding.RemoteDevDspBinding
import com.uwbliao.db.EntityDevice
import com.uwbliao.db.RepChatMsg
import com.uwbliao.db.RepDevice
import com.uwbliao.recycler.BlacklistRecyclerAdapter
import com.uwbliao.recycler.BlacklistRecyclerItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.distribution.UniformIntegerDistribution
import kotlin.math.cos
import kotlin.math.sin

open class MainCanvasView(context: Context): View(context), LifecycleOwner {
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
    private var mDispMode = DispMode.DEFAULT
    private var horizontalRadius = 0f
    private var remoteDevs = mutableListOf<EntityDevice>()
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initRemoteDevs()
        horizontalRadius = getHorizontalRadiusByMode()
//        //compass drawing observer
//        dirSensor.orientAngel.observe(this , {
//            drawCompass()
//        })
    }
    fun initRemoteDevs() {
        //direction sensor
        dirSensor = DirSensor(context)
        //remote dev init
        remoteDevs = mutableListOf()
        for(i in 0 until SettingActivity.scanRemoteNums) {
            val repdev = RepDevice(i.toString())
            lifecycleScope.launch {
                val dev = repdev.entityDevice!!
                remoteDevs.add(dev)
            }
        }
        if(remoteDevs.size > 0) {
            remoteDevs[0].nickname = "貂蝉"
            remoteDevs[0].gender = Gender.FEMALE
        }
        if(remoteDevs.size > 1) {
            remoteDevs[1].nickname = "吕布"
            remoteDevs[1].gender = Gender.MALE
        }
        if(remoteDevs.size > 2) {
            remoteDevs[2].nickname = "西施"
            remoteDevs[2].gender = Gender.FEMALE
        }
        if(remoteDevs.size > 3) {
            remoteDevs[3].nickname = "勾践"
            remoteDevs[3].gender = Gender.MALE
        }
    }
    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle() = lifecycleRegistry
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
    private fun drawCompass() {
        val mymatrix = Matrix()
//        mymatrix.postRotate(dirSensor.orientAngel.value!!)
//        mymatrix.postRotate(DirSensor.orientAngel * 57.2957795f)
        val degree = Math.toDegrees(DirSensor.orientAngel.toDouble())
//        mymatrix.setRotate(DirSensor.orientAngel * 360f / Math.PI.toFloat(),
//            bitmapCompass.width.toFloat()/2, bitmapCompass.height.toFloat()/2)
        mymatrix.setRotate(-degree.toFloat(),
            bitmapCompass.width.toFloat()/2, bitmapCompass.height.toFloat()/2)
        mymatrix.postTranslate(width-20-bitmapCompass.width.toFloat(), 20f)
        //degree
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
        extraCanvas.drawBitmap(bitmapCompass, mymatrix, null)
        extraCanvas.drawText(DirSensor.orientAngel.toString(), 30f, 40f, paint)
        extraCanvas.drawText(degree.toString(), 30f, 80f, paint)
    }
    private fun drawAdjust() {
        val dplus = AppCompatResources.getDrawable(context, R.drawable.ic_adjust_plus)
        dplus!!.setBounds(ZoomInRec.left,
            height-(ZoomInRec.bottom+ZoomInRec.height),
            ZoomInRec.left+ZoomInRec.width,
            height-ZoomInRec.bottom)
        dplus.draw(extraCanvas)
        val dminus = AppCompatResources.getDrawable(context, R.drawable.ic_adjust_minus)
        dminus!!.setBounds(ZoomOutRec.left,
            height-(ZoomOutRec.bottom+ZoomOutRec.height),
            ZoomOutRec.left+ZoomOutRec.width,
            height-ZoomOutRec.bottom)
        dminus.draw(extraCanvas)
    }
    private fun drawBlacklist() {
        val b = AppCompatResources.getDrawable(context, R.drawable.ic_blacklist)
        b!!.setBounds(width-(BlacklistRec.width+BlacklistRec.right),
            height-(ZoomInRec.bottom+ZoomInRec.height),
            width-BlacklistRec.right,
            height-ZoomInRec.bottom)
        b.draw(extraCanvas)
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
    private val remoteColor = ResourcesCompat.getColor(resources, R.color.remotePaint, null)
    private val remoteInterestedColor = ResourcesCompat.getColor(resources, R.color.remoteInterestedPaint, null)
    private val remoteVeryInterestedColor = ResourcesCompat.getColor(resources, R.color.remoteVeryInterestedPaint, null)
    private var centerX = 0f
    private var centerY = 0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()
        //alpha = 0f//transparent for background redraw
        redrawBackground()
        infoRefreshHandler.removeCallbacks(infoRefreshTask)
        infoRefreshHandler.post(infoRefreshTask)

        // Create the new scale gesture detector object use above pinch zoom gesture listener.
        scaleGestureDetector = ScaleGestureDetector(context, pinchListener)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val x = event.x.toInt()
        val y = event.y.toInt()
        val zoomInRec = ZoomInRec(width, height)
        val zoomOutRec = ZoomOutRec(width, height)
        val blacklistRec = BlacklistRec(width, height)
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
                when {
                    tappedI != null -> {
                        //remote device tapped
                        displayRemoteDevice(tappedI)
                    }
                    zoomInRec.hit(x,y) -> {
                        //zoom in
                        changeHorizontalRadiusByMode(-1f)
                        refreshInfo()
                        invalidate()
                        return true
                    }
                    zoomOutRec.hit(x,y) -> {
                        //zoom out
                        changeHorizontalRadiusByMode(1f)
                        refreshInfo()
                        invalidate()
                        return true
                    }
                    blacklistRec.hit(x,y) -> {
                        //blacklist
                        displayBlacklist()
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {

            }
        }

        //other event
        //dispatch touch event to scale gesture
        scaleGestureDetector.onTouchEvent(event)
        return true
    }
    private val pinchListener = object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
//        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
//            //stop view redraw
//            infoRefreshHandler.removeCallbacks(infoRefreshTask)
//            return true
//        }
        // When pinch zoom gesture occurred.
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            //Toast.makeText(context, "on scale", Toast.LENGTH_SHORT).show()
            val scaleFactor = detector.scaleFactor
            scaleImage(scaleFactor, scaleFactor)
            return true
        }
//        override fun onScaleEnd(detector: ScaleGestureDetector?) {
//            //restart view redraw
//            infoRefreshHandler.post(infoRefreshTask)
//        }
        //scale the image
        private fun scaleImage(xScale: Float, yScale: Float) {
            val scaledWidth = extraBitmap.width*xScale
            val scaledHeight = extraBitmap.height*yScale
            val scaledBitmap = scale(extraBitmap, scaledWidth.toInt(), scaledHeight.toInt())
            extraCanvas.drawBitmap(scaledBitmap,
                centerX-scaledBitmap.width/2, centerY-scaledBitmap.height/2,
                null)
            invalidate()//redraw immediately
            //set horizontal radius
            horizontalRadius *= xScale
            if(horizontalRadius > horizontalRadiusMax()) horizontalRadius = horizontalRadiusMax()
            if(horizontalRadius < horizontalRadiusMin()) horizontalRadius = horizontalRadiusMin()
            //set dispmode
            mDispMode = if(horizontalRadiusMin() < horizontalRadius && horizontalRadius < horizontalRadiusMid()) {
                //between min-mid -> displaying large
                DispMode.LARGE
            } else if(horizontalRadiusMid() < horizontalRadius && horizontalRadius < horizontalRadiusMax()) {
                //between mid-max -> displaying small
                DispMode.SMALL
            } else {
                DispMode.DEFAULT
            }
        }
        // Scale a bitmap preserving the aspect ratio.
        private fun scale(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
            // Determine the constrained dimension, which determines both dimensions.
            val width: Int
            val height: Int
            val widthRatio = bitmap.width.toFloat() / maxWidth
            val heightRatio = bitmap.height.toFloat() / maxHeight
            // Width constrained.
            if (widthRatio >= heightRatio) {
                width = maxWidth
                height = (width.toFloat() / bitmap.width * bitmap.height).toInt()
            } else {
                height = maxHeight
                width = (height.toFloat() / bitmap.height * bitmap.width).toInt()
            }
            val scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val ratioX = width.toFloat() / bitmap.width
            val ratioY = height.toFloat() / bitmap.height
            val middleX = width / 2.0f
            val middleY = height / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(
                bitmap,
                middleX - bitmap.width / 2,
                middleY - bitmap.height / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            return scaledBitmap
        }
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
        (idx.toString() + "/" + Gender.getName(resources, remoteDevs[idx].gender) + ":"
                + remoteDevs[idx].nickname.toString()).also { mBinding.txtNickname.text = it }
        (Utils.realDistanceFromCoordinateDistance(remoteDevs[idx].distance, myDistanceRatio())
            .formatDecimalPoint1() + "m").also { mBinding.txtDistance.text = it }
        //get latest in/out msg
        val repChatMsg = RepChatMsg()
        var entityChatMsg = runBlocking { repChatMsg.getNewestChatMsg(remoteDevs[idx].deviceName) }
        //favorite icon
        when(remoteDevs[idx].interest) {
            Interest.INTERESTED -> {
                mBinding.imgInterest.setImageResource(R.drawable.ic_favorite)
            }
        }
        mBinding.imgInterest.setOnClickListener {
            when(remoteDevs[idx].interest) {
                Interest.INTERESTED -> {
                    mBinding.imgInterest.setImageResource(R.drawable.ic_favorite_border)
                    remoteDevs[idx].interest = Interest.NONE
                }
                else -> {
                    mBinding.imgInterest.setImageResource(R.drawable.ic_favorite)
                    remoteDevs[idx].interest = Interest.INTERESTED
                }
            }
            //save to db
            val repdev = RepDevice(remoteDevs[idx].deviceName)
            lifecycleScope.launch { repdev.updateDevice(remoteDevs[idx]) }
        }
        //comment text
        mBinding.txtComment.setText(remoteDevs[idx].comment)
        mBinding.txtComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                remoteDevs[idx].comment = s.toString()
                val repdev = RepDevice(remoteDevs[idx].deviceName)
                lifecycleScope.launch { repdev.updateDevice(remoteDevs[idx]) }
            }
        })
        //cancel button
        mBinding.btnCancel.setOnClickListener {
            dlg.dismiss()
        }
        //okay button
        mBinding.btnOkay.setOnClickListener {
            dlg.dismiss()
        }
        //in/out msg
        if(entityChatMsg != null){
            when(entityChatMsg.type) {
                ChatMsgType.IN -> mBinding.txtMsgIn.setText(entityChatMsg!!.msg)
                ChatMsgType.OUT -> mBinding.txtMsgOut.setText(entityChatMsg!!.msg)
            }
        }
        //send out msg
        mBinding.txtMsgOut.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                setSendButtonVisibility(mBinding)
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int,count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) { }
        })
        mBinding.txtMsgOut.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if(mBinding.txtMsgOut.text.isNotEmpty()) {
                    sendOutMsg(idx, mBinding.txtMsgOut.text.toString())
                }
                handled = true
            }
            handled
        }
        //text send button
        setSendButtonVisibility(mBinding)
        mBinding.imgSend.setOnClickListener {
            if(mBinding.txtMsgOut.text.isEmpty()) return@setOnClickListener
            sendOutMsg(idx, mBinding.txtMsgOut.text.toString())
        }
        //blacklist icon
        mBinding.imgBlacklist.setOnClickListener {
            if(remoteDevs[idx].hide) {
                mBinding.imgBlacklist.setImageResource(R.drawable.ic_blacklist_off)
                remoteDevs[idx].hide = false
            } else {
                mBinding.imgBlacklist.setImageResource(R.drawable.ic_blacklist_on)
                remoteDevs[idx].hide = true
            }
            //save to db
            val repdev = RepDevice(remoteDevs[idx].deviceName)
            lifecycleScope.launch { repdev.updateDevice(remoteDevs[idx]) }
        }
        dlg.show()
    }
    private fun setSendButtonVisibility(mBinding: RemoteDevDspBinding) {
        mBinding.imgSend.isVisible = !mBinding.txtMsgOut.text.isEmpty()
    }
    private fun sendOutMsg(idx: Int, msg: String) {
        //save to db
        val rep = RepChatMsg()
        lifecycleScope.launch { rep.insertChatMsg(remoteDevs[idx].deviceName, ChatMsgType.OUT, msg) }
    }
    //display blacklist dialog
    private fun displayBlacklist() {
        val dsp = AlertDialog.Builder(
            context,
//            R.style.FullscreenAlertDialogStyle
//            android.R.style.Theme_Material_Light_NoActionBar_Fullscreen
        )
        val mBinding = BlacklistDspBinding.inflate(LayoutInflater.from(context))
        dsp.setView(mBinding.root.rootView)
        val dlg = dsp.create()
        dlg.setCanceledOnTouchOutside(true)
        dlg.setTitle(resources.getString(R.string.txt_blacklist_title))
        //recycler
        val blacklistRecyclerAdapter = BlacklistRecyclerAdapter(mBinding)
        val layoutManager = LinearLayoutManager(context)
        mBinding.listBlacklist.layoutManager = layoutManager
        mBinding.listBlacklist.adapter = blacklistRecyclerAdapter
        //add blacklist items
        for(i in 0 until SettingActivity.scanRemoteNums) {
            if(remoteDevs[i].hide) {
                blacklistRecyclerAdapter.addSingleItem(
                    BlacklistRecyclerItem(
                        DeviceName = remoteDevs[i].deviceName,
                        Nickname = remoteDevs[i].nickname.toString(),
                        Gender = Gender.getName(resources, remoteDevs[i].gender),
                        RemoteDev = remoteDevs[i]
                    )
                )
            }
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
    private fun drawFar() {
        val r = (horizontalRadius*3f)/2f//(width*3f)/4f
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
        val r = horizontalRadius//width/2f
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
        val r = horizontalRadius/2f//width/4f
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
        if(width == 0) return//wait until layout init complete
        extraCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        extraCanvas.drawColor(backgroundColor)
        drawFar()
        drawMid()
        drawNear()
        drawMe()
        drawCompass()
        drawAdjust()
        drawBlacklist()
    }
    private fun drawRemote(i: Int, remoteDev: EntityDevice) {
//        val theta = remoteDev.theta + Math.PI/2 - dirSensor.orientAngel.value!!
        val theta = remoteDev.theta + Math.PI/2 - DirSensor.orientAngel
        val drawdistance = remoteDev.distance!!*(horizontalRadius*2/width)
//        remoteDev.currentx = centerX + remoteDev.distance!!*cos(theta).toFloat()
//        remoteDev.currenty = centerY + remoteDev.distance!!*sin(theta).toFloat()
        remoteDev.currentx = centerX + drawdistance*cos(theta).toFloat()
        remoteDev.currenty = centerY + drawdistance*sin(theta).toFloat()
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
            color = getRemoteColor(remoteDev.interest)
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
            if(!remoteDevs[i].hide) drawRemote(i, remoteDevs[i])
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
        val r = UniformIntegerDistribution(20, horizontalRadiusMax().toInt()-20).sample()
        val gender = UniformIntegerDistribution(0, 3).sample()
        remoteDev.theta = theta
        remoteDev.distance = r
        remoteDev.gender = gender
//        remoteDev.currentx = centerX + r* cos(theta).toFloat()
//        remoteDev.currenty = centerY + r* sin(theta).toFloat()
    }
    //utils
    private fun myDistanceRatio(): Float {
        return Utils.realDistanceRatio(MID_DISTANCE, horizontalRadiusMid().toInt())
    }
    private fun getRemoteColor(interest: Int): Int {
        return when(interest) {
            Interest.INTERESTED -> remoteInterestedColor
            Interest.VERY_INTERESTED -> remoteVeryInterestedColor
            else -> remoteColor
        }
    }
    private fun horizontalRadiusMax(): Float {
        return width*3f/4
    }
    private fun horizontalRadiusMid(): Float {
        return width/2f
    }
    private fun horizontalRadiusMin(): Float {
        return width*3f/8
    }
    private fun getHorizontalRadiusByMode(): Float {
        return (when(mDispMode) {
            DispMode.LARGE -> horizontalRadiusMin()
            DispMode.SMALL -> horizontalRadiusMax()
            else -> horizontalRadiusMid()
        })
    }
    private fun changeHorizontalRadiusByMode(scale: Float) {
        if(scale > 0) {//small -> large
            if(mDispMode == DispMode.SMALL) mDispMode = DispMode.DEFAULT
            else if(mDispMode == DispMode.DEFAULT) mDispMode = DispMode.LARGE
        } else if(scale < 0) {//large -> small
            if(mDispMode == DispMode.LARGE) mDispMode = DispMode.DEFAULT
            else if(mDispMode == DispMode.DEFAULT) mDispMode = DispMode.SMALL
        }
        //set horizontal radius
        horizontalRadius = getHorizontalRadiusByMode()
    }

    companion object {
        private val TAG = MainCanvasView::class.java.simpleName
    }
}