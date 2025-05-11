package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CheatDetectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = resources.getIdentifier("fragment_cheat_detection", "layout", requireContext().packageName)
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewId = resources.getIdentifier("cheatDetectionText", "id", requireContext().packageName)
        val textView = view.findViewById<TextView>(textViewId)
        textView.text = "Cheat Detection In Progress\nComing in Sprint 3"
    }
}
