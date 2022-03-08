package com.uwbliao.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["deviceName", "logTime"])
data class EntityChatMsg(
    @ColumnInfo(name = "deviceName") var deviceName: String = "",
    @ColumnInfo(name = "logTime") var logTime: Long,
    @ColumnInfo(name = "type") var type: Int,//0:in  1:out
    @ColumnInfo(name = "msg") var msg: String,
)