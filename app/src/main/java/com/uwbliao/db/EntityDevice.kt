package com.uwbliao.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EntityDevice(
    @PrimaryKey var deviceName: String = "",
    @ColumnInfo(name = "mac") var mac: String? = null,
    @ColumnInfo(name = "type") var type: Int = 0,//0:I scanned  1:connected to me
    @ColumnInfo(name = "interest") var interest: Int = Interest.NONE,
    @ColumnInfo(name = "display") var display: Boolean? = true,//true:display false:non display
    @ColumnInfo(name = "nickname") var nickname: String? = null,
    @ColumnInfo(name = "distance") var distance: Int? = null,//meter
    @ColumnInfo(name = "theta") var theta: Double = 0.toDouble(),//angle
    var currentx: Float = 0f,
    var currenty: Float = 0f,
)

class Interest {
    companion object {
        const val NONE = 0
        const val INTERESTED = 1
        const val VERY_INTERESTED = 2
    }
}