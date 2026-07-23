package com.kharcha.tracker.presentation.screens.managecategories

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.Category
import com.kharcha.core.model.TransactionType
import com.kharcha.core.domain.repository.CategoryRepository
import com.kharcha.core.designsystem.theme.CategoryColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val showDialog: Boolean = false,
    val editingCategory: Category? = null,
    
    // Dialog inputs
    val nameInput: String = "",
    val typeInput: TransactionType = TransactionType.EXPENSE,
    val iconInput: String = "more_horiz",
    val colorInput: Int = CategoryColors[0].toArgb(),
    
    val error: String? = null
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val analyticsManager: com.kharcha.core.data.analytics.AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageCategoriesUiState())
    val uiState: StateFlow<ManageCategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllCategories().collect { list ->
                _uiState.update { it.copy(categories = list, isLoading = false) }
            }
        }
    }

    fun onAddClick() {
        _uiState.update { 
            it.copy(
                showDialog = true,
                editingCategory = null,
                nameInput = "",
                typeInput = TransactionType.EXPENSE,
                iconInput = "more_horiz",
                colorInput = CategoryColors[0].toArgb(),
                error = null
            )
        }
    }

    fun onEditClick(category: Category) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingCategory = category,
                nameInput = category.name,
                typeInput = if (category.type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
                iconInput = category.iconName,
                colorInput = category.color,
                error = null
            )
        }
    }

    fun onDeleteClick(category: Category) {
        viewModelScope.launch {
            if (category.isDefault) return@launch
            repository.updateCategory(category.copy(isArchived = true))
        }
    }

    fun onRestoreClick(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isArchived = false))
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(nameInput = name, error = null) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(typeInput = type) }
    }

    fun onIconChange(iconName: String) {
        _uiState.update { it.copy(iconInput = iconName) }
    }

    fun onColorChange(color: Int) {
        _uiState.update { it.copy(colorInput = color) }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            val category = Category(
                id = state.editingCategory?.id ?: 0,
                name = state.nameInput.trim(),
                type = state.typeInput.name,
                iconName = state.iconInput,
                color = state.colorInput,
                isDefault = state.editingCategory?.isDefault ?: false
            )

            if (state.editingCategory != null) {
                repository.updateCategory(category)
            } else {
                repository.insertCategory(category)
                analyticsManager.logCategoryCreated(category.name, category.type)
            }
            _uiState.update { it.copy(showDialog = false) }
        }
    }
}
