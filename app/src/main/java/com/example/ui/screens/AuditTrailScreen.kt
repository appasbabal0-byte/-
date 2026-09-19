package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryLog
import com.example.ui.dialogs.AuditLogItemCard
import com.example.ui.theme.FireRedBright
import com.example.ui.theme.StockGreen
import com.example.ui.theme.WaterBlueBright
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AuditTrailScreen(
    logs: List<InventoryLog>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "إدخال (توريد)", "إخراج (صيانة عجلة)", "تعديل موقع")

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "إدخال (توريد)" -> logs.filter { it.actionType == InventoryLog.ActionType.CHECK_IN }
            "إخراج (صيانة عجلة)" -> logs.filter { it.actionType == InventoryLog.ActionType.CHECK_OUT }
            "تعديل موقع" -> logs.filter { it.actionType == InventoryLog.ActionType.ADJUST_LOCATION }
            else -> logs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "سجل العمليات التاريخي والتوثيق الزمني (Audit Trail)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "سجل آلي بالثانية غير قابل للتعديل يوثق حركة القطع، الفنيين، وعجلات الإطفاء",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (filter) {
                            "إدخال (توريد)" -> StockGreen
                            "إخراج (صيانة عجلة)" -> FireRedBright
                            "تعديل موقع" -> WaterBlueBright
                            else -> MaterialTheme.colorScheme.primary
                        },
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val context = androidx.compose.ui.platform.LocalContext.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "إجمالي العمليات الموثقة: ${filteredLogs.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Export filtered/all logs to Excel
                OutlinedButton(
                    onClick = {
                        when (selectedFilter) {
                            "إدخال (توريد)" -> com.example.util.DocumentPrintAndExportHelper.exportCheckInsToExcel(context, logs)
                            "إخراج (صيانة عجلة)" -> com.example.util.DocumentPrintAndExportHelper.exportCheckOutsToExcel(context, logs)
                            else -> com.example.util.DocumentPrintAndExportHelper.exportAllLogsToExcel(context, filteredLogs)
                        }
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = StockGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (selectedFilter) {
                            "إدخال (توريد)" -> "تصدير الإدخال Excel"
                            "إخراج (صيانة عجلة)" -> "تصدير الصرف Excel"
                            else -> "تصدير Excel"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StockGreen
                    )
                }

                // Print latest or active transaction
                if (filteredLogs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val firstLog = filteredLogs.first()
                            com.example.util.DocumentPrintAndExportHelper.printLogEntryDocument(context, firstLog)
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "طباعة آخر حركة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد عمليات مسجلة في هذا التصنيف",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    AuditLogItemCard(log = log, dateFormat = dateFormat)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
