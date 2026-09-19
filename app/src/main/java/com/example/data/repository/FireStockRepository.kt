package com.example.data.repository

import com.example.data.dao.InventoryLogDao
import com.example.data.dao.SparePartDao
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import kotlinx.coroutines.flow.Flow

class FireStockRepository(
    private val partDao: SparePartDao,
    private val logDao: InventoryLogDao
) {
    val allParts: Flow<List<SparePart>> = partDao.getAllParts()
    val criticalParts: Flow<List<SparePart>> = partDao.getCriticalParts()
    val allLogs: Flow<List<InventoryLog>> = logDao.getAllLogs()
    val recentLogs: Flow<List<InventoryLog>> = logDao.getRecentLogs(20)

    fun getLogsForPart(partId: Long): Flow<List<InventoryLog>> = logDao.getLogsForPart(partId)

    suspend fun getPartById(id: Long): SparePart? = partDao.getPartById(id)

    suspend fun getPartByBarcode(barcode: String): SparePart? = partDao.getPartByBarcode(barcode.trim())

    suspend fun getPartByPartNumber(partNumber: String): SparePart? = partDao.getPartByPartNumber(partNumber.trim())

    /**
     * تسجيل إدخال مواد جديدة (Check-in)
     * مع التوثيق الزمني وسجل حركة غير قابل للتعديل
     */
    suspend fun checkIn(
        partId: Long,
        quantityToAdd: Int,
        supplierName: String,
        technicianName: String,
        notes: String
    ): Result<SparePart> {
        val part = partDao.getPartById(partId) ?: return Result.failure(Exception("القطعة غير موجودة"))
        val prevQty = part.quantity
        val newQty = prevQty + quantityToAdd
        val updated = part.copy(
            quantity = newQty,
            supplier = if (supplierName.isNotBlank()) supplierName else part.supplier
        )
        partDao.update(updated)

        logDao.insertLog(
            InventoryLog(
                partId = updated.id,
                partName = updated.name,
                partNumber = updated.partNumber,
                actionType = InventoryLog.ActionType.CHECK_IN,
                quantityChange = quantityToAdd,
                previousQuantity = prevQty,
                newQuantity = newQty,
                technicianName = technicianName.ifBlank { "أمين المستودع" },
                fireTruckUnit = "المستودع (توريد وارد)",
                notes = if (supplierName.isNotBlank()) "المورد: $supplierName | $notes" else notes,
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(updated)
    }

    /**
     * تسجيل إخراج مواد لصيانة عجلة إطفاء محددة (Check-out)
     * إلزام تحديد اسم الفني والعجلة المستلمة
     */
    suspend fun checkOut(
        partId: Long,
        quantityToRemove: Int,
        technicianName: String,
        fireTruckUnit: String,
        notes: String
    ): Result<SparePart> {
        val part = partDao.getPartById(partId) ?: return Result.failure(Exception("القطعة غير موجودة"))
        if (part.quantity < quantityToRemove) {
            return Result.failure(Exception("الكمية المطلوبة ($quantityToRemove) تتجاوز الرصيد المتوفر في المخزن (${part.quantity})!"))
        }
        val prevQty = part.quantity
        val newQty = prevQty - quantityToRemove
        val updated = part.copy(quantity = newQty)
        partDao.update(updated)

        logDao.insertLog(
            InventoryLog(
                partId = updated.id,
                partName = updated.name,
                partNumber = updated.partNumber,
                actionType = InventoryLog.ActionType.CHECK_OUT,
                quantityChange = -quantityToRemove,
                previousQuantity = prevQty,
                newQuantity = newQty,
                technicianName = technicianName.ifBlank { "فني الطوارئ" },
                fireTruckUnit = fireTruckUnit.ifBlank { "عجلة طوارئ عامة" },
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(updated)
    }

    /**
     * تعديل موقع التخزين في المستودع
     */
    suspend fun updateLocation(
        partId: Long,
        newBinLocation: String,
        warehouse: String,
        aisle: String,
        shelf: String,
        bin: String,
        technicianName: String,
        notes: String
    ): Result<SparePart> {
        val part = partDao.getPartById(partId) ?: return Result.failure(Exception("القطعة غير موجودة"))
        val oldLoc = part.binLocation
        val updated = part.copy(
            binLocation = newBinLocation,
            warehouse = warehouse,
            aisle = aisle,
            shelf = shelf,
            bin = bin
        )
        partDao.update(updated)

        logDao.insertLog(
            InventoryLog(
                partId = updated.id,
                partName = updated.name,
                partNumber = updated.partNumber,
                actionType = InventoryLog.ActionType.ADJUST_LOCATION,
                quantityChange = 0,
                previousQuantity = part.quantity,
                newQuantity = part.quantity,
                technicianName = technicianName.ifBlank { "مسؤول المستودع" },
                fireTruckUnit = "نقل موقع",
                notes = "تم نقل القطعة من [$oldLoc] إلى [$newBinLocation]. $notes",
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(updated)
    }

    suspend fun addNewPart(part: SparePart, technicianName: String): Long {
        val id = partDao.insert(part)
        logDao.insertLog(
            InventoryLog(
                partId = id,
                partName = part.name,
                partNumber = part.partNumber,
                actionType = InventoryLog.ActionType.CHECK_IN,
                quantityChange = part.quantity,
                previousQuantity = 0,
                newQuantity = part.quantity,
                technicianName = technicianName.ifBlank { "مسؤول المستودع" },
                fireTruckUnit = "إدخال جديد للنظام",
                notes = "تسجيل قطعة جديدة برقم باركود [${part.barcode}] وموقع [${part.binLocation}]",
                timestamp = System.currentTimeMillis()
            )
        )
        return id
    }

    suspend fun deletePart(part: SparePart) {
        partDao.delete(part)
    }
}
