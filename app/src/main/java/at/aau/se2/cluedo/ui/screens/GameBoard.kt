package at.aau.se2.cluedo.ui.screens

import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.GameBoard
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBoardBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [gameBoard.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameBoard : Fragment() {

    private lateinit var gameBoard: GameBoard
    private var playerBitmap: Bitmap? = null
    private var playerX: Float = 0f
    private var playerY: Float = 0f
    var displayMetrics: DisplayMetrics = DisplayMetrics()
    var webSocketService:WebSocketService?=null

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
        println("gameBoard Hi")
        gameBoard = view.findViewById(R.id.gameBoardView) as GameBoard
        gameBoard.init()
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

}