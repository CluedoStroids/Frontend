package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SolveCaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val layoutId = context.resources.getIdentifier("fragment_solve_case", "layout", context.packageName)
        val view = inflater.inflate(layoutId, container, false)

        val solveButtonId = context.resources.getIdentifier("button_solve_case", "id", context.packageName)
        val solveButton: Button = view.findViewById(solveButtonId)
        solveButton.setOnClickListener {
            solveCase()
        }

        return view
    }

    private fun solveCase() {
        // To be filled out
    }
}
