package org.openedx.courses.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.openedx.core.ui.theme.OpenEdXTheme

class AllEnrolledCoursesFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val initial = arguments?.getString(ARG_INITIAL_FILTER, "") ?: ""
                AllEnrolledCoursesView(
                    fragmentManager = requireActivity().supportFragmentManager,
                    initialFilter = initial
                )
            }
        }
    }

    companion object {
        const val LOAD_MORE_THRESHOLD = 4
        const val TABLET_GRID_COLUMNS = 3
        const val MOBILE_GRID_COLUMNS = 2
        private const val ARG_INITIAL_FILTER = "initial_filter"
        fun newInstance(initialFilter: String = ""): AllEnrolledCoursesFragment {
            val fragment = AllEnrolledCoursesFragment()
            fragment.arguments = android.os.Bundle().apply {
                putString(ARG_INITIAL_FILTER, initialFilter)
            }
            return fragment
        }
    }
}
