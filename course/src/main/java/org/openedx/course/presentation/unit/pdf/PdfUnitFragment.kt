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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ahmer.pdfviewer.PDFView
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.ui.FullScreenErrorView
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import java.io.File

class PdfUnitFragment : Fragment() {

    private val viewModel: PdfUnitViewModel by viewModel()
    private var blockId: String = ""
    private var pdfUrl: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockId = requireArguments().getString(ARG_BLOCK_ID, "")
        pdfUrl = requireArguments().getString(ARG_PDF_URL, "")
        title = requireArguments().getString(ARG_TITLE, "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                
                LaunchedEffect(pdfUrl) {
                    viewModel.downloadPdf(pdfUrl, context.cacheDir)
                }

                PdfUnitScreen(
                    uiState = uiState,
                    onReloadClick = {
                        viewModel.downloadPdf(pdfUrl, context.cacheDir)
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_BLOCK_ID = "blockId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_TITLE = "title"

        fun newInstance(
            blockId: String,
            pdfUrl: String,
            title: String
        ): PdfUnitFragment {
            val fragment = PdfUnitFragment()
            fragment.arguments = bundleOf(
                ARG_BLOCK_ID to blockId,
                ARG_PDF_URL to pdfUrl,
                ARG_TITLE to title
            )
            return fragment
        }
    }
}

@Composable
fun PdfUnitScreen(
    uiState: PdfUnitUIState,
    onReloadClick: () -> Unit
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
            when (uiState) {
                is PdfUnitUIState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                }
                is PdfUnitUIState.Loaded -> {
                    PdfViewer(uiState.file)
                }
                is PdfUnitUIState.Error -> {
                    FullScreenErrorView(
                        modifier = Modifier.fillMaxSize(),
                        errorType = uiState.errorType,
                        onReloadClick = onReloadClick
                    )
                }
            }
        }
    }
}

@Composable
fun PdfViewer(file: File) {
    AndroidView(
        factory = { context ->
            PDFView(context, null)
        },
        modifier = Modifier.fillMaxSize(),
        update = { pdfView ->
            pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubleTap(true)
                .defaultPage(0)
                .load()
        }
    )
}
