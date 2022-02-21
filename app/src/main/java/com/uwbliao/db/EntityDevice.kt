package com.uwbliao.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uwbliao.Gender
import com.uwbliao.Interest

@Entity
data class EntityDevice(
    @PrimaryKey var deviceName: String = "",
    @ColumnInfo(name = "mac") var mac: String? = null,
    @ColumnInfo(name = "type") var type: Int = 0,//0:I scanned  1:connected to me
    @ColumnInfo(name = "interest") var interest: Int = Interest.NONE,
    @ColumnInfo(name = "hide") var hide: Boolean = false,//true:hide false:show
    @ColumnInfo(name = "nickname") var nickname: String? = null,
    @ColumnInfo(name = "gender") var gender: Int = Gender.OTHER,
    @ColumnInfo(name = "comment") var comment: String = "",
    @ColumnInfo(name = "distance") var distance: Int? = null,//meter
    @ColumnInfo(name = "theta") var theta: Double = 0.toDouble(),//angle
    var currentx: Float = 0f,
    var currenty: Float = 0f,
)