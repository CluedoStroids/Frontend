package at.aau.se2.cluedo.ui.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.aau.se2.cluedo.viewmodels.DynamicGridHelper
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityGameBoardBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton


class GameBoardActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var gameBoardBinding: ActivityGameBoardBinding
    private lateinit var gameBoardNavController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var gridHelper: DynamicGridHelper

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        gameBoardBinding = ActivityGameBoardBinding.inflate(layoutInflater)
        setContentView(gameBoardBinding.root)
        enableEdgeToEdge()

        bottomSheetBehavior = BottomSheetBehavior.from(gameBoardBinding.bottomSheet)

        // Initially hide the bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Set up expand button
        gameBoardBinding.cardsOpenButton.setOnClickListener {
            toggleBottomSheet()
        }

        //setupRecyclerView()

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Change FAB icon to collapse
                        gameBoardBinding.cardsOpenButton.setImageResource(R.drawable.cards_close_icon)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        // Change FAB icon to expand
                        gameBoardBinding.cardsOpenButton.setImageResource(R.drawable.cards_open_icon)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: Implement any animation during sliding
            }

        })
    }

    /*
    private fun setupRecyclerView() {

        // Determine optimal number of columns based on number of cards
        val columnCount = gridHelper.getColumnCountForPlayerCards(playerCards.size)

        gameBoardBinding.playerCardsRecyclerview.layoutManager = GridLayoutManager(this, columnCount)

        // Create and set adapter
        adapter = PlayerCardAdapter(playerCards) { playerCard ->
            // Handle card click
            Toast.makeText(this, "Selected: ${playerCard.name}", Toast.LENGTH_SHORT).show()

            // You can add additional functionality here when a card is clicked
            // For example, show details, highlight the selected card, etc.
        }

        gameBoardBinding.playerCardsRecyclerview.adapter = adapter
    }

     */

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}