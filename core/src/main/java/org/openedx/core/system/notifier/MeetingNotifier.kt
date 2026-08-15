package org.openedx.core.system.notifier

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MeetingNotifier {
    private val _isMeetingActive = MutableSharedFlow<Boolean>(replay = 1)
    val isMeetingActive: Flow<Boolean> = _isMeetingActive.asSharedFlow()

    private val _isInPipMode = MutableSharedFlow<Boolean>(replay = 1)
    val isInPipMode: Flow<Boolean> = _isInPipMode.asSharedFlow()

    suspend fun send(isActive: Boolean) {
        _isMeetingActive.emit(isActive)
    }

    suspend fun setPipMode(isInPip: Boolean) {
        _isInPipMode.emit(isInPip)
    }
}
