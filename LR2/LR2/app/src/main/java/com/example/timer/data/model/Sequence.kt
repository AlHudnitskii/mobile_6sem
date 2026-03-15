package com.example.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequences")
data class Sequence(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val warmupDuration: Int = 10,
    val workDuration: Int = 20,
    val restDuration: Int = 10,
    val cooldownDuration: Int = 10,
    val cycles: Int = 8,
    val restBetweenCycles: Int = 60
)
