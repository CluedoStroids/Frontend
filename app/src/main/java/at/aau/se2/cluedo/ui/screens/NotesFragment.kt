package at.aau.se2.cluedo.ui.screens

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentNotesBinding
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import androidx.fragment.app.activityViewModels



class NotesFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    private lateinit var binding: FragmentNotesBinding

    private val players = listOf("Green", "Yellow", "Red", "Blue", "White", "Purple")
    private val playerColors =
        listOf("#4CAF50", "#FFEB3B", "#F44336", "#2196F3", "#FFFFFF", "#9C27B0")

    private val suspects = listOf(
        "Mrs. White",
        "Miss Scarlett",
        "Mrs. Peacock",
        "Mr. Green",
        "Professor Plum",
        "Colonel Mustard"
    )
    private val weapons = listOf("Candlestick", "Revolver", "Rope", "Lead Pipe", "Wrench", "Dagger")
    private val rooms = listOf(
        "Library",
        "Kitchen",
        "Ballroom",
        "Study",
        "Hall",
        "Billiard Room",
        "Dining Room",
        "Lounge",
        "Conservatory"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val masterTable = binding.tableSuspects
        masterTable.removeAllViews()

        val titleRow = TableRow(context)
        val titleCell = TextView(context).apply {
            text = "Player"
            textSize = 20f
            setPadding(12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        titleRow.addView(titleCell)

        players.forEachIndexed { index, _ ->
            val colorBlock = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f
                    setColor(Color.parseColor(playerColors[index]))
                    setStroke(2, Color.DKGRAY)
                }
                layoutParams = TableRow.LayoutParams(100, 40).apply {
                    marginEnd = 8
                }
            }
            titleRow.addView(colorBlock)
        }
        masterTable.addView(titleRow)

        addSection("Suspects", suspects, masterTable, true)
        addSection("Weapons", weapons, masterTable, true)
        addSection("Rooms", rooms, masterTable, true)

    }

    private fun addSection(
        label: String,
        items: List<String>,
        table: TableLayout,
        boldDivider: Boolean
    ) {
        val sectionRow = TableRow(context)
        val sectionLabel = TextView(context).apply {
            text = label
            textSize = 18f
            setPadding(12)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
        }
        sectionRow.addView(sectionLabel)
        repeat(players.size) {
            val emptyCell = TextView(context).apply {
                setBackgroundColor(Color.parseColor("#6200EE"))
            }
            sectionRow.addView(emptyCell)
        }
        table.addView(sectionRow)

        items.forEach { item ->
            val row = TableRow(context)
            val itemCell = TextView(context).apply {
                text = item
                textSize = 16f
                setPadding(12)
            }
            row.addView(itemCell)
            players.forEachIndexed { index, _ ->
                val checkBox = CheckBox(context).apply {
                    val color = if (players[index] == "White") Color.BLACK else Color.parseColor(
                        playerColors[index]
                    )
                    val outline = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f
                        setStroke(2, Color.BLACK)
                    }
                    background = outline
                    buttonTintList = android.content.res.ColorStateList.valueOf(color)

                    // Load previously saved state
                    isChecked = lobbyViewModel.isNoteChecked(item, players[index])

                    // Save new state on change
                    setOnCheckedChangeListener { _, isChecked ->
                        lobbyViewModel.setNote(item, players[index], isChecked)
                    }
                }

                row.addView(checkBox)
            }
            table.addView(row)
        }
    }

}