package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.FireStockAI
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import com.example.ui.components.Barcode1DView
import com.example.ui.components.BinLocationBadge
import com.example.ui.components.QrCodeView
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.FireRedBright
import com.example.ui.theme.StockGreen
import com.example.ui.theme.WaterBlueBright
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartDetailDialog(
    part: SparePart,
    partLogs: List<InventoryLog>,
    onDismiss: () -> Unit,
    onOpenCheckIn: (SparePart) -> Unit,
    onOpenCheckOut: (SparePart) -> Unit,
    onUpdateLocation: (partId: Long, newBinLocation: String, warehouse: String, aisle: String, shelf: String, bin: String, technician: String, notes: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isEditingLocation by remember { mutableStateOf(false) }

    // Bin location fields
    var warehouseInput by remember { mutableStateOf(part.warehouse) }
    var aisleInput by remember { mutableStateOf(part.aisle) }
    var shelfInput by remember { mutableStateOf(part.shelf) }
    var binInput by remember { mutableStateOf(part.bin) }
    var technicianInput by remember { mutableStateOf("مسؤول المستودع") }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }
    val aiPrediction = remember(part, partLogs) { FireStockAI.predictDepletion(part, partLogs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(680.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = part.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "رمز المادة: ${part.partNumber}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = " • الفئة: ${part.category}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Status & Quick Actions Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StockStatusBadge(
                        quantity = part.quantity,
                        minStock = part.minStock,
                        unit = part.unit,
                        status = part.stockStatus
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onOpenCheckIn(part) },
                            colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.testTag("part_detail_check_in_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدخال (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOpenCheckOut(part) },
                            colors = ButtonDefaults.buttonColors(containerColor = FireRedBright),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.testTag("part_detail_check_out_button")
                        ) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صرف (-)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tabs: 0: تفاصيل وباركود | 1: موقع التخزين | 2: سجل الحركات (Audit) | 3: ذكاء التنبؤ
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("الباركود والمواصفات", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("موقع التخزين", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("سجل العمليات (${partLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberCaution)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تنبؤ AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Barcode & Specifications Tab
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text(
                                        text = "الباركود والرمز المشفر (Scannable Barcode & QR):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Barcode1DView(
                                        barcodeText = part.barcode,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        QrCodeView(
                                            dataText = "FIRESTOCK:${part.partNumber}:${part.barcode}:${part.binLocation}",
                                            modifier = Modifier.size(120.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("رمز QR الميداني للمستودع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                "يحتوي على رمز القطعة ورقمها وموقع الرف المباشر لمسحه بواسطة فرق الصيانة في الطوارئ.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "الباركود: ${part.barcode}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("المواصفات الفنية والتوافق:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "• نوع الوحدة المخزنية: ${part.unit} (عدد / طقم)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "• التوافق مع العجلات: ${part.compatibility}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "• المورد المعتمد: ${part.supplier}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "• حد الإنذار الأدنى (Min Stock): ${part.minStock} ${part.unit}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (part.notes.isNotBlank()) {
                                                Text(
                                                    "• ملاحظات فنية: ${part.notes}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            val context = androidx.compose.ui.platform.LocalContext.current
                                            OutlinedButton(
                                                onClick = {
                                                    com.example.util.DocumentPrintAndExportHelper.printCheckInDocument(
                                                        context = context,
                                                        part = part,
                                                        quantity = part.quantity,
                                                        supplier = part.supplier,
                                                        technician = "مسؤول المستودع",
                                                        notes = "طباعة بطاقة تعريف ومواصفات القطعة"
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("طباعة بطاقة تعريف المادة والمستند الرسمي", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Bin Location Management Tab
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "تحديد موقع التخزين الدقيق (Bin Location Management)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                // Visual Location Breadcrumbs
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "رمز الموقع الحالي: ${part.binLocation}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            LocationStepChip("المستودع", part.warehouse)
                                            LocationStepChip("الممر", part.aisle)
                                            LocationStepChip("الرف", part.shelf)
                                            LocationStepChip("الصندوق", part.bin)
                                        }
                                    }
                                }

                                if (!isEditingLocation) {
                                    FilledTonalButton(
                                        onClick = { isEditingLocation = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.EditLocation, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تعديل موقع الرف أو الصندوق في المستودع")
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("تحديث إحداثيات التخزين:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = warehouseInput,
                                                    onValueChange = { warehouseInput = it },
                                                    label = { Text("المستودع") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                                OutlinedTextField(
                                                    value = aisleInput,
                                                    onValueChange = { aisleInput = it },
                                                    label = { Text("الممر") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = shelfInput,
                                                    onValueChange = { shelfInput = it },
                                                    label = { Text("الرف") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                                OutlinedTextField(
                                                    value = binInput,
                                                    onValueChange = { binInput = it },
                                                    label = { Text("الصندوق") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                            }
                                            OutlinedTextField(
                                                value = technicianInput,
                                                onValueChange = { technicianInput = it },
                                                label = { Text("اسم منفذ النقل") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { isEditingLocation = false }) {
                                                    Text("إلغاء")
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = {
                                                        val generatedCode = "WH${warehouseInput.filter { it.isDigit() }.ifEmpty { "1" }}-A${aisleInput.takeLast(1).ifEmpty { "A" }}-S${shelfInput.takeLast(1).ifEmpty { "1" }}-B${binInput.takeLast(1).ifEmpty { "1" }}"
                                                        onUpdateLocation(
                                                            part.id,
                                                            generatedCode,
                                                            warehouseInput,
                                                            aisleInput,
                                                            shelfInput,
                                                            binInput,
                                                            technicianInput,
                                                            "نقل منظم لموقع جديد"
                                                        )
                                                        isEditingLocation = false
                                                    }
                                                ) {
                                                    Text("حفظ الموقع الجديد")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Immutable Audit Trail Timeline Tab
                            if (partLogs.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "لا توجد حركات مسجلة لهذه القطعة بعد",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(partLogs) { log ->
                                        AuditLogItemCard(log = log, dateFormat = dateFormat)
                                    }
                                }
                            }
                        }

                        3 -> {
                            // On-Device AI Depletion Prediction Tab
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AmberCaution
                                            )
                                            Text(
                                                text = "التحليل التنبؤي الذكي للمخزون (On-Device AI)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = aiPrediction.urgencyLevel.titleAr,
                                            fontWeight = FontWeight.Bold,
                                            color = when (aiPrediction.urgencyLevel) {
                                                FireStockAI.DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY -> FireRedBright
                                                FireStockAI.DepletionPrediction.UrgencyLevel.HIGH_ALERT -> AmberCaution
                                                else -> StockGreen
                                            },
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = aiPrediction.adviceTextAr,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("معدل الاستهلاك الأسبوعي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("${"%.1f".format(aiPrediction.consumptionRatePerWeek)} ${part.unit}/أسبوع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            Column {
                                                Text("الأيام المتوقعة قبل النفاد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    if (aiPrediction.estimatedDaysRemaining != null) "${aiPrediction.estimatedDaysRemaining} يوماً" else "مخزون آمن",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if ((aiPrediction.estimatedDaysRemaining ?: 999) < 14) FireRedBright else MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Column {
                                                Text("الكمية المقترح توريدها", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("${aiPrediction.recommendedOrderQty} ${part.unit}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WaterBlueBright)
                                            }
                                        }

                                        if (aiPrediction.affectedFireTrucks.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "العجلات الأكثر استهلاكاً لهذه القطعة: ${aiPrediction.affectedFireTrucks.joinToString(" • ")}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationStepChip(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AuditLogItemCard(log: InventoryLog, dateFormat: SimpleDateFormat) {
    val isCheckIn = log.actionType == InventoryLog.ActionType.CHECK_IN
    val isCheckOut = log.actionType == InventoryLog.ActionType.CHECK_OUT

    val badgeColor = when (log.actionType) {
        InventoryLog.ActionType.CHECK_IN -> StockGreen
        InventoryLog.ActionType.CHECK_OUT -> FireRedBright
        InventoryLog.ActionType.ADJUST_LOCATION -> WaterBlueBright
        InventoryLog.ActionType.STOCK_AUDIT -> AmberCaution
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.actionType.labelAr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = badgeColor
                    )
                }

                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "الفني: ${log.technicianName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (log.fireTruckUnit.isNotBlank()) {
                        Text(
                            text = "العجلة/الجهة: ${log.fireTruckUnit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (log.quantityChange != 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (log.quantityChange > 0) "+${log.quantityChange}" else "${log.quantityChange}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (log.quantityChange > 0) StockGreen else FireRedBright
                        )
                        Text(
                            text = "الرصيد: ${log.previousQuantity} ➔ ${log.newQuantity}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات: ${log.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        com.example.util.DocumentPrintAndExportHelper.printLogEntryDocument(context, log)
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = "طباعة المستند",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "طباعة مستند الحركة",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
