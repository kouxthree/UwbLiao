package com.uwbliao.recycler

import com.uwbliao.db.EntityDevice

class BlacklistRecyclerItem(
    var DeviceName: String,
    val Nickname: String,
    val Gender: String,
    val RemoteDev: EntityDevice
    )