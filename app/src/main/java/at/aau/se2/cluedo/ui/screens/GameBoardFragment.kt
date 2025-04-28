package at.aau.se2.cluedo.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class GameBoardFragment(context: Context) : View(context) {
    private var playerBitmap: Bitmap? = null
    private var gameBoardBitmap:Bitmap? = null
    private var playerX: Float = 0f
    private var playerY: Float = 0f
    private var boardX:Float = 0f
    private var boardY:Float = 0f

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context) {
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs) {
        init()
    }

    private fun init() {
        println("HI")
        playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.chess_game)
        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.gameboard)
        // Set initial position (example: center of the view)
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,60,60,false) }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,700,700,false) }
        gameBoardBitmap?.let {
            boardX =  (it.width - width) / 2f
            boardY = (it.height - height) / 2f
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
        playerX = 400f
        playerY = 700f

        gameBoardBitmap?.let {
            canvas.drawBitmap(it,0f,0f,null)
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