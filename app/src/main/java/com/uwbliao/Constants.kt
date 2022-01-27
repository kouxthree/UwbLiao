package com.uwbliao

import android.content.res.Resources

const val STROKE_WIDTH = 12f
const val ME_RADIUS = 10f
const val REMOTE_RADIUS = 5f
const val NEAR_DISTANCE = 3//meters
const val MID_DISTANCE = 6//meters
const val FAR_DISTANCE = 9//meters
const val INFO_REFRESH_RATE = 3000L//1000 == one seconds
const val REMOTE_TEXT_SIZE = 50f
const val DISTANCE_TEXT_SIZE = 60f
const val MY_NICKNAME_DEFAULT = "LiaoU User"
const val APP_ROOM_DB_NAME = "liaoudb"
const val SCAN_NUMS_MAX = 9
const val TAP_ACCURACY = 30

class Gender {
    companion object {
        const val OTHER = 0
        const val MALE = 1
        const val FEMALE = 2
        fun getName(resources: Resources, gender: Int): String {
            return(when(gender) {
                MALE -> resources.getString(R.string.gender_male)
                FEMALE -> resources.getString(R.string.gender_female)
                else -> resources.getString(R.string.gender_other)
            })
        }
    }
}

class Interest {
    companion object {
        const val NONE = 0
        const val INTERESTED = 1
        const val VERY_INTERESTED = 2
    }
}

class DispMode {
    companion object {
        const val DEFAULT = 0
        const val LARGE = 1
        const val SMALL = 2
    }
}

class ZoomInRec(screenWidth: Int, screenHeight: Int) {
    var screenWidth = 0
    var screenHeight = 0
    companion object {
        const val left = 20//distance from screen left bound
        const val bottom = 20//distance from screen bottom bound
        const val width = 80//rectangle width
        const val height = 80//rectangle height
    }
    init {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }
    fun hit(x: Int, y: Int): Boolean {
        return (x in left..left+width)
                && (y in screenHeight-(bottom+height) .. screenHeight-bottom)
    }
}

class ZoomOutRec(screenWidth: Int, screenHeight: Int) {
    var screenWidth = 0
    var screenHeight = 0
    companion object {
        const val left = 120//distance from screen left bound
        const val bottom = 20//distance from screen bottom bound
        const val width = 80//rectangle width
        const val height = 80//rectangle height
    }
    init {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }
    fun hit(x: Int, y: Int): Boolean {
        return (x in left .. left+width)
                && (y in screenHeight-(bottom+height) .. screenHeight-bottom)
    }
}

class BlacklistRec(screenWidth: Int, screenHeight: Int) {
    var screenWidth = 0
    var screenHeight = 0
    companion object {
        const val right = 20//distance from screen left bound
        const val bottom = 20//distance from screen bottom bound
        const val width = 80//rectangle width
        const val height = 80//rectangle height
    }
    init {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }
    fun hit(x: Int, y: Int): Boolean {
        return (x in screenWidth-(width+right) .. screenWidth-right)
                && (y in screenHeight-(bottom+height) .. screenHeight-bottom)
    }
}