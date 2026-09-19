package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentPrintAndExportHelper {

    private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    private val dateFormatSimple = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())

    /**
     * طباعة مستند إدخال مواد (سند توريد واستلام رسمي)
     */
    fun printCheckInDocument(
        context: Context,
        part: SparePart,
        quantity: Int,
        supplier: String,
        technician: String,
        notes: String
    ) {
        val documentNumber = "IN-${System.currentTimeMillis().toString().takeLast(6)}"
        val htmlContent = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8"/>
                <title>سند إدخال وتوريد مواد - $documentNumber</title>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1a1a1a; direction: rtl; }
                    .header { text-align: center; border-bottom: 2px solid #b71c1c; padding-bottom: 12px; margin-bottom: 20px; }
                    .title { font-size: 22px; font-weight: bold; color: #b71c1c; margin: 0; }
                    .subtitle { font-size: 14px; color: #555; margin-top: 5px; }
                    .meta-table, .data-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    .meta-table td { padding: 8px; font-size: 13px; }
                    .data-table th, .data-table td { border: 1px solid #ccc; padding: 10px; text-align: right; font-size: 13px; }
                    .data-table th { background-color: #f2f2f2; font-weight: bold; }
                    .badge { display: inline-block; padding: 4px 8px; background: #e8f5e9; color: #2e7d32; border-radius: 4px; font-weight: bold; }
                    .signatures { margin-top: 40px; width: 100%; }
                    .signatures td { width: 50%; padding-top: 30px; font-weight: bold; font-size: 14px; text-align: center; }
                    .footer { text-align: center; font-size: 11px; color: #888; margin-top: 30px; border-top: 1px dashed #ccc; padding-top: 10px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">إدارة الإطفاء والدفاع المدني - مستودع قطع الغيار</h1>
                    <div class="subtitle">سند إدخال وتوريد مواد ومعدات (رقم السند: $documentNumber)</div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td><strong>تاريخ ووقت التوريد:</strong> ${dateTimeFormat.format(Date())}</td>
                        <td><strong>أمين المستودع المستلم:</strong> ${technician.ifBlank { "مسؤول المستودع" }}</td>
                    </tr>
                    <tr>
                        <td><strong>المورد / جهة التوريد:</strong> ${supplier.ifBlank { part.supplier }}</td>
                        <td><strong>موقع التخزين المخصص:</strong> ${part.binLocation} (${part.warehouse} - ${part.shelf})</td>
                    </tr>
                </table>

                <table class="data-table">
                    <thead>
                        <tr>
                            <th>رقم القطعة</th>
                            <th>اسم المادة / القطعة</th>
                            <th>التصنيف</th>
                            <th>الوحدة</th>
                            <th>الكمية الموردة</th>
                            <th>الرصيد بعد التوريد</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>${part.partNumber}</strong></td>
                            <td>${part.name}</td>
                            <td>${part.category}</td>
                            <td><span class="badge">${part.unit}</span></td>
                            <td style="color: #2e7d32; font-weight: bold; font-size: 15px;">+$quantity</td>
                            <td>${part.quantity + quantity} ${part.unit}</td>
                        </tr>
                    </tbody>
                </table>

                <div style="background: #f9f9f9; border: 1px solid #e0e0e0; padding: 12px; border-radius: 6px; font-size: 13px;">
                    <strong>ملاحظات ورقم إذن التوريد:</strong> ${notes.ifBlank { "تم فحص المواد ومطابقتها للمواصفات الفنية وإدخالها للرصيد الفعلي." }}
                </div>

                <table class="signatures">
                    <tr>
                        <td>توقيع أمين المستودع المستلم:<br/><br/>______________________</td>
                        <td>توقيع مندوب المورد / جهة التسليم:<br/><br/>______________________</td>
                    </tr>
                </table>

                <div class="footer">
                    تم إصدار هذا المستند آلياً بواسطة نظام FireStock AI لإدارة مخزون عجلات الإطفاء
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "سند_إدخال_$documentNumber", htmlContent)
    }

    /**
     * طباعة مستند إخراج مواد (إذن صرف لصيانة عجلة إطفاء)
     */
    fun printCheckOutDocument(
        context: Context,
        part: SparePart,
        quantity: Int,
        technician: String,
        fireTruckUnit: String,
        notes: String
    ) {
        val documentNumber = "OUT-${System.currentTimeMillis().toString().takeLast(6)}"
        val htmlContent = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8"/>
                <title>إذن صرف مواد لصيانة عجلة إطفاء - $documentNumber</title>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1a1a1a; direction: rtl; }
                    .header { text-align: center; border-bottom: 2px solid #c62828; padding-bottom: 12px; margin-bottom: 20px; }
                    .title { font-size: 22px; font-weight: bold; color: #c62828; margin: 0; }
                    .subtitle { font-size: 14px; color: #555; margin-top: 5px; }
                    .meta-table, .data-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    .meta-table td { padding: 8px; font-size: 13px; }
                    .data-table th, .data-table td { border: 1px solid #ccc; padding: 10px; text-align: right; font-size: 13px; }
                    .data-table th { background-color: #ffebee; font-weight: bold; }
                    .badge { display: inline-block; padding: 4px 8px; background: #ffebee; color: #c62828; border-radius: 4px; font-weight: bold; }
                    .signatures { margin-top: 40px; width: 100%; }
                    .signatures td { width: 50%; padding-top: 30px; font-weight: bold; font-size: 14px; text-align: center; }
                    .footer { text-align: center; font-size: 11px; color: #888; margin-top: 30px; border-top: 1px dashed #ccc; padding-top: 10px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">إدارة الإطفاء والدفاع المدني - مستودع الصيانة الفنية</h1>
                    <div class="subtitle">إذن صرف مواد وقطع غيار لصيانة عجلة إطفاء (رقم: $documentNumber)</div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td><strong>تاريخ ووقت الصرف:</strong> ${dateTimeFormat.format(Date())}</td>
                        <td><strong>العجلة / المركبة المستلمة:</strong> <span style="font-size:15px; font-weight:bold; color:#c62828;">$fireTruckUnit</span></td>
                    </tr>
                    <tr>
                        <td><strong>اسم الفني المستلم:</strong> $technician</td>
                        <td><strong>موقع الصرف من الرف:</strong> ${part.binLocation}</td>
                    </tr>
                </table>

                <table class="data-table">
                    <thead>
                        <tr>
                            <th>رقم القطعة</th>
                            <th>اسم المادة / القطعة</th>
                            <th>نوع الوحدة</th>
                            <th>الكمية المصروفة</th>
                            <th>الرصيد المتبقي</th>
                            <th>توافق الطراز</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>${part.partNumber}</strong></td>
                            <td>${part.name}</td>
                            <td>${part.unit}</td>
                            <td style="color: #c62828; font-weight: bold; font-size: 15px;">-$quantity</td>
                            <td>${part.quantity - quantity} ${part.unit}</td>
                            <td>${part.compatibility}</td>
                        </tr>
                    </tbody>
                </table>

                <div style="background: #fff3e0; border: 1px solid #ffe0b2; padding: 12px; border-radius: 6px; font-size: 13px;">
                    <strong>سبب الصرف وطبيعة الصيانة:</strong> ${notes.ifBlank { "تركيب واستبدال مباشر لضمان الجاهزية التشغيلية للطوارئ." }}
                </div>

                <table class="signatures">
                    <tr>
                        <td>توقيع الفني المستلم:<br/><br/>______________________</td>
                        <td>اعتماد مسؤول مستودع الإطفاء:<br/><br/>______________________</td>
                    </tr>
                </table>

                <div class="footer">
                    تم إنشاء إذن الصرف بموجب التوثيق الأمني المعتمد في نظام FireStock AI
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "إذن_صرف_$documentNumber", htmlContent)
    }

    /**
     * طباعة مستند جرد المواد الشامل
     */
    fun printInventoryAuditReport(
        context: Context,
        parts: List<SparePart>
    ) {
        val documentNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
        val rows = StringBuilder()
        for ((index, part) in parts.withIndex()) {
            val statusColor = when (part.stockStatus) {
                SparePart.StockStatus.GOOD -> "#2e7d32"
                SparePart.StockStatus.WARNING -> "#f57f17"
                SparePart.StockStatus.CRITICAL -> "#c62828"
            }
            val statusLabel = when (part.stockStatus) {
                SparePart.StockStatus.GOOD -> "ممتاز"
                SparePart.StockStatus.WARNING -> "حد الطلب"
                SparePart.StockStatus.CRITICAL -> "حرج / نفاد"
            }

            rows.append("""
                <tr>
                    <td style="text-align:center;">${index + 1}</td>
                    <td><strong>${part.partNumber}</strong></td>
                    <td>${part.name}</td>
                    <td>${part.category}</td>
                    <td style="text-align:center;"><strong>${part.unit}</strong></td>
                    <td style="text-align:center; font-weight:bold;">${part.quantity}</td>
                    <td style="text-align:center;">${part.minStock}</td>
                    <td style="text-align:center;"><span style="color:${statusColor}; font-weight:bold;">${statusLabel}</span></td>
                    <td>${part.binLocation} (${part.shelf})</td>
                </tr>
            """.trimIndent())
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8"/>
                <title>تقرير الجرد الشامل للمستودع - $documentNumber</title>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1a1a1a; direction: rtl; }
                    .header { text-align: center; border-bottom: 2px solid #b71c1c; padding-bottom: 10px; margin-bottom: 16px; }
                    .title { font-size: 20px; font-weight: bold; color: #b71c1c; margin: 0; }
                    .meta { font-size: 12px; color: #555; margin-top: 5px; }
                    .data-table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 11px; }
                    .data-table th, .data-table td { border: 1px solid #bbb; padding: 6px 8px; text-align: right; }
                    .data-table th { background-color: #f2f2f2; font-weight: bold; }
                    .signatures { margin-top: 30px; width: 100%; }
                    .signatures td { width: 50%; padding-top: 20px; font-weight: bold; font-size: 13px; text-align: center; }
                    .footer { text-align: center; font-size: 10px; color: #888; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">كشف الجرد الفعلي لقطع غيار ومعدات عجلات الإطفاء</h1>
                    <div class="meta">تاريخ التقرير: ${dateTimeFormat.format(Date())} | إجمالي المواد المفحوصة: ${parts.size} مادة</div>
                </div>

                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width:30px; text-align:center;">م</th>
                            <th>رقم القطعة</th>
                            <th>اسم المادة</th>
                            <th>التصنيف</th>
                            <th style="text-align:center;">نوع الوحدة</th>
                            <th style="text-align:center;">الرصيد الفعلي</th>
                            <th style="text-align:center;">حد الأمان</th>
                            <th style="text-align:center;">حالة المخزون</th>
                            <th>الموقع التخزيني</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <table class="signatures">
                    <tr>
                        <td>توقيع لجنة الجرد الفني:<br/><br/>______________________</td>
                        <td>اعتماد مدير المستودعات والجاهزية:<br/><br/>______________________</td>
                    </tr>
                </table>

                <div class="footer">
                    نظام FireStock AI - تقرير رسمي معتمد للجرد الميداني
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "كشف_جرد_المستودع_$documentNumber", htmlContent)
    }

    /**
     * طباعة حركة معينة من سجل العمليات
     */
    fun printLogEntryDocument(
        context: Context,
        log: InventoryLog
    ) {
        val documentNumber = "LOG-${log.id}"
        val actionTitle = when (log.actionType) {
            InventoryLog.ActionType.CHECK_IN -> "سند إدخال وتوريد رسمي"
            InventoryLog.ActionType.CHECK_OUT -> "إذن صرف وإخراج مواد"
            InventoryLog.ActionType.ADJUST_LOCATION -> "محضر نقل وتعديل موقع تخزين"
            InventoryLog.ActionType.STOCK_AUDIT -> "محضر جرد وتدقيق رصيد"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8"/>
                <title>$actionTitle - $documentNumber</title>
                <style>
                    body { font-family: sans-serif; margin: 25px; color: #1a1a1a; direction: rtl; }
                    .header { text-align: center; border-bottom: 2px solid #b71c1c; padding-bottom: 12px; margin-bottom: 20px; }
                    .title { font-size: 22px; font-weight: bold; color: #b71c1c; margin: 0; }
                    .data-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    .data-table th, .data-table td { border: 1px solid #ccc; padding: 10px; text-align: right; font-size: 13px; }
                    .data-table th { background-color: #f5f5f5; font-weight: bold; width: 30%; }
                    .signatures { margin-top: 40px; width: 100%; }
                    .signatures td { width: 50%; padding-top: 30px; font-weight: bold; font-size: 14px; text-align: center; }
                    .footer { text-align: center; font-size: 11px; color: #888; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">إدارة مستودعات الإطفاء والدفاع المدني</h1>
                    <div style="font-size: 15px; color: #555; margin-top: 5px;">$actionTitle (رقم المرجع: $documentNumber)</div>
                </div>

                <table class="data-table">
                    <tr>
                        <th>نوع الحركة:</th>
                        <td><strong>${log.actionType.labelAr}</strong></td>
                    </tr>
                    <tr>
                        <th>تاريخ وتوقيت العملية:</th>
                        <td>${dateTimeFormat.format(Date(log.timestamp))}</td>
                    </tr>
                    <tr>
                        <th>اسم المادة / القطعة:</th>
                        <td>${log.partName} (${log.partNumber})</td>
                    </tr>
                    <tr>
                        <th>الكمية المنفذة:</th>
                        <td style="font-weight:bold; font-size:16px;">${if (log.quantityChange > 0) "+${log.quantityChange}" else "${log.quantityChange}"}</td>
                    </tr>
                    <tr>
                        <th>الرصيد قبل ➔ بعد:</th>
                        <td>${log.previousQuantity} ➔ <strong>${log.newQuantity}</strong></td>
                    </tr>
                    <tr>
                        <th>الفني أو أمين المستودع المسؤول:</th>
                        <td>${log.technicianName}</td>
                    </tr>
                    <tr>
                        <th>العجلة / جهة الصرف أو التوريد:</th>
                        <td>${log.fireTruckUnit.ifBlank { "المستودع الرئيسي" }}</td>
                    </tr>
                    <tr>
                        <th>ملاحظات العملية:</th>
                        <td>${log.notes.ifBlank { "لا توجد ملاحظات إضافية." }}</td>
                    </tr>
                </table>

                <table class="signatures">
                    <tr>
                        <td>توقيع الفني / المنفذ:<br/><br/>______________________</td>
                        <td>اعتماد المشرف المناوب:<br/><br/>______________________</td>
                    </tr>
                </table>

                <div class="footer">
                    نظام FireStock AI - مستند تدقيق مخزني موثق بالثانية
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "سجل_عملية_$documentNumber", htmlContent)
    }

    // ==========================================
    // EXPORT TO CSV / EXCEL (.csv / .xls)
    // ==========================================

    /**
     * تصدير كشف جرد المواد على شكل ملف إكسل متوافق (CSV مع UTF-8 BOM لفتح العربي بسلاسة)
     */
    fun exportInventoryToExcel(context: Context, parts: List<SparePart>) {
        val fileName = "جرد_المستودع_${dateFormatSimple.format(Date())}.csv"
        val header = "رقم القطعة,اسم المادة,التصنيف,النوع (عدد/طقم),الكمية المتوفرة,حد الطلب الأدنى,حالة المخزون,موقع التخزين,المستودع,الممر,الرف,الصندوق,المورد,التوافق مع العجلات,الباركود\n"

        val sb = StringBuilder()
        sb.append(header)
        for (p in parts) {
            val status = when (p.stockStatus) {
                SparePart.StockStatus.GOOD -> "ممتاز"
                SparePart.StockStatus.WARNING -> "تحذير - اقتراب من الحد الأدنى"
                SparePart.StockStatus.CRITICAL -> "حرج - نفاد أو دون الحد"
            }
            sb.append("\"${escapeCsv(p.partNumber)}\",")
            sb.append("\"${escapeCsv(p.name)}\",")
            sb.append("\"${escapeCsv(p.category)}\",")
            sb.append("\"${escapeCsv(p.unit)}\",")
            sb.append("${p.quantity},")
            sb.append("${p.minStock},")
            sb.append("\"$status\",")
            sb.append("\"${escapeCsv(p.binLocation)}\",")
            sb.append("\"${escapeCsv(p.warehouse)}\",")
            sb.append("\"${escapeCsv(p.aisle)}\",")
            sb.append("\"${escapeCsv(p.shelf)}\",")
            sb.append("\"${escapeCsv(p.bin)}\",")
            sb.append("\"${escapeCsv(p.supplier)}\",")
            sb.append("\"${escapeCsv(p.compatibility)}\",")
            sb.append("\"${escapeCsv(p.barcode)}\"\n")
        }

        saveAndShareFile(context, fileName, sb.toString(), "تصدير كشف جرد قطع الغيار للإكسل")
    }

    /**
     * تصدير سجل الإدخال (التوريدات) على شكل ملف إكسل
     */
    fun exportCheckInsToExcel(context: Context, logs: List<InventoryLog>) {
        val checkIns = logs.filter { it.actionType == InventoryLog.ActionType.CHECK_IN }
        val fileName = "سجل_توريد_المواد_${dateFormatSimple.format(Date())}.csv"
        val header = "رقم الحركة,التاريخ والوقت,رقم القطعة,اسم المادة,نوع الحركة,الكمية الموردة,الرصيد السابق,الرصيد الجديد,المورد / جهة الاستلام,الفني المستلم,الملاحظات\n"

        val sb = StringBuilder()
        sb.append(header)
        for (l in checkIns) {
            sb.append("${l.id},")
            sb.append("\"${dateTimeFormat.format(Date(l.timestamp))}\",")
            sb.append("\"${escapeCsv(l.partNumber)}\",")
            sb.append("\"${escapeCsv(l.partName)}\",")
            sb.append("\"${escapeCsv(l.actionType.labelAr)}\",")
            sb.append("${l.quantityChange},")
            sb.append("${l.previousQuantity},")
            sb.append("${l.newQuantity},")
            sb.append("\"${escapeCsv(l.fireTruckUnit)}\",")
            sb.append("\"${escapeCsv(l.technicianName)}\",")
            sb.append("\"${escapeCsv(l.notes)}\"\n")
        }

        saveAndShareFile(context, fileName, sb.toString(), "تصدير سجل التوريد والإدخال للإكسل")
    }

    /**
     * تصدير سجل الإخراج (صرف صيانة العجلات) على شكل ملف إكسل
     */
    fun exportCheckOutsToExcel(context: Context, logs: List<InventoryLog>) {
        val checkOuts = logs.filter { it.actionType == InventoryLog.ActionType.CHECK_OUT }
        val fileName = "سجل_صرف_المواد_${dateFormatSimple.format(Date())}.csv"
        val header = "رقم الحركة,التاريخ والوقت,رقم القطعة,اسم المادة,نوع الحركة,الكمية المصروفة,الرصيد السابق,الرصيد الجديد,العجلة المستلمة,الفني المستلم,الملاحظات\n"

        val sb = StringBuilder()
        sb.append(header)
        for (l in checkOuts) {
            sb.append("${l.id},")
            sb.append("\"${dateTimeFormat.format(Date(l.timestamp))}\",")
            sb.append("\"${escapeCsv(l.partNumber)}\",")
            sb.append("\"${escapeCsv(l.partName)}\",")
            sb.append("\"${escapeCsv(l.actionType.labelAr)}\",")
            sb.append("${l.quantityChange},")
            sb.append("${l.previousQuantity},")
            sb.append("${l.newQuantity},")
            sb.append("\"${escapeCsv(l.fireTruckUnit)}\",")
            sb.append("\"${escapeCsv(l.technicianName)}\",")
            sb.append("\"${escapeCsv(l.notes)}\"\n")
        }

        saveAndShareFile(context, fileName, sb.toString(), "تصدير سجل الصرف والإخراج للإكسل")
    }

    /**
     * تصدير السجل الشامل لجميع الحركات المخزنية للإكسل
     */
    fun exportAllLogsToExcel(context: Context, logs: List<InventoryLog>) {
        val fileName = "سجل_العمليات_الشامل_${dateFormatSimple.format(Date())}.csv"
        val header = "رقم الحركة,التاريخ والوقت,رقم القطعة,اسم المادة,نوع الحركة,تغير الكمية,الرصيد السابق,الرصيد الجديد,العجلة / الجهة,الفني المسؤول,ملاحظات\n"

        val sb = StringBuilder()
        sb.append(header)
        for (l in logs) {
            sb.append("${l.id},")
            sb.append("\"${dateTimeFormat.format(Date(l.timestamp))}\",")
            sb.append("\"${escapeCsv(l.partNumber)}\",")
            sb.append("\"${escapeCsv(l.partName)}\",")
            sb.append("\"${escapeCsv(l.actionType.labelAr)}\",")
            sb.append("${l.quantityChange},")
            sb.append("${l.previousQuantity},")
            sb.append("${l.newQuantity},")
            sb.append("\"${escapeCsv(l.fireTruckUnit)}\",")
            sb.append("\"${escapeCsv(l.technicianName)}\",")
            sb.append("\"${escapeCsv(l.notes)}\"\n")
        }

        saveAndShareFile(context, fileName, sb.toString(), "تصدير سجل العمليات الشامل للإكسل")
    }

    // ==========================================
    // INTERNAL HELPERS
    // ==========================================

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ")
    }

    private fun saveAndShareFile(context: Context, fileName: String, content: String, title: String) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileName)
            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel opens Arabic text correctly without encoding distortion
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                fos.write(content.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "مرفق ملف $title من نظام FireStock AI.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "تم تجهيز ملف Excel بنجاح: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء تصدير الملف: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun doPrintHtml(context: Context, jobName: String, htmlContent: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة في هذا الجهاز", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("id", "print", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager.print(jobName, printAdapter, printAttributes)
                }
            }

            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "فشل بدء الطباعة: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
