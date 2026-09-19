package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partId: Long,
    val partName: String,
    val partNumber: String,
    val actionType: ActionType,
    val quantityChange: Int, // e.g. +5 or -2
    val previousQuantity: Int,
    val newQuantity: Int,
    val technicianName: String, // اسم الفني
    val fireTruckUnit: String,  // رقم/اسم عجلة الإطفاء المستلمة
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class ActionType(val labelAr: String) {
        CHECK_IN("إدخال مادة (توريد)"),
        CHECK_OUT("إخراج مادة (صيانة عجلة)"),
        ADJUST_LOCATION("تعديل موقع التخزين"),
        STOCK_AUDIT("تسوية وجرد مخزني")
    }
}
