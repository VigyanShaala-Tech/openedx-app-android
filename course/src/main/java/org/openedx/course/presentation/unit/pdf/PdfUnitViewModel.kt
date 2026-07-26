package org.openedx.course.presentation.unit.pdf

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.openedx.core.presentation.global.ErrorType
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.foundation.presentation.BaseViewModel
import java.io.File

class PdfUnitViewModel(
    private val networkConnection: NetworkConnection,
    private val notifier: CourseNotifier,
    private val okHttpClient: OkHttpClient
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PdfUnitUIState>(PdfUnitUIState.Loading)
    val uiState = _uiState.asStateFlow()

    fun downloadPdf(url: String, cacheDir: File) {
        if (_uiState.value is PdfUnitUIState.Loaded) return

        _uiState.value = PdfUnitUIState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val file = File(cacheDir, "temp_pdf.pdf")
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    _uiState.value = PdfUnitUIState.Loaded(file)
                } else {
                    _uiState.value = PdfUnitUIState.Error(ErrorType.UNKNOWN_ERROR)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = PdfUnitUIState.Error(
                    if (networkConnection.isOnline()) {
                        ErrorType.UNKNOWN_ERROR
                    } else {
                        ErrorType.CONNECTION_ERROR
                    }
                )
            }
        }
    }

    fun notifyCompletionSet() {
        viewModelScope.launch {
            notifier.send(CourseCompletionSet())
        }
    }
}

sealed class PdfUnitUIState {
    object Loading : PdfUnitUIState()
    data class Loaded(val file: File) : PdfUnitUIState()
    data class Error(val errorType: ErrorType) : PdfUnitUIState()
}
