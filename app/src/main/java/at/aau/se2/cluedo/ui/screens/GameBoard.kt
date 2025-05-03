package at.aau.se2.cluedo.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import com.example.myapplication.R

class GameBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //Bitmaps
    private var playerBitmap: Bitmap? = null
    private var gameBoardBitmap:Bitmap? = null

    //Player and Board variables
    private var playerX: Float = 0f
    private var playerY: Float = 0f
    private var boardPosX:Float = 0f
    private var boardPosY:Float = 0f
    private var sizeX:Int=1080
    private var sizeY:Int=1080
    private var gridScale:Float= 0.9f
    private var playerPosX=24
    private var playerPosY=10
    var gridSize= sizeX*gridScale
    private var playerSizeX=(gridSize/25).toInt()
    private var playerSizeY=(gridSize/25).toInt()

    //Button

    fun init() {

        playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.block)
        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.grid)

        // Set initial position (example: center of the view)
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,(sizeX*gridScale).toInt(),(sizeY*gridScale).toInt(),false) }


    }

    fun moveUp(){
        playerPosY--
        if(playerPosY<=0){
            playerPosY=0
        }
        invalidate()
    }
    fun moveDown(){
        playerPosY++
        if(playerPosY>=24){
            playerPosY=24
        }
        invalidate()
    }
    fun moveLeft(){
        playerPosX--
        if(playerPosX<=0){
            playerPosX=0
        }
        invalidate()
    }
    fun moveRight(){
        playerPosX++
        if(playerPosX>=24){
            playerPosX=24
        }
        invalidate()
    }
    fun performMoveClicked(){
        gridScale=2f
        calcSize()
        invalidate()
        println("Scaling")
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update position if view size changes

        playerBitmap?.let {
            playerX = (width - it.width) / 2f
            playerY = (height - it.height) / 2f
        }
    }
    fun calcSize(){
        gridSize= sizeX*gridScale
        playerSizeX=(gridSize/25).toInt()
        playerSizeY=(gridSize/25).toInt()
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,(sizeX*gridScale).toInt(),(sizeY*gridScale).toInt(),false) }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calcSize()
        gameBoardBitmap?.let {
            boardPosX =  (canvas.width - it.width) / 2f
            boardPosY = (canvas.height - it.height) / 2f
           canvas.drawBitmap(it,boardPosX,boardPosY,null)
            playerX =((playerPosX*(gridSize/25))+boardPosX)
            playerY = ((playerPosY*(gridSize/25))+boardPosY)
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