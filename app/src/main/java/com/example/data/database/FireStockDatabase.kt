package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryLogDao
import com.example.data.dao.SparePartDao
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromActionType(value: InventoryLog.ActionType): String = value.name

    @TypeConverter
    fun toActionType(value: String): InventoryLog.ActionType = try {
        InventoryLog.ActionType.valueOf(value)
    } catch (e: Exception) {
        InventoryLog.ActionType.CHECK_IN
    }
}

@Database(
    entities = [SparePart::class, InventoryLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FireStockDatabase : RoomDatabase() {
    abstract fun sparePartDao(): SparePartDao
    abstract fun inventoryLogDao(): InventoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: FireStockDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FireStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FireStockDatabase::class.java,
                    "firestock_ai_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.sparePartDao(), database.inventoryLogDao())
                    }
                }
            }

            suspend fun populateInitialData(partDao: SparePartDao, logDao: InventoryLogDao) {
                val initialParts = listOf(
                    SparePart(
                        partNumber = "FS-PMP-882",
                        name = "مضخة مياه ضغط عالي روزنباور (NH35)",
                        category = "مضخات وخراطيم",
                        barcode = "FS9928174001",
                        imageDrawableName = "part_water_pump",
                        binLocation = "WH1-A3-S2",
                        warehouse = "المستودع الرئيسي 1",
                        aisle = "ممر المضخات A",
                        shelf = "الرف السفلي 3",
                        bin = "الصندوق رقم 2",
                        quantity = 4,
                        minStock = 3,
                        unit = "مضخة",
                        supplier = "شركة روزنباور العالمية (Rosenbauer AG)",
                        compatibility = "سيارات إطفاء طراز Panther 6x6 و Mercedes Atego",
                        notes = "مضخة طرد مركزي متكاملة مع صمام تصريف وتحكم إلكتروني بالضغط"
                    ),
                    SparePart(
                        partNumber = "FS-HOS-65MM",
                        name = "خرطوم إطفاء كنباس مقوى 2.5 بوصة (30م)",
                        category = "مضخات وخراطيم",
                        barcode = "FS9928174002",
                        imageDrawableName = "part_fire_hose",
                        binLocation = "WH1-B1-S4",
                        warehouse = "المستودع الرئيسي 1",
                        aisle = "ممر الخراطيم B",
                        shelf = "الرف الأوسط 1",
                        bin = "المنصة 4",
                        quantity = 28,
                        minStock = 15,
                        unit = "لفة (30م)",
                        supplier = "مؤسسة التجهيزات التكتيكية للدفاع المدني",
                        compatibility = "جميع عجلات وصهاريج الإطفاء (وصلات Storz)",
                        notes = "مقاوم للاحتكاك والزيوت والحرارة حتى 400 بار ضغط انفجار"
                    ),
                    SparePart(
                        partNumber = "FS-TURBO-450",
                        name = "شاحن توربيني ديزل محرك إطفاء روزنباور",
                        category = "المحرك وناقل الحركة",
                        barcode = "FS9928174003",
                        imageDrawableName = "hero_fire_depot",
                        binLocation = "WH2-C2-S1",
                        warehouse = "مستودع المحركات 2",
                        aisle = "ممر المحركات C",
                        shelf = "الرف 2",
                        bin = "صندوق 1",
                        quantity = 2,
                        minStock = 2, // Critical / Warning threshold
                        unit = "شاحن",
                        supplier = "وكالة مان للمركبات الثقيلة (MAN Truck & Bus)",
                        compatibility = "عجلة إطفاء سريعة طراز MAN TGM 18.340 4x4",
                        notes = "شاحن توربيني مع مبرد داخلي لتحمل سرعات الاستجابة القصوى"
                    ),
                    SparePart(
                        partNumber = "FS-HYD-CUT90",
                        name = "فكي إنقاذ وقص هيدروليكي لوكاس (Lukas E-Draulic)",
                        category = "الأنظمة الهيدروليكية",
                        barcode = "FS9928174004",
                        imageDrawableName = "ic_firestock_logo",
                        binLocation = "WH1-D4-S3",
                        warehouse = "المستودع الرئيسي 1",
                        aisle = "ممر الهيدروليك D",
                        shelf = "الرف العلوي 4",
                        bin = "صندوق الإنقاذ 3",
                        quantity = 1,
                        minStock = 2, // Critical! Below min stock
                        unit = "طقم شفرات",
                        supplier = "شركة لوكاس للأدوات الهيدروليكية (Lukas Rescue)",
                        compatibility = "عجلات التدخل السريع وسيارات إنقاذ الحوادث",
                        notes = "شفرات قص فائقة القوة للتعامل مع هياكل المركبات المقواة"
                    ),
                    SparePart(
                        partNumber = "FS-BRK-HD400",
                        name = "طقم تيل فرامل هوائية خلفية ثقيلة (سفايف)",
                        category = "المحرك وناقل الحركة",
                        barcode = "FS9928174005",
                        imageDrawableName = null,
                        binLocation = "WH2-B2-S1",
                        warehouse = "مستودع المحركات 2",
                        aisle = "ممر الفرامل B",
                        shelf = "الرف 2",
                        bin = "صندوق 1",
                        quantity = 6,
                        minStock = 8, // Warning!
                        unit = "طقم (4 قطع)",
                        supplier = "كنور بريمزه للفرامل (Knorr-Bremse)",
                        compatibility = "سيارات إطفاء مرسيدس آكتورس وسكوتيا",
                        notes = "بطانات فرامل مقاومة لدرجات الحرارة العالية الناتجة عن التوقف المفاجئ"
                    ),
                    SparePart(
                        partNumber = "FS-FLT-D88",
                        name = "فلتر وقود ديزل فاصل للماء مزدوج",
                        category = "المحرك وناقل الحركة",
                        barcode = "FS9928174006",
                        imageDrawableName = null,
                        binLocation = "WH1-E1-S5",
                        warehouse = "المستودع الرئيسي 1",
                        aisle = "ممر الفلاتر E",
                        shelf = "الرف 1",
                        bin = "صندوق 5",
                        quantity = 18,
                        minStock = 6,
                        unit = "فلتر",
                        supplier = "شركة مان وهيومل (MANN-FILTER)",
                        compatibility = "محركات الديزل الثقيلة لمركبات الإطفاء",
                        notes = "تنقية 99.8% من الشوائب وفصل كامل لرطوبة الديزل"
                    ),
                    SparePart(
                        partNumber = "FS-LED-LT911",
                        name = "وحدة إنارة طوارئ LED زرقاء وحمراء محيطية",
                        category = "الكهرباء والإنارة",
                        barcode = "FS9928174007",
                        imageDrawableName = null,
                        binLocation = "WH1-F3-S2",
                        warehouse = "المستودع الرئيسي 1",
                        aisle = "ممر الكهرباء F",
                        shelf = "الرف 3",
                        bin = "صندوق 2",
                        quantity = 12,
                        minStock = 4,
                        unit = "وحدة",
                        supplier = "شركة فيدرال سيجنال (Federal Signal)",
                        compatibility = "سقف وأجناب جميع آليات الدفاع المدني والإطفاء",
                        notes = "قوة سطوع 10000 لومن مع 14 نمط وميض ومقاومة للماء IP68"
                    )
                )

                partDao.insertAll(initialParts)

                // Pre-populate immutable audit logs to demonstrate tracking & AI burn-rate prediction
                val now = System.currentTimeMillis()
                val oneHour = 3600000L
                val oneDay = 86400000L

                val initialLogs = listOf(
                    InventoryLog(
                        partId = 1,
                        partName = "مضخة مياه ضغط عالي روزنباور (NH35)",
                        partNumber = "FS-PMP-882",
                        actionType = InventoryLog.ActionType.CHECK_IN,
                        quantityChange = 5,
                        previousQuantity = 0,
                        newQuantity = 5,
                        technicianName = "م. خالد العتيبي (مسؤول الإسناد)",
                        fireTruckUnit = "المستودع المركزي",
                        notes = "توريد دفعة جديدة معتمدة من المصنع بشهادة مطابقة",
                        timestamp = now - (oneDay * 4)
                    ),
                    InventoryLog(
                        partId = 1,
                        partName = "مضخة مياه ضغط عالي روزنباور (NH35)",
                        partNumber = "FS-PMP-882",
                        actionType = InventoryLog.ActionType.CHECK_OUT,
                        quantityChange = -1,
                        previousQuantity = 5,
                        newQuantity = 4,
                        technicianName = "الفني راشد الشمري",
                        fireTruckUnit = "عجلة إطفاء 104 (فهد 6x6)",
                        notes = "استبدال طارئ للمضخة بعد بلاغ حريق المنطقة الصناعية",
                        timestamp = now - (oneDay * 2) - (oneHour * 3)
                    ),
                    InventoryLog(
                        partId = 4,
                        partName = "فكي إنقاذ وقص هيدروليكي لوكاس (Lukas E-Draulic)",
                        partNumber = "FS-HYD-CUT90",
                        actionType = InventoryLog.ActionType.CHECK_OUT,
                        quantityChange = -1,
                        previousQuantity = 2,
                        newQuantity = 1,
                        technicianName = "الفني طارق الدوسري",
                        fireTruckUnit = "عجلة إنقاذ الحوادث 302",
                        notes = "تجهيز فرقة التدخل السريع بحادث تصادم طريق المطار",
                        timestamp = now - (oneHour * 5)
                    ),
                    InventoryLog(
                        partId = 2,
                        partName = "خرطوم إطفاء كنباس مقوى 2.5 بوصة (30م)",
                        partNumber = "FS-HOS-65MM",
                        actionType = InventoryLog.ActionType.CHECK_OUT,
                        quantityChange = -4,
                        previousQuantity = 32,
                        newQuantity = 28,
                        technicianName = "الفني فهد المطيري",
                        fireTruckUnit = "صهريج تزويد مياه 501",
                        notes = "استبدال خراطيم متضررة من الردم أثناء عملية مكافحة المستودعات",
                        timestamp = now - (oneHour * 1)
                    )
                )

                logDao.insertAll(initialLogs)
            }
        }
    }
}
