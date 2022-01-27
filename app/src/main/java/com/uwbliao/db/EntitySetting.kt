package com.uwbliao.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uwbliao.Gender
import com.uwbliao.MY_NICKNAME_DEFAULT

@Entity
data class EntitySetting(
    @PrimaryKey var uid: Int = 0,
    @ColumnInfo(name = "first_name") var firstName: String? = "",
    @ColumnInfo(name = "last_name") var lastName: String? = "",
    @ColumnInfo(name = "nick_name") var nickName: String? = MY_NICKNAME_DEFAULT,
    @ColumnInfo(name = "my_gender") var myGender: Int = Gender.OTHER,
    @ColumnInfo(name = "remote_gender") var remoteGender: Int = Gender.OTHER,
    @ColumnInfo(name = "scan_nums") var scanNums: Int = 0,
)