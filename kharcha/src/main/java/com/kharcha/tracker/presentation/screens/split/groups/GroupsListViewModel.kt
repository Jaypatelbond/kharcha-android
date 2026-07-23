package com.kharcha.tracker.presentation.screens.split.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.GroupWithMembers
import com.kharcha.core.domain.repository.SplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsListViewModel @Inject constructor(
    private val repository: SplitRepository
) : ViewModel() {

    val groups: StateFlow<List<GroupWithMembers>> = repository.getGroupList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createGroup(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.createGroup(name)
            }
        }
    }
}
