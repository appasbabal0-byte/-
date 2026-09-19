package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spare_parts")
data class SparePart(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partNumber: String,
    val name: String,
    val category: String,
    val barcode: String,
    val imageDrawableName: String? = null,
    val imageUri: String? = null,
    val binLocation: String, // e.g., "WH1-A3-S2"
    val warehouse: String = "المستودع المركزي 1",
    val aisle: String = "الممر A",
    val shelf: String = "الرف 3",
    val bin: String = "الصندوق 2",
    val quantity: Int,
    val minStock: Int,
    val unit: String = "قطعة",
    val supplier: String = "المورد المعتمد",
    val compatibility: String = "عجلات إطفاء متعددة الطرازات",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class StockStatus {
        GOOD,       // 🟢 أخضر: كمية ممتازة
        WARNING,    // 🟡 أصفر: كمية متوسطة - اقتراب من حد الطلب
        CRITICAL    // 🔴 أحمر: كمية حرجة أو نافدة
    }

    val stockStatus: StockStatus
        get() = when {
            quantity <= minStock -> StockStatus.CRITICAL
            quantity <= (minStock * 1.5).toInt() -> StockStatus.WARNING
            else -> StockStatus.GOOD
        }
}
