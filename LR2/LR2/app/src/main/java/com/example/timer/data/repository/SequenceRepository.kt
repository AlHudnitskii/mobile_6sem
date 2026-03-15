package com.example.timer.data.repository

import androidx.lifecycle.LiveData
import com.example.timer.data.db.SequenceDao
import com.example.timer.data.model.Sequence

class SequenceRepository(private val dao: SequenceDao) {
    val allSequences: LiveData<List<Sequence>> = dao.getAll()
    suspend fun insert(s: Sequence) = dao.insert(s)
    suspend fun update(s: Sequence) = dao.update(s)
    suspend fun delete(s: Sequence) = dao.delete(s)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun getById(id: Long) = dao.getById(id)
}
