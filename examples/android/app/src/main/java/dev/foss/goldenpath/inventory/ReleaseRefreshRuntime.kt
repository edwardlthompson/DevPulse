package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ReleaseRefreshRuntime {
    private val runningState = MutableStateFlow(false)
    private val progressState = MutableStateFlow(RefreshProgress(0, 0))

    val running: StateFlow<Boolean> = runningState.asStateFlow()
    val progress: StateFlow<RefreshProgress> = progressState.asStateFlow()

    fun tryBegin(): Boolean {
        if (runningState.value) return false
        progressState.value = RefreshProgress(0, 0)
        runningState.value = true
        return true
    }

    fun setProgress(value: RefreshProgress) {
        progressState.value = value
    }

    fun finish() {
        runningState.value = false
    }

    fun reset() {
        runningState.value = false
        progressState.value = RefreshProgress(0, 0)
    }
}
