package org.openedx.course.presentation.unit.pdf

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ahmer.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.isPreview
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfUnitFragment : Fragment() {

    private val viewModel: PdfUnitViewModel by viewModel {
        parametersOf(
            requireArguments().getString(ARG_COURSE_ID, ""),
            requireArguments().getString(ARG_BLOCK_ID, "")
        )
    }
    private var blockId: String = ""
    private var courseId: String = ""
    private var pdfUrl: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockId = requireArguments().getString(ARG_BLOCK_ID, "")
        courseId = requireArguments().getString(ARG_COURSE_ID, "")
        pdfUrl = requireArguments().getString(ARG_PDF_URL, "")
        title = requireArguments().getString(ARG_TITLE, "")
//        viewModel.markBlockCompleted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                PdfUnitScreen(
                    pdfUrl = pdfUrl,
                    onDownloadComplete = {
                        viewModel.markBlockCompleted()
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_BLOCK_ID = "blockId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_TITLE = "title"

        fun newInstance(
            blockId: String,
            courseId: String,
            pdfUrl: String,
            title: String
        ): PdfUnitFragment {
            val fragment = PdfUnitFragment()
            fragment.arguments = bundleOf(
                ARG_BLOCK_ID to blockId,
                ARG_COURSE_ID to courseId,
                ARG_PDF_URL to pdfUrl,
                ARG_TITLE to title
            )
            return fragment
        }
    }
}

@Composable
fun PdfUnitScreen(
    pdfUrl: String,
    onDownloadComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background),
        contentAlignment = Alignment.Center
    ) {
        PdfViewer(
            url = pdfUrl,
            onDownloadComplete = onDownloadComplete
        )
    }
}

@Composable
fun PdfViewer(
    url: String,
    onDownloadComplete: () -> Unit
) {
    if (isPreview) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PDF Viewer Preview",
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textPrimary
            )
        }
        return
    }

    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        isLoading = true
        error = null
        if (url.isEmpty()) {
            error = "Invalid PDF URL"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            withContext(Dispatchers.IO) {
                val connection = (URL(url).openConnection() as java.net.HttpURLConnection).apply {
                    val cookies = android.webkit.CookieManager.getInstance().getCookie(url)
                    if (!cookies.isNullOrEmpty()) {
                        setRequestProperty("Cookie", cookies)
                    }
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android)")
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val tempFile = File.createTempFile("textbook", ".pdf", context.cacheDir)
                    FileOutputStream(tempFile).use { output ->
                        inputStream.use { input -> input.copyTo(output) }
                    }
                    pdfFile = tempFile
                } else {
                    throw Exception("HTTP $responseCode ${connection.responseMessage}")
                }
            }
            onDownloadComplete()
        } catch (e: Exception) {
            error = e.message ?: "Error downloading PDF"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.appColors.primary
            )
        } else if (error != null) {
            Text(
                text = "Error loading PDF: $error",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.error
            )
        } else {
            pdfFile?.let { file ->
                AndroidView(
                    factory = { ctx ->
                        PDFView(ctx, null).apply {
                            fromFile(file)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubleTap(true)
                                .defaultPage(0)
                                .spacing(0)
                                .fitEachPage(true)
                                .autoSpacing(false)
                                .load()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PdfUnitScreenPreview() {
    OpenEdXTheme {
        PdfUnitScreen(
            pdfUrl = "",
            onDownloadComplete = {}
        )
    }
}
