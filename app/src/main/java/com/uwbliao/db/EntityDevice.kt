package com.uwbliao.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EntityDevice(
    @PrimaryKey var deviceName: String = "",
    @ColumnInfo(name = "mac") val mac: String? = null,
    @ColumnInfo(name = "type") val type: Int = 0,//0:I scanned  1:connected to me
    @ColumnInfo(name = "display") val display: Boolean? = true,//true:display false:non display
    @ColumnInfo(name = "nickname") var nickname: String? = null,
    @ColumnInfo(name = "distance") var distance: Int? = null,//meter
    @ColumnInfo(name = "theta") var theta: Double = 0.toDouble(),//angle
    @ColumnInfo(name = "currentx") var currentx: Float = 0f,
    @ColumnInfo(name = "currenty") var currenty: Float = 0f,
)