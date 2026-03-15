package com.example.timer.data.model

enum class PhaseType { WARMUP, WORK, REST, COOLDOWN }

data class Phase(
    val type: PhaseType,
    val durationSeconds: Int,
    val cycleNumber: Int = 0
)

fun Sequence.buildPhases(): List<Phase> {
    val phases = mutableListOf<Phase>()
    if (warmupDuration > 0)
        phases.add(Phase(PhaseType.WARMUP, warmupDuration))
    for (i in 1..cycles) {
        phases.add(Phase(PhaseType.WORK, workDuration, i))
        if (i < cycles)
            phases.add(Phase(PhaseType.REST, restDuration, i))
    }
    if (cooldownDuration > 0)
        phases.add(Phase(PhaseType.COOLDOWN, cooldownDuration))
    return phases
}   