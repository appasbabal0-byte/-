package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FireStockAI
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import com.example.ui.dialogs.AuditLogItemCard
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.FireRedBright
import com.example.ui.theme.StockGreen
import com.example.ui.theme.TacticalSurfaceVariant
import com.example.ui.theme.WaterBlueBright
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    parts: List<SparePart>,
    recentLogs: List<InventoryLog>,
    predictions: List<FireStockAI.DepletionPrediction>,
    onOpenCheckIn: () -> Unit,
    onOpenCheckOut: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenVisualAi: () -> Unit,
    onSelectPart: (SparePart) -> Unit,
    onViewAllLogs: () -> Unit,
    onViewAiAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPartsCount = parts.size
    val criticalCount = parts.count { it.stockStatus == SparePart.StockStatus.CRITICAL }
    val uniqueTrucks = recentLogs.map { it.fireTruckUnit }.filter { it.isNotBlank() && !it.contains("المستودع") }.distinct().size
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    val criticalPrediction = predictions.firstOrNull {
        it.urgencyLevel == FireStockAI.DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Depot Visual Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161820))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Try to load hero banner from drawables
                    val heroResId = remember {
                        context.resources.getIdentifier("hero_fire_depot", "drawable", context.packageName)
                    }
                    if (heroResId != 0) {
                        Image(
                            painter = painterResource(id = heroResId),
                            contentDescription = "ورشة ومستودع سيارات الإطفاء",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Scrim gradient for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x88000000),
                                        Color(0xCC0A0B0E),
                                        Color(0xF00A0B0E)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = FireRedBright,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نظام الإطفاء الذكي لإدارة قطع الغيار",
                                color = AmberCaution,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FireStock AI • الاستجابة السريعة والجاهزية",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "إدارة المخزون، تتبع عجلات الإطفاء، والتنبؤ بالنفاد (Offline-First)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 5: Rapid Action Big Buttons (Dashboard Requirement)
        item {
            Column {
                Text(
                    text = "الإجراءات والعمليات الميدانية السريعة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. إدخال مادة (Check-in)
                    QuickActionButton(
                        title = "إدخال مادة",
                        subtitle = "توريد وارد جديد",
                        icon = Icons.Default.AddCircle,
                        accentColor = StockGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_check_in_button"),
                        onClick = onOpenCheckIn
                    )

                    // 2. إخراج مادة (Check-out)
                    QuickActionButton(
                        title = "إخراج مادة",
                        subtitle = "صرف لصيانة عجلة",
                        icon = Icons.Default.RemoveCircle,
                        accentColor = FireRedBright,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_check_out_button"),
                        onClick = onOpenCheckOut
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. مسح باركود (Scan Barcode)
                    QuickActionButton(
                        title = "مسح باركود",
                        subtitle = "قارئ 1D / QR كاميرا",
                        icon = Icons.Default.QrCodeScanner,
                        accentColor = WaterBlueBright,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_scan_barcode_button"),
                        onClick = onOpenScanner
                    )

                    // 4. الجرد الشامل (Full Inventory)
                    QuickActionButton(
                        title = "الجرد الشامل",
                        subtitle = "فحص كافة الأرصدة",
                        icon = Icons.Default.Inventory,
                        accentColor = AmberCaution,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_inventory_button"),
                        onClick = onOpenInventory
                    )
                }
            }
        }

        // Status Metrics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "إجمالي المواد",
                    value = "$totalPartsCount صنف",
                    accentColor = WaterBlueBright,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "حالات حرجة",
                    value = "$criticalCount قطع",
                    accentColor = if (criticalCount > 0) FireRedBright else StockGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "العجلات المخدومة",
                    value = "$uniqueTrucks مركبة",
                    accentColor = AmberCaution,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Proactive On-Device AI Depletion Alert Banner (Section 3.2)
        if (criticalPrediction != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381212)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FireRedBright.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewAiAnalytics() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FireRedBright,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "تنبيه ذكاء اصطناعي بنفاد حرج:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = FireRedBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberCaution, modifier = Modifier.size(13.dp))
                            }
                            Text(
                                text = "${criticalPrediction.partName} - الرصيد (${criticalPrediction.currentStock}) دون حد الأمان!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "العجلات المعرضة للخروج من الخدمة: ${criticalPrediction.affectedFireTrucks.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Section 3.1: Visual Smart Part Recognition Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberCaution.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVisualAi() }
                    .testTag("dashboard_visual_ai_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberCaution.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AmberCaution, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "التعرف البصري الذكي على القطع",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberCaution, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "تعرف على شكل ونوع قطعة الغيار عند تلف أو غياب الباركود",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "فحص ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AmberCaution
                    )
                }
            }
        }

        // Recent Audit Trail Operations
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل العمليات الأخير (Audit Trail)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onViewAllLogs) {
                    Text("عرض الكل ➔", fontSize = 12.sp)
                }
            }
        }

        items(recentLogs.take(5)) { log ->
            AuditLogItemCard(log = log, dateFormat = dateFormat)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier.height(96.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accentColor)
        }
    }
}
