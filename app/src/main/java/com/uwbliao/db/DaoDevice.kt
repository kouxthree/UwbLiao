package com.uwbliao.db

import androidx.room.*

@Dao
interface DaoDevice {
    @Query("SELECT * FROM EntityDevice")
    fun getAll(): List<EntityDevice>

    @Query("SELECT * FROM EntityDevice WHERE deviceName IN (:names)")
    fun loadAllByNames(names: List<String>): List<EntityDevice>

    @Query("SELECT * FROM EntityDevice WHERE deviceName LIKE :name LIMIT 1")
    fun findByName(name: String): EntityDevice

    @Insert
    fun insert(vararg devices: EntityDevice)

    @Update
    fun update(vararg devices: EntityDevice)

    @Delete
    fun delete(device: EntityDevice)
}