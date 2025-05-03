package at.aau.se2.cluedo.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.example.myapplication.R

class GameBoardFragment(context: Context) : View(context) {

    //Bitmaps
    private var playerBitmap: Bitmap? = null
    private var gameBoardBitmap:Bitmap? = null
    private var grid:Bitmap? = null

    //Player and Board variables
    private var playerX: Float = 0f
    private var playerY: Float = 0f
    private var boardPosX:Float = 0f
    private var boardPosY:Float = 0f
    private var sizeX:Int=1000
    private var sizeY:Int=1000
    private var playerPosX=24
    private var playerPosY=24
    private var playerSizeX=sizeX/25
    private var playerSizeY=sizeY/25

    //Button


    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context) {
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs) {
        init()
    }

    private fun init() {
        println("HI")
        playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.block)
        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.gameboard)
        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.grid)
        // Set initial position (example: center of the view)
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,sizeX,sizeY,false) }
        grid = grid?.let { Bitmap.createScaledBitmap(it,sizeX,sizeY,false) }
        gameBoardBitmap?.let {
            boardPosX =  0f//(it.width - width) / 2f
            boardPosY = 0f //(it.height - height) / 2f
        }
        grid?.let {
            boardPosX =  0f//(it.width - width) / 2f
            boardPosY = 0f //(it.height - height) / 2f
        }
        playerBitmap?.let {
            playerX = (width - it.width) / 2f
            playerY = (height - it.height) / 2f
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update position if view size changes
        playerBitmap?.let {
            playerX = (width - it.width) / 2f
            playerY = (height - it.height) / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gameBoardBitmap?.let {
           canvas.drawBitmap(it,boardPosX,boardPosY,null)
            playerX =((playerPosX*(sizeX/25))+boardPosX)+2
            playerY = ((playerPosY*(sizeY/25))+boardPosY)
        }
        grid?.let {
            canvas.drawBitmap(it,boardPosX,boardPosY,null)
        }
        playerBitmap?.let {

            canvas.drawBitmap(it, playerX, playerY, null) // Draw the bitmap

        }

    }

    // Method to update player position (example)
    fun setPlayerPosition(x: Float, y: Float) {
        playerX = x
        playerY = y
        invalidate() // Request a redraw of the view
    }
}