package com.uwbliao.db

import androidx.room.Room
import com.uwbliao.APP_ROOM_DB_NAME
import com.uwbliao.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RepDevice(deviceName: String) {
    private val appRoomDb = Room.databaseBuilder(
        MainApplication.appContext(),
        AppDb::class.java, APP_ROOM_DB_NAME
    ).build()
    private val daoDevice = appRoomDb.daoDevice()
    private suspend fun insertDevice(dname: String) {
        entityDevice = EntityDevice()
        entityDevice!!.deviceName = dname
        return withContext(Dispatchers.IO) { daoDevice.insertAll(entityDevice!!) }
    }
    private suspend fun getDevice(dname: String): EntityDevice? {
        entityDevice = withContext(Dispatchers.IO) {
            daoDevice.findByName(dname)
        }
        if(entityDevice == null) insertDevice(dname)
        return entityDevice
    }
    var entityDevice: EntityDevice? = runBlocking { getDevice(deviceName) }
    suspend fun updateDevice(entityDevice: EntityDevice?) {
        if(entityDevice == null) return
        return withContext(Dispatchers.IO) {
            daoDevice.update(entityDevice)
        }
    }
}