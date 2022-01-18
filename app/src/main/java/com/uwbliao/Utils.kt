package com.uwbliao

import kotlin.math.pow
import kotlin.math.sqrt

fun Float?.formatDecimalPoint1(): String{
    if(this == null) return ""
    val fmt = "%.${1}f"
    return fmt.format(this)
}
class Utils {
    companion object {
        //readD: real distance
        //coordinateD: coordinate distance
        fun realDistanceRatio(realD: Int, coordinateD: Int): Float {
            if(coordinateD == 0) return 0f
            return realD.toFloat()/coordinateD.toFloat()
        }
        //c: coordinate distance
        fun realDistanceFromCoordinateDistance(c: Int?, ratio: Float): Float {
            if(c == null || ratio == 0f) return 0f
            return c.toFloat()*ratio
        }
        fun distanceBetweenPoints(x1: Int, y1: Int, x2: Int, y2: Int): Double {
            return sqrt(
                (x1 - x2).toDouble().pow(2.toDouble())
                + (y1 - y2).toDouble().pow(2.toDouble())
            )
        }
    }
}