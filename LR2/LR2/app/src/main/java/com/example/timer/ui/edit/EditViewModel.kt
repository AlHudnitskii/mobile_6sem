package com.example.timer.ui.edit

import androidx.lifecycle.*
import com.example.timer.data.model.Sequence
import com.example.timer.data.repository.SequenceRepository
import kotlinx.coroutines.launch

class EditViewModel(private val repo: SequenceRepository) : ViewModel() {
    private val _sequence = MutableLiveData<Sequence?>()
    val sequence: LiveData<Sequence?> = _sequence

    fun load(id: Long) = viewModelScope.launch {
        _sequence.postValue(repo.getById(id))
    }

    fun save(seq: Sequence) = viewModelScope.launch {
        if (seq.id == 0L) repo.insert(seq) else repo.update(seq)
    }
}

class EditViewModelFactory(private val repo: SequenceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EditViewModel(repo) as T
}
