package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.FireRedBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartDialog(
    onDismiss: () -> Unit,
    onGenerateBarcode: () -> String,
    onConfirm: (
        partNumber: String,
        name: String,
        category: String,
        barcode: String,
        binLocation: String,
        warehouse: String,
        aisle: String,
        shelf: String,
        bin: String,
        quantity: Int,
        minStock: Int,
        unit: String,
        supplier: String,
        compatibility: String,
        notes: String,
        technician: String
    ) -> Unit
) {
    val categories = listOf(
        "مضخات وخراطيم",
        "المحرك وناقل الحركة",
        "الأنظمة الهيدروليكية",
        "الكهرباء والإنارة",
        "أجهزة الإطفاء والمكافحة"
    )

    var name by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("FS-" + (100..999).random().toString()) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var barcode by remember { mutableStateOf(onGenerateBarcode()) }
    var warehouse by remember { mutableStateOf("المستودع الرئيسي 1") }
    var aisle by remember { mutableStateOf("الممر A") }
    var shelf by remember { mutableStateOf("الرف 2") }
    var bin by remember { mutableStateOf("الصندوق 1") }
    var quantityText by remember { mutableStateOf("5") }
    var minStockText by remember { mutableStateOf("2") }
    var unit by remember { mutableStateOf("قطعة") }
    var supplier by remember { mutableStateOf("شركة التوريدات المعتمدة") }
    var compatibility by remember { mutableStateOf("عجلات إطفاء متعددة") }
    var notes by remember { mutableStateOf("") }
    var technician by remember { mutableStateOf("مسؤول المستودع") }

    var expandedCategory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp),
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
                    Column {
                        Text(
                            text = "تسجيل مادة جديدة في النظام",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "إضافة قطعة غيار لعجلات الإطفاء مع توليد الباركود والموقع",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Part Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المادة (العلمي والتجاري) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_part_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Part Number & Category
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = partNumber,
                        onValueChange = { partNumber = it },
                        label = { Text("رقم القطعة (Part No) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("فئة القطعة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barcode with Auto-Generate button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("رمز الباركود (Barcode) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedButton(
                        onClick = { barcode = onGenerateBarcode() }
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توليد")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Structured Location Coordinates
                Text(
                    text = "تحديد موقع التخزين في المستودع:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = warehouse,
                        onValueChange = { warehouse = it },
                        label = { Text("المستودع") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = aisle,
                        onValueChange = { aisle = it },
                        label = { Text("الممر") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = shelf,
                        onValueChange = { shelf = it },
                        label = { Text("الرف") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bin,
                        onValueChange = { bin = it },
                        label = { Text("الصندوق") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Min Stock & Unit (عدد أو طقم)
                Text(
                    text = "نوع تسجيل المادة والوحدة المخزنية:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unitOptions = listOf("عدد", "طقم", "قطعة", "متر", "لتر")
                    unitOptions.forEach { opt ->
                        val isSelected = unit == opt
                        androidx.compose.material3.FilterChip(
                            selected = isSelected,
                            onClick = { unit = opt },
                            label = { Text(opt, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (opt == "طقم") MaterialTheme.colorScheme.secondary else FireRedBright,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantityText = it },
                        label = { Text("الكمية المتوفرة ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minStockText = it },
                        label = { Text("حد الإنذار الأدنى") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compatibility & Supplier
                OutlinedTextField(
                    value = compatibility,
                    onValueChange = { compatibility = it },
                    label = { Text("التوافق مع سيارات الإطفاء") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("اسم المورد / الشركة الصانعة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || partNumber.isBlank() || barcode.isBlank()) {
                                errorMessage = "يرجى تعبئة الحقول الإلزامية (اسم المادة، رقم القطعة، الباركود)"
                                return@Button
                            }
                            val qty = quantityText.toIntOrNull() ?: 0
                            val min = minStockText.toIntOrNull() ?: 1
                            val binLoc = "WH1-${aisle.takeLast(1)}-S${shelf.takeLast(1)}"
                            onConfirm(
                                partNumber,
                                name,
                                selectedCategory,
                                barcode,
                                binLoc,
                                warehouse,
                                aisle,
                                shelf,
                                bin,
                                qty,
                                min,
                                unit,
                                supplier,
                                compatibility,
                                notes,
                                technician
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FireRedBright),
                        modifier = Modifier.testTag("confirm_create_part_button")
                    ) {
                        Text("تسجيل المادة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
