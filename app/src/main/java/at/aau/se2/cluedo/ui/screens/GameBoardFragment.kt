package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.aau.se2.cluedo.data.models.BasicCard
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.CardAdapter
import at.aau.se2.cluedo.viewmodels.GameBoard
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBoardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [gameBoard.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameBoardFragment : Fragment() {

    private lateinit var gameBoard: GameBoard
    var webSocketService:WebSocketService?=null
    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    private var _binding: FragmentGameBoardBinding? = null
    private val binding get() = _binding!!

    private val roomCoordinates = setOf(
        Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1), // Küche
        Pair(0, 4), Pair(1, 4), Pair(0, 5), Pair(1, 5), // Speisezimmer
        Pair(0, 9), Pair(1, 9), Pair(0, 10), Pair(1, 10), // Salon
        Pair(4, 0), Pair(5, 0), Pair(4, 1), Pair(5, 1), // Musikzimmer
        Pair(4, 9), Pair(5, 9), Pair(4, 10), Pair(5, 10), // Halle
        Pair(8, 0), Pair(9, 0), Pair(8, 1), Pair(9, 1), // Wintergarten
        Pair(8, 4), Pair(9, 4), Pair(8, 5), Pair(9, 5), // Billardzimmer
        Pair(8, 6), Pair(9, 6), Pair(8, 7), Pair(9, 7), // Bibliothek
        Pair(8, 9), Pair(9, 9), Pair(8, 10), Pair(9, 10) // Arbeitszimmer
    )


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cardsRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }
    private fun init() {
        println("HI")
        webSocketService = WebSocketService.getInstance()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGameBoardBinding.inflate(inflater, container, false)
       // binding =_binding!!

        // Inflate the layout for this fragment
        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webSocketService?.connect()
        //println("gameBoard Hi")
        gameBoard = view.findViewById(R.id.gameBoardView) as GameBoard
        // Check if we have a game state and log it

        //Solve Case Buttons
        binding.notesButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_notesFragment)
        }

        binding.solveCaseButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_solveCaseFragment)
        }

        binding.makeSuspicionButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_suspicionPopupFragment)
        }

        //BottomSheet to show cards
        val bottomSheet = view.findViewById<NestedScrollView>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


        binding.cardsOpenButton.setOnClickListener {
            toggleBottomSheet()
        }
        //Change Icon of FloatingActionButton (openCardsButton) depending on state of BottomSheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.cardsOpenButton.setImageResource(R.drawable.cards_close_icon)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.cardsOpenButton.setImageResource(R.drawable.cards_open_icon)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // not needed but must be overridden
            }

        })

        val recyclerView = binding.playerCardsRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        var cards = WebSocketService.getInstance().player.value?.cards
        recyclerView.adapter = CardAdapter(BasicCard.getCardIDs(cards))


        val gameState = lobbyViewModel.gameState.value
        if (gameState != null) {
            showToast("Game state available: ${gameState.players.size} players")

            // Log all players to help with debugging
            gameState.players.forEach { player ->
                lobbyViewModel.logMessage("Player in game: ${player.name} (${player.character})")
            }

        } else {
            showToast("No game state available yet")
            lobbyViewModel.logMessage("Game state is null in GameFragment")

            // Try to get the game state from the lobby state
            val lobbyState = lobbyViewModel.lobbyState.value
            if (lobbyState != null) {
                lobbyViewModel.logMessage("Lobby state available with ${lobbyState.players.size} players")

                // Create a temporary game state from the lobby state
                val tempGameState = GameStartedResponse(
                    lobbyId = lobbyState.id,
                    players = lobbyState.players
                )

                // Update the UI with the lobby players
                updatePlayersUI(tempGameState)
            } else {
                lobbyViewModel.logMessage("Both game state and lobby state are null")
            }

            // Try to check if a game has started
            lobbyViewModel.checkGameStarted()
        }


        gameBoard.init()
        updatePlayers()

        //println(gameBoard)
        val moveButton = view.findViewById<Button>(R.id.movebutton)
        val upButton = view.findViewById<Button>(R.id.moveUp)
        val downButton = view.findViewById<Button>(R.id.moveDown)
        val leftButton = view.findViewById<Button>(R.id.moveLeft)
        val rightButton = view.findViewById<Button>(R.id.moveRight)
        val doneButton = view.findViewById<Button>(R.id.Done)

        moveButton.setOnClickListener {

            moveButton.visibility= View.GONE
            upButton.visibility= View.VISIBLE
            downButton.visibility= View.VISIBLE
            leftButton.visibility= View.VISIBLE
            rightButton.visibility= View.VISIBLE
            doneButton.visibility = View.VISIBLE
            gameBoard.performMoveClicked()

        }
        upButton.setOnClickListener {
            gameBoard.moveUp()
        }
        downButton.setOnClickListener {
            gameBoard.moveDown()
        }
        leftButton.setOnClickListener {
            gameBoard.moveLeft()
        }
        rightButton.setOnClickListener {
            gameBoard.moveRight()
        }
        doneButton.setOnClickListener {
            gameBoard.done()
            moveButton.visibility= View.VISIBLE
            upButton.visibility= View.GONE
            downButton.visibility= View.GONE
            leftButton.visibility= View.GONE
            rightButton.visibility= View.GONE
            doneButton.visibility = View.GONE
        }

        binding.rollDice.setOnClickListener {
            webSocketService?.rollDice()
        }

        lifecycleScope.launch {
            launch {
                webSocketService?.diceOneResult?.collect { value ->
                    value?.let {
                        binding.diceOneValueTextView2.text = it.toString()
                       // view.findViewById<TextView>(R.id.diceTwoValueTextView).text = it.toString()
                    }
                }
            }
            launch {
                webSocketService?.diceTwoResult?.collect { value ->
                    value?.let {
                        binding.diceTwoValueTextView2.text = it.toString()
                        //view.findViewById<TextView>(R.id.diceTwoValueTextView).text = it.toString()
                    }
                }
            }
        }


    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage)
                    }
                }

                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        val currentPlayer = lobby?.players?.find { it.isCurrentPlayer == true }
                        val isInRoom = roomCoordinates.contains(Pair(currentPlayer?.x, currentPlayer?.y))
                        binding.makeSuspicionButton.isEnabled = isInRoom
                    }
                }


            }
        }
    }
    private fun updatePlayers(){

    }

    private fun updatePlayersUI(gameState: GameStartedResponse) {
        // Update players list
        val playersList = gameState.players.joinToString("\n") { player ->
            val currentPlayerMark = if (player.isCurrentPlayer) " (Current Turn)" else ""
            "  - ${player.name} (${player.character})$currentPlayerMark"
        }


        // Log for debugging
        lobbyViewModel.logMessage("Updated UI with ${gameState.players.size} players")
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}