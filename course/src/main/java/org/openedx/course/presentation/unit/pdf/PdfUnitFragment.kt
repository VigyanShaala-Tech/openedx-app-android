package org.openedx.course.presentation.unit.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ahmer.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.appColors.background),
            contentAlignment = Alignment.Center
        ) {
            PdfViewer(
                url = pdfUrl,
                onDownloadComplete = onDownloadComplete
            )
        }
    }
}

@Composable
fun PdfViewer(
    url: String,
    onDownloadComplete: () -> Unit
) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        isLoading = true
        error = null
        try {
            withContext(Dispatchers.IO) {
                val connection = URL(url).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val tempFile = File.createTempFile("textbook", ".pdf", context.cacheDir)
                FileOutputStream(tempFile).use { output ->
                    inputStream.use { input -> input.copyTo(output) }
                }
                pdfFile = tempFile
            }
            onDownloadComplete()
        } catch (e: Exception) {
            error = e.message
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
                                .load()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
