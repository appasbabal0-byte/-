package com.example.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SparePart
import com.example.ui.theme.FireRedBright

@Composable
fun BarcodeScannerDialog(
    sampleParts: List<SparePart>,
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Animated laser beam for camera viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(580.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FireRedBright.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = FireRedBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "قارئ الباركود والوسائط المدمج",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "مسح فوري لرموز القطع (1D / QR Code)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { isFlashlightOn = !isFlashlightOn }) {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = "فلاش",
                                tint = if (isFlashlightOn) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Viewfinder Canvas with glowing laser line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Color(0xFF090A0D), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF2A2D3A), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Target box coordinates
                        val boxWidth = canvasWidth * 0.75f
                        val boxHeight = canvasHeight * 0.75f
                        val left = (canvasWidth - boxWidth) / 2
                        val top = (canvasHeight - boxHeight) / 2

                        // Draw Corner Guides
                        val cornerLen = 28f
                        val cornerColor = FireRedBright

                        // Top-Left
                        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = 5f)
                        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = 5f)

                        // Top-Right
                        drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLen, top), strokeWidth = 5f)
                        drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLen), strokeWidth = 5f)

                        // Bottom-Left
                        drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left + cornerLen, top + boxHeight), strokeWidth = 5f)
                        drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLen), strokeWidth = 5f)

                        // Bottom-Right
                        drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLen, top + boxHeight), strokeWidth = 5f)
                        drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLen), strokeWidth = 5f)

                        // Draw Animated Laser Scan Line
                        val laserY = top + (boxHeight * laserPosition)
                        drawLine(
                            color = FireRedBright,
                            start = Offset(left + 6f, laserY),
                            end = Offset(left + boxWidth - 6f, laserY),
                            strokeWidth = 3f
                        )
                    }

                    Text(
                        text = "وجّه الكاميرا نحو باركود قطعة الغيار",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Barcode Chips (Instant Test Simulation for emulator & field testing)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تجربة مسح باركود من مستودع الإطفاء (نقرة سريعة):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleParts.take(6).forEach { part ->
                            FilterChip(
                                selected = false,
                                onClick = { onBarcodeScanned(part.barcode) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(part.name.take(20) + "...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(part.barcode, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual barcode entry field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualBarcode,
                        onValueChange = { manualBarcode = it },
                        label = { Text("أو أدخل رقم الباركود يدوياً") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_barcode_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (manualBarcode.isNotBlank()) onBarcodeScanned(manualBarcode.trim())
                        })
                    )

                    Button(
                        onClick = {
                            if (manualBarcode.isNotBlank()) onBarcodeScanned(manualBarcode.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FireRedBright),
                        modifier = Modifier.testTag("manual_barcode_submit_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح")
                    }
                }
            }
        }
    }
}
