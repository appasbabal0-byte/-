package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.FireStockAI
import com.example.data.database.FireStockDatabase
import com.example.data.model.InventoryLog
import com.example.data.model.SparePart
import com.example.data.repository.FireStockRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val titleAr: String) {
    DASHBOARD("الرئيسية"),
    INVENTORY("الجرد والمخزون"),
    AUDIT_TRAIL("سجل العمليات"),
    AI_ANALYTICS("ذكاء التنبؤ والصيانة")
}

class FireStockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FireStockRepository
    val allParts: StateFlow<List<SparePart>>
    val allLogs: StateFlow<List<InventoryLog>>
    val recentLogs: StateFlow<List<InventoryLog>>

    init {
        val database = FireStockDatabase.getDatabase(application, viewModelScope)
        repository = FireStockRepository(database.sparePartDao(), database.inventoryLogDao())

        allParts = repository.allParts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentLogs = repository.recentLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Navigation & UI State
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedPart = MutableStateFlow<SparePart?>(null)
    val selectedPart: StateFlow<SparePart?> = _selectedPart.asStateFlow()

    // Dialogs state
    val isCheckInDialogOpen = MutableStateFlow(false)
    val isCheckOutDialogOpen = MutableStateFlow(false)
    val isAddPartDialogOpen = MutableStateFlow(false)
    val isScannerDialogOpen = MutableStateFlow(false)
    val isVisualAiDialogOpen = MutableStateFlow(false)
    val isPartDetailDialogOpen = MutableStateFlow(false)

    // Temporary target part for quick action dialogs
    val activeDialogPart = MutableStateFlow<SparePart?>(null)

    // User feedback messages (Snackbar / Alert)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Filtered parts based on search query, category, and smart matching
    val filteredParts: StateFlow<List<SparePart>> = combine(
        allParts,
        _searchQuery,
        _selectedCategory
    ) { parts, query, category ->
        parts.filter { part ->
            val matchesCategory = when (category) {
                "الكل" -> true
                "كمية حرجة" -> part.stockStatus == SparePart.StockStatus.CRITICAL
                "كمية متوسطة" -> part.stockStatus == SparePart.StockStatus.WARNING
                else -> part.category == category
            }
            val matchesSearch = FireStockAI.matchesSmartQuery(part, query)
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Critical parts list
    val criticalParts: StateFlow<List<SparePart>> = allParts.combine(allParts) { parts, _ ->
        parts.filter { it.stockStatus == SparePart.StockStatus.CRITICAL }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Smart AI Predictions list
    val aiPredictions: StateFlow<List<FireStockAI.DepletionPrediction>> = combine(
        allParts,
        allLogs
    ) { parts, logs ->
        parts.map { FireStockAI.predictDepletion(it, logs) }
            .sortedBy { it.urgencyLevel.ordinal }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectPart(part: SparePart?) {
        _selectedPart.value = part
        if (part != null) {
            isPartDetailDialogOpen.value = true
        }
    }

    fun openCheckIn(part: SparePart? = null) {
        activeDialogPart.value = part
        isCheckInDialogOpen.value = true
    }

    fun openCheckOut(part: SparePart? = null) {
        activeDialogPart.value = part
        isCheckOutDialogOpen.value = true
    }

    fun openAddPart() {
        isAddPartDialogOpen.value = true
    }

    fun openScanner() {
        isScannerDialogOpen.value = true
    }

    fun openVisualAi() {
        isVisualAiDialogOpen.value = true
    }

    fun dismissDetailDialog() {
        isPartDetailDialogOpen.value = false
        _selectedPart.value = null
    }

    // Business Logic Actions

    fun performCheckIn(
        partId: Long,
        quantity: Int,
        supplier: String,
        technician: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.checkIn(partId, quantity, supplier, technician, notes)
            result.onSuccess { updated ->
                _toastMessage.emit("✅ تم إدخال $quantity ${updated.unit} بنجاح إلى رصيد [${updated.name}]")
                isCheckInDialogOpen.value = false
                activeDialogPart.value = null
            }.onFailure { error ->
                _toastMessage.emit("❌ خطأ: ${error.message}")
            }
        }
    }

    fun performCheckOut(
        partId: Long,
        quantity: Int,
        technician: String,
        fireTruckUnit: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.checkOut(partId, quantity, technician, fireTruckUnit, notes)
            result.onSuccess { updated ->
                _toastMessage.emit("🚨 تم صرف $quantity ${updated.unit} لصالح [$fireTruckUnit] بنجاح")
                isCheckOutDialogOpen.value = false
                activeDialogPart.value = null
            }.onFailure { error ->
                _toastMessage.emit("❌ خطأ: ${error.message}")
            }
        }
    }

    fun updateBinLocation(
        partId: Long,
        newBinLocation: String,
        warehouse: String,
        aisle: String,
        shelf: String,
        bin: String,
        technician: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.updateLocation(
                partId, newBinLocation, warehouse, aisle, shelf, bin, technician, notes
            )
            result.onSuccess { updated ->
                _toastMessage.emit("📍 تم تحديث موقع التخزين إلى [$newBinLocation]")
                if (_selectedPart.value?.id == partId) {
                    _selectedPart.value = updated
                }
            }.onFailure { error ->
                _toastMessage.emit("❌ خطأ: ${error.message}")
            }
        }
    }

    fun createPart(
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
    ) {
        viewModelScope.launch {
            val part = SparePart(
                partNumber = partNumber.trim(),
                name = name.trim(),
                category = category.trim(),
                barcode = barcode.trim(),
                binLocation = binLocation.trim(),
                warehouse = warehouse.trim(),
                aisle = aisle.trim(),
                shelf = shelf.trim(),
                bin = bin.trim(),
                quantity = quantity,
                minStock = minStock,
                unit = unit.trim(),
                supplier = supplier.trim(),
                compatibility = compatibility.trim(),
                notes = notes.trim()
            )
            val id = repository.addNewPart(part, technician)
            _toastMessage.emit("🎉 تم إضافة المادة الجديدة بنجاح برقم تعريف (#$id)")
            isAddPartDialogOpen.value = false
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            val matched = repository.getPartByBarcode(barcode.trim())
            if (matched != null) {
                _toastMessage.emit("🎯 تم العثور على المادة: ${matched.name}")
                _selectedPart.value = matched
                isPartDetailDialogOpen.value = true
                isScannerDialogOpen.value = false
            } else {
                _toastMessage.emit("⚠️ لم يتم العثور على أي قطعة تطابق الباركود: $barcode")
            }
        }
    }

    fun generateNewBarcode(): String {
        val randomSuffix = (1000..9999).random()
        return "FS${System.currentTimeMillis().toString().takeLast(6)}$randomSuffix"
    }
}
