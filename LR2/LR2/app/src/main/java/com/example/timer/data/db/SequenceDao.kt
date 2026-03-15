package com.example.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.timer.data.model.Sequence

@Dao
interface SequenceDao {
    @Query("SELECT * FROM sequences ORDER BY id DESC")
    fun getAll(): LiveData<List<Sequence>>

    @Query("SELECT * FROM sequences WHERE id = :id")
    suspend fun getById(id: Long): Sequence?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sequence: Sequence): Long

    @Update
    suspend fun update(sequence: Sequence)

    @Delete
    suspend fun delete(sequence: Sequence)

    @Query("DELETE FROM sequences")
    suspend fun deleteAll()
}
