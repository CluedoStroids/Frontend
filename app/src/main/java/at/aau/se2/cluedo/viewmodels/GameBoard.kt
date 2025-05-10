package at.aau.se2.cluedo.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.lifecycleScope
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.network.WebSocketService
import com.example.myapplication.R
import kotlinx.coroutines.launch

class GameBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //Bitmaps
    private var playerBitmap: Bitmap? = null
    private var gameBoardBitmap:Bitmap? = null
    private val playersBitmap= arrayListOf<Bitmap>()

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
    private var gridPosX=playerPosX
    private var gridPosY=playerPosY
    var gridSize= sizeX*gridScale
    var move:Boolean=false
    private var playerSizeX=(gridSize/25).toInt()
    private var playerSizeY=(gridSize/25).toInt()

    private var players:List<Player>?= ArrayList()
    private var moves= arrayListOf<String>()
    //Button

    fun init() {
        WebSocketService.getInstance().players()

        players=WebSocketService.getInstance().getPlayers()
        if(players!=null) {
            for (i in 0..players?.size!!) {
                if (players!!.get(i).name != WebSocketService.getInstance().getMe()?.name) {
                    playersBitmap.add(
                        BitmapFactory.decodeResource(
                            resources,
                            players?.get(i)?.color?.img!!
                        )
                    )
                }
            }
        }
        playerBitmap = BitmapFactory.decodeResource(resources, WebSocketService.getInstance().getMe()?.color?.img!!)
        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.grid)

        // Set initial position (example: center of the view)
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,(sizeX*gridScale).toInt(),(sizeY*gridScale).toInt(),false) }

    }

    fun moveUp(){
        if(move)
        {
            gridPosY--
            if(gridPosY<=0){
                gridPosY=0
            }

        }else{
            playerPosY--
            if(playerPosY<=0){
                playerPosY=0
            }
        }
        moves.add("W")
        invalidate()
    }
    fun moveDown(){
        if(move)
        {
            gridPosY++
            if(gridPosY<=0){
                gridPosY=0
            }

        }else{
            playerPosY++
            if(playerPosY<=0){
                playerPosY=0
            }
        }
        moves.add("S")
        invalidate()
    }
    fun moveLeft(){
        if(move)
        {
            gridPosX--
            if(gridPosX<=0){
                gridPosX=0
            }
        }else{
            playerPosX--
            if(playerPosX<=0){
                playerPosX=0
            }
        }
        moves.add("A")
        invalidate()
    }
    fun moveRight(){
        if(move)
        {
            gridPosX++
            if(gridPosX<=0){
                gridPosX=0
            }
        }else{
            playerPosX++
            if(playerPosX<=0){
                playerPosX=0
            }
        }
        moves.add("D")
        invalidate()
    }
    fun performMoveClicked(){
        //gridScale=2f
        calcSize()
        //move=true
        gridPosY = playerPosY
        gridPosX = playerPosX
        moves=ArrayList()
        invalidate()
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
    fun done(){
        WebSocketService.getInstance().performMovement(moves)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calcSize()
        if(!move) {
            gameBoardBitmap?.let {
                boardPosX = (canvas.width - it.width) / 2f
                boardPosY = (canvas.height - it.height) / 2f
                canvas.drawBitmap(it, boardPosX, boardPosY, null)
                playerX = ((playerPosX * (gridSize / 25)) + boardPosX)
                playerY = ((playerPosY * (gridSize / 25)) + boardPosY)
            }

            playerBitmap?.let {

                canvas.drawBitmap(it, playerX, playerY, null) // Draw the bitmap

            }

        }
        else{
            gameBoardBitmap?.let {
                var centerX = (canvas.width - playerSizeX)/2f
                var centerY = (canvas.width - playerSizeY)/2f


                boardPosX = (centerX -(gridPosX * (gridSize / 25)) )
                boardPosY = (centerY -(gridPosY * (gridSize / 25)) )
                println((gridSize / 25))
                println(boardPosX)
                canvas.drawBitmap(it, boardPosX, boardPosY, null)
            }

            playerBitmap?.let {
                playerX = (canvas.width - it.width)/2f
                playerY = (canvas.height - it.height) / 2f
                println(playerX)
                canvas.drawBitmap(it, playerX, playerY, null) // Draw the bitmap
            }
        }

    }

    // Method to update player position (example)
    fun setPlayerPosition(x: Float, y: Float) {
        playerX = x
        playerY = y
        invalidate() // Request a redraw of the view
    }

    fun centeredPlayerMovement(){

    }
}