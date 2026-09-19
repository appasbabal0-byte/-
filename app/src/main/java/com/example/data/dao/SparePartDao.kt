package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SparePart
import kotlinx.coroutines.flow.Flow

@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts ORDER BY name ASC")
    fun getAllParts(): Flow<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE id = :id")
    suspend fun getPartById(id: Long): SparePart?

    @Query("SELECT * FROM spare_parts WHERE barcode = :barcode LIMIT 1")
    suspend fun getPartByBarcode(barcode: String): SparePart?

    @Query("SELECT * FROM spare_parts WHERE partNumber = :partNumber LIMIT 1")
    suspend fun getPartByPartNumber(partNumber: String): SparePart?

    @Query("SELECT * FROM spare_parts WHERE quantity <= minStock")
    fun getCriticalParts(): Flow<List<SparePart>>

    @Query("SELECT COUNT(*) FROM spare_parts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(part: SparePart): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parts: List<SparePart>)

    @Update
    suspend fun update(part: SparePart)

    @Delete
    suspend fun delete(part: SparePart)
}
