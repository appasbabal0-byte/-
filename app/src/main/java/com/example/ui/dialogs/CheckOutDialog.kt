package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SparePart
import com.example.ui.theme.FireRedBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutDialog(
    initialPart: SparePart?,
    allParts: List<SparePart>,
    onDismiss: () -> Unit,
    onConfirm: (partId: Long, quantity: Int, technician: String, fireTruckUnit: String, notes: String) -> Unit
) {
    var selectedPart by remember { mutableStateOf(initialPart ?: allParts.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var technicianText by remember { mutableStateOf("الفني راشد الشمري") }
    var fireTruckText by remember { mutableStateOf("عجلة إطفاء 104 (فهد 6x6)") }
    var notesText by remember { mutableStateOf("صيانة دورية عاجلة لاستبدال القطعة المستهلكة") }
    var expandedPartSelect by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonFireTrucks = listOf(
        "عجلة إطفاء 104 (فهد 6x6)",
        "صهريج مياه 202 (مان TGM)",
        "عجلة إنقاذ الحوادث 302",
        "سلم هيدروليكي 405 (روزنباور)",
        "فرقة التدخل السريع 101"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FireRedBright.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircle,
                                    contentDescription = null,
                                    tint = FireRedBright
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إخراج مواد (صيانة عجلة)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "صرف القطعة لسيارة إطفاء محددة مع التوثيق",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Part Selection Dropdown if not fixed
                if (initialPart == null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPartSelect,
                        onExpandedChange = { expandedPartSelect = !expandedPartSelect }
                    ) {
                        OutlinedTextField(
                            value = selectedPart?.name ?: "اختر قطعة الغيار...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("قطعة الغيار المراد صرفها") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPartSelect) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPartSelect,
                            onDismissRequest = { expandedPartSelect = false }
                        ) {
                            allParts.forEach { part ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(part.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${part.partNumber} • المتاح: ${part.quantity} ${part.unit}",
                                                fontSize = 11.sp,
                                                color = if (part.quantity <= part.minStock) FireRedBright else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPart = part
                                        expandedPartSelect = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Display selected part card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = initialPart.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "رقم القطعة: ${initialPart.partNumber} • الرف: ${initialPart.binLocation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "الرصيد المتاح حالياً: ${initialPart.quantity} ${initialPart.unit}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (initialPart.quantity <= initialPart.minStock) FireRedBright else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) quantityText = it
                    },
                    label = { Text("الكمية المطلوبة للصرف (${selectedPart?.unit ?: "قطعة"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("check_out_quantity_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Fire Truck Unit Selection
                OutlinedTextField(
                    value = fireTruckText,
                    onValueChange = { fireTruckText = it },
                    label = { Text("عجلة الإطفاء المستلمة (إلزامي)") },
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fire_truck_unit_input"),
                    singleLine = true
                )

                // Quick selector chips for common fire trucks
                Text(
                    text = "اختيار سريع لمركبات الإطفاء العاملة:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonFireTrucks.forEach { truck ->
                        FilterChip(
                            selected = fireTruckText == truck,
                            onClick = { fireTruckText = truck },
                            label = { Text(truck, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Technician Input
                OutlinedTextField(
                    value = technicianText,
                    onValueChange = { technicianText = it },
                    label = { Text("اسم الفني المنفذ (إلزامي)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("technician_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Maintenance / Emergency Reason
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("سبب الصرف / رقم بلاغ الحريق أو الصيانة") },
                    leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            val qty = quantityText.toIntOrNull() ?: 1
                            if (selectedPart != null) {
                                com.example.util.DocumentPrintAndExportHelper.printCheckOutDocument(
                                    context = context,
                                    part = selectedPart!!,
                                    quantity = qty,
                                    technician = technicianText,
                                    fireTruckUnit = fireTruckText,
                                    notes = notesText
                                )
                            }
                        },
                        enabled = selectedPart != null
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة إذن الصرف", fontSize = 12.sp)
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val qty = quantityText.toIntOrNull() ?: 0
                                if (selectedPart == null) {
                                    errorMessage = "يرجى اختيار قطعة الغيار"
                                    return@Button
                                }
                                if (qty <= 0) {
                                    errorMessage = "يرجى تحديد كمية صحيحة أكبر من صفر"
                                    return@Button
                                }
                                if (qty > selectedPart!!.quantity) {
                                    errorMessage = "الكمية المطلوبة ($qty) تتجاوز الرصيد المتوفر (${selectedPart!!.quantity})!"
                                    return@Button
                                }
                                if (fireTruckText.isBlank()) {
                                    errorMessage = "إلزام تحديد اسم أو رقم عجلة الإطفاء المستلمة"
                                    return@Button
                                }
                                if (technicianText.isBlank()) {
                                    errorMessage = "إلزام تحديد اسم الفني المستلم"
                                    return@Button
                                }
                                onConfirm(selectedPart!!.id, qty, technicianText, fireTruckText, notesText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FireRedBright),
                            modifier = Modifier.testTag("confirm_check_out_button")
                        ) {
                            Text("صرف وإخراج", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
