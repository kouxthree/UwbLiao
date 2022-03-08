package com.uwbliao.db

import androidx.room.Room
import com.uwbliao.APP_ROOM_DB_NAME
import com.uwbliao.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class RepChatMsg {
    private val appRoomDb = Room.databaseBuilder(
        MainApplication.appContext(),
        AppDb::class.java, APP_ROOM_DB_NAME
    ).build()
    private val daoChatMsg = appRoomDb.daoChatMsg()
    suspend fun insertChatMsg(dname: String, type: Int, msg: String) {
        val entityChatMsg = EntityChatMsg(dname, Instant.now().toEpochMilli(), type, msg)
        return withContext(Dispatchers.IO) { daoChatMsg.insert(entityChatMsg) }
    }
    suspend fun getNewestChatMsg(dname: String): EntityChatMsg {
        return withContext(Dispatchers.IO) {
            daoChatMsg.findNewestByName(dname)
        }
    }
    suspend fun updateChatMsg(entityChatMsg: EntityChatMsg?) {
        if(entityChatMsg == null) return
        return withContext(Dispatchers.IO) {
            daoChatMsg.update(entityChatMsg)
        }
    }
}