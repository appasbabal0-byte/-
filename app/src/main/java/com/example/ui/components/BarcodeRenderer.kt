package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * 1D Code128-Style Barcode Generator & Renderer in Compose
 * Draws deterministic bar patterns generated from the barcode string
 */
@Composable
fun Barcode1DView(
    barcodeText: String,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Generate pseudo-bars deterministically based on barcode string hash
                val hash = barcodeText.hashCode().absoluteValue
                val seed = if (hash == 0) 123456 else hash
                val barCount = 48
                val barWidth = canvasWidth / (barCount * 1.5f)

                // Draw start guard
                drawRect(
                    color = barColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(barWidth * 1.5f, canvasHeight)
                )

                var currentX = barWidth * 2.5f

                for (i in 0 until barCount) {
                    val bit1 = ((seed shr (i % 31)) and 1) == 1
                    val charVal = if (barcodeText.isNotEmpty()) barcodeText[i % barcodeText.length].code else 7
                    val bit2 = ((charVal shr (i % 7)) and 1) == 1
                    val isBlack = (i % 2 == 0) || bit1 || (bit2 && i % 3 != 0)

                    if (isBlack) {
                        val thickness = if ((charVal + i) % 3 == 0) barWidth * 1.8f else barWidth
                        drawRect(
                            color = barColor,
                            topLeft = Offset(currentX, 0f),
                            size = Size(thickness, canvasHeight)
                        )
                        currentX += thickness + barWidth * 0.5f
                    } else {
                        currentX += barWidth * 1.2f
                    }

                    if (currentX >= canvasWidth - barWidth * 3) break
                }

                // Draw end guard
                drawRect(
                    color = barColor,
                    topLeft = Offset(canvasWidth - barWidth * 2f, 0f),
                    size = Size(barWidth * 1.8f, canvasHeight)
                )
            }

            Text(
                text = barcodeText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = barColor,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 2D QR Code Matrix Simulator in Compose
 * Draws a clean 2D matrix pattern with finder patterns at the corners
 */
@Composable
fun QrCodeView(
    dataText: String,
    modifier: Modifier = Modifier,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(110.dp)) {
                val matrixSize = 21
                val cellSize = size.width / matrixSize
                val hash = dataText.hashCode().absoluteValue

                // Function to draw finder pattern (7x7 with 3x3 center)
                fun drawFinderPattern(startX: Int, startY: Int) {
                    // Outer 7x7
                    for (r in 0 until 7) {
                        for (c in 0 until 7) {
                            if (r == 0 || r == 6 || c == 0 || c == 6) {
                                drawRect(
                                    color = qrColor,
                                    topLeft = Offset((startX + c) * cellSize, (startY + r) * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        }
                    }
                    // Inner 3x3
                    for (r in 2..4) {
                        for (c in 2..4) {
                            drawRect(
                                color = qrColor,
                                topLeft = Offset((startX + c) * cellSize, (startY + r) * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }

                // Draw top-left, top-right, bottom-left finders
                drawFinderPattern(0, 0)
                drawFinderPattern(matrixSize - 7, 0)
                drawFinderPattern(0, matrixSize - 7)

                // Fill data cells
                for (r in 0 until matrixSize) {
                    for (c in 0 until matrixSize) {
                        // Skip finders
                        val inTopLeft = (r < 8 && c < 8)
                        val inTopRight = (r < 8 && c >= matrixSize - 8)
                        val inBottomLeft = (r >= matrixSize - 8 && c < 8)

                        if (!inTopLeft && !inTopRight && !inBottomLeft) {
                            val seedVal = ((hash shr ((r * 3 + c) % 29)) and 1) == 1
                            val mathVal = (r * c + dataText.length) % 3 == 0
                            if (seedVal xor mathVal) {
                                drawRect(
                                    color = qrColor,
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = Size(cellSize * 0.92f, cellSize * 0.92f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
