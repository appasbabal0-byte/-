package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FireStockAI
import com.example.data.model.SparePart
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.FireRedBright
import com.example.ui.theme.StockGreen
import com.example.ui.theme.WaterBlueBright

@Composable
fun AiAnalyticsScreen(
    predictions: List<FireStockAI.DepletionPrediction>,
    allParts: List<SparePart>,
    onOpenCheckIn: (SparePart) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AmberCaution.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AmberCaution,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "التنبؤ الذكي بنفاد المخزون (On-Device AI)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "تحليل وتوقع استهلاك القطع لتجنب خروج عجلات الإطفاء من الجاهزية",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(predictions) { pred ->
                val matchingPart = allParts.firstOrNull { it.id == pred.partId }

                val borderAndBadgeColor = when (pred.urgencyLevel) {
                    FireStockAI.DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY -> FireRedBright
                    FireStockAI.DepletionPrediction.UrgencyLevel.HIGH_ALERT -> AmberCaution
                    FireStockAI.DepletionPrediction.UrgencyLevel.MODERATE -> WaterBlueBright
                    FireStockAI.DepletionPrediction.UrgencyLevel.STABLE -> StockGreen
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderAndBadgeColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pred.partName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = borderAndBadgeColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderAndBadgeColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = pred.urgencyLevel.titleAr,
                                    color = borderAndBadgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pred.adviceTextAr,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("الرصيد / حد الأمان", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pred.currentStock} / ${pred.minStock}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Column {
                                Text("الاستهلاك المقدر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.1f".format(pred.consumptionRatePerWeek)}/أسبوع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Column {
                                Text("النفاد المتوقع", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (pred.estimatedDaysRemaining != null) "${pred.estimatedDaysRemaining} يوماً" else "مستقر",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = borderAndBadgeColor
                                )
                            }

                            if (matchingPart != null) {
                                Button(
                                    onClick = { onOpenCheckIn(matchingPart) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("طلب توريد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (pred.affectedFireTrucks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "العجلات المعتمدة: ${pred.affectedFireTrucks.joinToString(" • ")}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
