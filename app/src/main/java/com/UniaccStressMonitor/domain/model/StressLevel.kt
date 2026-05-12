package com.UniaccStressMonitor.domain.model

enum class StressLevel(val value: Int, val label: String) {
    LOW_STRESS(0, "Estrés Bajo"),
    MEDIUM_STRESS(1, "Estrés Medio"),
    HIGH_STRESS(2, "Estrés Alto");

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: LOW_STRESS
    }
}
