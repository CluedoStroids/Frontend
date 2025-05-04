package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import at.aau.se2.cluedo.ui.screens.GameBoard


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [gameBoard.newInstance] factory method to
 * create an instance of this fragment.
 */
class gameBoard : Fragment() {

    private lateinit var gameBoard: GameBoard
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var playerBitmap: Bitmap? = null
    private var playerX: Float = 0f
    private var playerY: Float = 0f
    var displayMetrics: DisplayMetrics = DisplayMetrics()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        init()
    }
    private fun init() {
        println("HI")

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_board, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

            moveButton.visibility=View.GONE
            upButton.visibility=View.VISIBLE
            downButton.visibility=View.VISIBLE
            leftButton.visibility=View.VISIBLE
            rightButton.visibility=View.VISIBLE
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
            moveButton.visibility=View.VISIBLE
            upButton.visibility=View.GONE
            downButton.visibility=View.GONE
            leftButton.visibility=View.GONE
            rightButton.visibility=View.GONE
            doneButton.visibility = View.GONE
        }


    }
}