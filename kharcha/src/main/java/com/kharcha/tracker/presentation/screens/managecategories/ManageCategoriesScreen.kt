package com.kharcha.tracker.presentation.screens.managecategories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.core.model.Category
import com.kharcha.core.model.TransactionType
import com.kharcha.core.designsystem.theme.CategoryColors
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.designsystem.util.CategoryIcons
import com.kharcha.core.designsystem.util.ui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBack: () -> Unit,
    viewModel: ManageCategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(TransactionType.EXPENSE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddClick,
                containerColor = TealPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = if (selectedTab == TransactionType.EXPENSE) 0 else 1,
                contentColor = TealPrimary
            ) {
                Tab(
                    selected = selectedTab == TransactionType.EXPENSE,
                    onClick = { selectedTab = TransactionType.EXPENSE },
                    text = { Text("Expense") }
                )
                Tab(
                    selected = selectedTab == TransactionType.INCOME,
                    onClick = { selectedTab = TransactionType.INCOME },
                    text = { Text("Income") }
                )
            }

            // List
            val filteredCategories = state.categories.filter { 
                if (selectedTab == TransactionType.EXPENSE) it.type == "EXPENSE" else it.type == "INCOME"
            }
            val activeCategories = filteredCategories.filter { !it.isArchived }
            val archivedCategories = filteredCategories.filter { it.isArchived }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeCategories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = { viewModel.onEditClick(category) },
                        onDelete = { viewModel.onDeleteClick(category) }
                    )
                }

                if (archivedCategories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Archived Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(archivedCategories, key = { it.id }) { category ->
                        ArchivedCategoryItem(
                            category = category,
                            onRestore = { viewModel.onRestoreClick(category) }
                        )
                    }
                }
            }
        }

        if (state.showDialog) {
            AddEditCategoryDialog(
                state = state,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val ui = category.ui()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onEdit() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ui.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                ui.icon,
                contentDescription = null,
                tint = ui.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        if (!category.isDefault) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Archive",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ArchivedCategoryItem(
    category: Category,
    onRestore: () -> Unit
) {
    val ui = category.ui()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ui.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                ui.icon,
                contentDescription = null,
                tint = ui.color.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        
        TextButton(onClick = onRestore) {
            Text("Restore", color = TealPrimary)
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    state: ManageCategoriesUiState,
    viewModel: ManageCategoriesViewModel
) {
    Dialog(onDismissRequest = viewModel::onDialogDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.editingCategory == null) "Add Category" else "Edit Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Name
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Category Name") },
                    singleLine = true,
                    isError = state.error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Type (Only if adding?)
                // Actually, let's allow changing type for simplicity but warn user?
                // Or just allow it.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TransactionType.entries.forEach { type ->
                        val isSelected = state.typeInput == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(type.name) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, null) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(40.dp),
                    modifier = Modifier
                        .height(120.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CategoryIcons.entries.toList()) { (name, icon) ->
                        val isSelected = state.iconInput == name
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TealPrimary else Color.Transparent)
                                .border(1.dp, if (isSelected) TealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.onIconChange(name) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                val isPredefined = CategoryColors.any { it.toArgb() == state.colorInput }
                var showCustomColorPicker by remember { mutableStateOf(!isPredefined) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            showCustomColorPicker = !showCustomColorPicker
                            if (showCustomColorPicker) {
                                viewModel.onColorChange(Color.hsv(0f, 0.85f, 0.9f).toArgb())
                            } else {
                                viewModel.onColorChange(CategoryColors[0].toArgb())
                            }
                        }
                    ) {
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (showCustomColorPicker) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Checkbox(
                            checked = showCustomColorPicker,
                            onCheckedChange = { checked ->
                                showCustomColorPicker = checked
                                if (checked) {
                                    viewModel.onColorChange(Color.hsv(0f, 0.85f, 0.9f).toArgb())
                                } else {
                                    viewModel.onColorChange(CategoryColors[0].toArgb())
                                }
                            },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = TealPrimary)
                        )
                    }
                }

                if (!showCustomColorPicker) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(40.dp),
                        modifier = Modifier
                            .height(80.dp)
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CategoryColors) { color ->
                            val colorInt = color.toArgb()
                            val isSelected = state.colorInput == colorInt
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.onColorChange(colorInt) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val hsv = remember(state.colorInput) {
                        FloatArray(3).apply {
                            android.graphics.Color.colorToHSV(state.colorInput, this)
                        }
                    }
                    val currentHue = hsv[0]

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(state.colorInput))
                                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        androidx.compose.material3.Slider(
                            value = currentHue,
                            onValueChange = { newHue ->
                                val newColor = Color.hsv(newHue, 0.85f, 0.9f).toArgb()
                                viewModel.onColorChange(newColor)
                            },
                            valueRange = 0f..360f,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = TealPrimary,
                                activeTrackColor = TealPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = viewModel::onDialogDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = viewModel::onSave) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
