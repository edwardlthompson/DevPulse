package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchPace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ReleaseRefreshRuntime {
    private val runningState = MutableStateFlow(false)
    private val pausedState = MutableStateFlow(false)
    private val progressState = MutableStateFlow(RefreshProgress(0, 0))

    val running: StateFlow<Boolean> = runningState.asStateFlow()
    val paused: StateFlow<Boolean> = pausedState.asStateFlow()
    val progress: StateFlow<RefreshProgress> = progressState.asStateFlow()

    fun tryBegin(): Boolean {
        if (runningState.value) return false
        RefreshSkip.reset()
        RefreshOutletBoard.reset()
        GitHubSearchPace.reset()
        progressState.value = RefreshProgress(0, 0)
        pausedState.value = false
        runningState.value = true
        return true
    }

    fun pause() {
        if (runningState.value) pausedState.value = true
    }

    fun resume() {
        pausedState.value = false
    }

    fun awaitRun() {
        while (pausedState.value) Thread.sleep(40)
    }

    fun stopOutlet(id: String) {
        RefreshSkip.stop(id)
        RefreshProgressClock.pulseActive()
    }

    fun setProgress(value: RefreshProgress) {
        progressState.value = value
    }

    fun finish() {
        pausedState.value = false
        runningState.value = false
    }

    fun reset() {
        pausedState.value = false
        runningState.value = false
        progressState.value = RefreshProgress(0, 0)
    }
}
