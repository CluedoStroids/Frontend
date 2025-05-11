package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.GameBoard
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBoardBinding
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

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}