package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.ui.theme.StockGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDialog(
    initialPart: SparePart?,
    allParts: List<SparePart>,
    onDismiss: () -> Unit,
    onConfirm: (partId: Long, quantity: Int, supplier: String, technician: String, notes: String) -> Unit
) {
    var selectedPart by remember { mutableStateOf(initialPart ?: allParts.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var supplierText by remember { mutableStateOf(selectedPart?.supplier ?: "شركة التوريدات المعتمدة") }
    var technicianText by remember { mutableStateOf("م. خالد العتيبي (مسؤول الإسناد)") }
    var notesText by remember { mutableStateOf("استلام دفعة جديدة من المورد ومطابقة المواصفات") }
    var expandedPartSelect by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                            color = StockGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = StockGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إدخال مواد (توريد وارد)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تسجيل القطع الجديدة وزيادة الرصيد المخزني",
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
                            label = { Text("قطعة الغيار المراد توريدها") },
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
                                                "${part.partNumber} • الرصيد الحالي: ${part.quantity} ${part.unit}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPart = part
                                        supplierText = part.supplier
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
                                text = "رقم القطعة: ${initialPart.partNumber} • موقع الرف: ${initialPart.binLocation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "الرصيد الحالي: ${initialPart.quantity} ${initialPart.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
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
                    label = { Text("الكمية المستلمة (${selectedPart?.unit ?: "قطعة"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("check_in_quantity_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Supplier Input
                OutlinedTextField(
                    value = supplierText,
                    onValueChange = { supplierText = it },
                    label = { Text("اسم المورد / جهة التوريد") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Technician Input
                OutlinedTextField(
                    value = technicianText,
                    onValueChange = { technicianText = it },
                    label = { Text("اسم الفني / أمين المستودع المستلم") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Input
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("ملاحظات ورقم إذن التوريد") },
                    leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
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
                                com.example.util.DocumentPrintAndExportHelper.printCheckInDocument(
                                    context = context,
                                    part = selectedPart!!,
                                    quantity = qty,
                                    supplier = supplierText,
                                    technician = technicianText,
                                    notes = notesText
                                )
                            }
                        },
                        enabled = selectedPart != null
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة السند", fontSize = 12.sp)
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
                                    errorMessage = "يرجى إدخال كمية صحيحة أكبر من صفر"
                                    return@Button
                                }
                                onConfirm(selectedPart!!.id, qty, supplierText, technicianText, notesText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                            modifier = Modifier.testTag("confirm_check_in_button")
                        ) {
                            Text("تأكيد الإدخال", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
