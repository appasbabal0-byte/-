package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InventoryLog
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryLogDao {
    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_logs WHERE partId = :partId ORDER BY timestamp DESC")
    fun getLogsForPart(partId: Long): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_logs WHERE fireTruckUnit != '' GROUP BY fireTruckUnit")
    fun getDistinctFireTrucks(): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<InventoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InventoryLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<InventoryLog>)
}
