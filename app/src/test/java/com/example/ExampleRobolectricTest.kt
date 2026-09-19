package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.FireStockAI
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FireStock AI", appName)
  }

  @Test
  fun `verify AI depletion prediction calculation`() {
    val samplePart = SparePart(
      id = 1,
      partNumber = "FS-PUMP-01",
      name = "مضخة مياه طرد مركزي",
      category = "مضخات وخراطيم",
      barcode = "FS9928174001",
      binLocation = "WH1-A1-S1-B1",
      warehouse = "المستودع الرئيسي",
      aisle = "الممر A",
      shelf = "الرف 1",
      bin = "الصندوق 1",
      quantity = 2,
      minStock = 3,
      unit = "مضخة",
      supplier = "Rosenbauer",
      compatibility = "عجلات إطفاء متعددة"
    )

    val logs = listOf(
      InventoryLog(
        partId = 1,
        partName = samplePart.name,
        partNumber = samplePart.partNumber,
        actionType = InventoryLog.ActionType.CHECK_OUT,
        quantityChange = -1,
        previousQuantity = 3,
        newQuantity = 2,
        technicianName = "فهد المطيري",
        fireTruckUnit = "عجلة إطفاء 104",
        notes = "صيانة طارئة"
      )
    )

    val prediction = FireStockAI.predictDepletion(samplePart, logs)
    assertEquals(FireStockAI.DepletionPrediction.UrgencyLevel.CRITICAL_EMERGENCY, prediction.urgencyLevel)
    assertNotNull(prediction.adviceTextAr)
  }
}
