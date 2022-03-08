package com.uwbliao.db

import androidx.room.*

@Dao
interface DaoChatMsg {

    @Query("SELECT * FROM EntityChatMsg WHERE deviceName LIKE :name ORDER BY logTime")
    fun findByName(name: String): List<EntityChatMsg>

    @Query("SELECT * FROM EntityChatMsg WHERE deviceName LIKE :name ORDER BY logTime DESC LIMIT 1")
    fun findNewestByName(name: String): EntityChatMsg

    @Insert
    fun insert(vararg chatMsg: EntityChatMsg)

    @Update
    fun update(vararg chatMsg: EntityChatMsg)

    @Delete
    fun delete(chatMsg: EntityChatMsg)
}