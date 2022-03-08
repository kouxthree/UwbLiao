package com.uwbliao.db

import androidx.room.Room
import com.uwbliao.APP_ROOM_DB_NAME
import com.uwbliao.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RepSetting() {
    private val appRoomDb = Room.databaseBuilder(
        MainApplication.appContext(),
        AppDb::class.java, APP_ROOM_DB_NAME
    ).build()
    private val daoSetting = appRoomDb.daoSetting()
    private suspend fun insertDefault() {
        entitySetting = EntitySetting()
        return withContext(Dispatchers.IO) { daoSetting.insert(entitySetting!!) }
    }
    private suspend fun getCurrent(): EntitySetting? {
        entitySetting = withContext(Dispatchers.IO) {
            daoSetting.getFirst()
        }
        if(entitySetting == null) insertDefault()
        return entitySetting
    }
    var entitySetting: EntitySetting? = runBlocking { getCurrent() }
    suspend fun updateCurrent(entitySetting: EntitySetting?) {
        if(entitySetting == null) return
        return withContext(Dispatchers.IO) {
            daoSetting.update(entitySetting)
        }
    }
}