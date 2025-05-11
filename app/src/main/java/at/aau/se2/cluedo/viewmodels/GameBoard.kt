package at.aau.se2.cluedo.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaDrm.LogMessage
import android.util.AttributeSet
import android.util.Log
import android.view.View
import at.aau.se2.cluedo.data.GameData
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.network.WebSocketService
import com.example.myapplication.R

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

    private var playerArrPos:Int=0
    private var firstRound:Boolean=true
    //Button

    fun init() {
        //WebSocketService.getInstance().players()
        var id = WebSocketService.getInstance().lobbyState.value?.id
        var player = WebSocketService.getInstance().getPlayer()
        WebSocketService.getInstance().subscribeGetGameData(id!!) { gameData ->
            post {
                updateGameData(gameData)
            }
        }
        WebSocketService.getInstance().subscribeToMovementUpdates(id!!) { gameData ->
            post {
                updateGameData(gameData)
            }
        }

        if (player != null&&id!=null) {
            WebSocketService.getInstance().gameData(id,player)
        }


        players=WebSocketService.getInstance().gameDataState.value?.players

        if(players!=null) {
            for (i in 0..(players?.size!!-1)) {
                if (players!!.get(i).name != WebSocketService.getInstance().getPlayer()?.name) {
                    playersBitmap.add(
                        BitmapFactory.decodeResource(
                            resources,
                            players?.get(i)?.color?.img!!
                        )
                    )
                }
                else{
                    playerArrPos=i
                }
            }
        }

        if(WebSocketService.getInstance().getPlayer()!=null) {
            playerBitmap = BitmapFactory.decodeResource(
                resources,
                WebSocketService.getInstance().getPlayer()?.color?.img!!
            )
        }

        gameBoardBitmap = BitmapFactory.decodeResource(resources, R.drawable.gameboard)

        // Set initial position (example: center of the view)
        playerBitmap= playerBitmap?.let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        for(b in 0..(playersBitmap.size-1)){
            playersBitmap[b]=playersBitmap.get(b).let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,(sizeX*gridScale).toInt(),(sizeY*gridScale).toInt(),false) }

    }

    @SuppressLint("SuspiciousIndentation")
    fun moveUp() {
        val lobbyId = WebSocketService.getInstance().lobbyState.value?.id ?: return
        playerPosY--

        // Grenzen prüfen
        if (playerPosY < 0) {
            playerPosY = 0
            invalidate()
            return
        }

        // Wand-Abfrage auslösen
        WebSocketService.getInstance().isWall(lobbyId, playerPosX, playerPosY)

        // Einmalige Antwort verarbeiten
        WebSocketService.getInstance().subscribeIsWall(lobbyId) { isWall ->
            if (isWall) {
                playerPosY++ // Bewegung rückgängig machen
            } else {
                moves.add("W") // Bewegung merken
            }

            invalidate() // Zeichenfläche aktualisieren
        }
    }
    fun moveDown(){
        val lobbyId = WebSocketService.getInstance().lobbyState.value?.id ?: return
        playerPosY++

        // Grenzen prüfen
        if (playerPosY > 25) {
            playerPosY = 24
            invalidate()
            return
        }

        // Wand-Abfrage auslösen
        WebSocketService.getInstance().isWall(lobbyId, playerPosX, playerPosY)

        // Einmalige Antwort verarbeiten
        WebSocketService.getInstance().subscribeIsWall(lobbyId) { isWall ->
            if (isWall) {
                playerPosY-- // Bewegung rückgängig machen
            } else {
                moves.add("S") // Bewegung merken
            }

            invalidate() // Zeichenfläche aktualisieren
        }
    }
    fun moveLeft(){
        val lobbyId = WebSocketService.getInstance().lobbyState.value?.id ?: return
        playerPosX--

        // Grenzen prüfen
        if (playerPosX < 0) {
            playerPosX = 0
            invalidate()
            return
        }

        // Wand-Abfrage auslösen
        WebSocketService.getInstance().isWall(lobbyId, playerPosX, playerPosY)

        // Einmalige Antwort verarbeiten
        WebSocketService.getInstance().subscribeIsWall(lobbyId) { isWall ->
            if (isWall) {
                playerPosX++ // Bewegung rückgängig machen
            } else {
                moves.add("A") // Bewegung merken
            }

            invalidate() // Zeichenfläche aktualisieren
        }
    }
    fun moveRight(){
        val lobbyId = WebSocketService.getInstance().lobbyState.value?.id ?: return
        playerPosX++

        // Grenzen prüfen
        if (playerPosX > 25) {
            playerPosX = 24
            invalidate()
            return
        }

        // Wand-Abfrage auslösen
        WebSocketService.getInstance().isWall(lobbyId, playerPosX, playerPosY)

        // Einmalige Antwort verarbeiten
        WebSocketService.getInstance().subscribeIsWall(lobbyId) { isWall ->
            if (isWall) {
                playerPosX-- // Bewegung rückgängig machen
            } else {
                moves.add("D") // Bewegung merken
            }

            invalidate() // Zeichenfläche aktualisieren
        }
    }
    fun performMoveClicked(){
        //gridScale=2f
        calcSize()
        //move=true
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

        playerBitmap= playerBitmap?.let {Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false)}
        playerBitmap= playerBitmap?.let {Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false)}

        for(b in 0..(playersBitmap.size-1)){
            playersBitmap[b]=playersBitmap.get(b).let { Bitmap.createScaledBitmap(it,playerSizeX,playerSizeY,false) }
        }
        gameBoardBitmap = gameBoardBitmap?.let { Bitmap.createScaledBitmap(it,(sizeX*gridScale).toInt(),(sizeY*gridScale).toInt(),false) }
    }
    fun done(){
        var id = WebSocketService.getInstance().lobbyState.value?.id

        if (id != null) {
            WebSocketService.getInstance().performMovement(id,moves)
        }

        invalidate()
    }
    fun updateGameData(gameData: GameData?) {
        this.players = gameData?.players ?: emptyList()
        if(firstRound&& players != null && players!!.isNotEmpty()) {
            playerPosX = players!!.get(playerArrPos).x
            playerPosY = players!!.get(playerArrPos).y
            firstRound=false
        }
        // ggf. Bitmap aktualisieren oder andere Werte setzen
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calcSize()


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


            for (i in 0..(playersBitmap.size-1)){
                var playersx:Float=0f
                var playersy:Float=0f
                if(playerArrPos>i){
                    playersx=((players?.get(i)?.x!! * (gridSize / 25)) + boardPosX)
                    playersy = ((players?.get(i)?.y!! * (gridSize / 25)) + boardPosY)
                }else{
                    playersx=((players?.get(i+1)?.x!! * (gridSize / 25)) + boardPosX)
                    playersy = ((players?.get(i+1)?.y!! * (gridSize / 25)) + boardPosY)
                }
                playersBitmap.get(i).let {
                    canvas.drawBitmap(it, playersx, playersy, null) // Draw the bitmap
                }
            }
    }

    // Method to update player position (example)
    fun setPlayerPosition(x: Float, y: Float) {
        playerX = x
        playerY = y
        invalidate() // Request a redraw of the view
    }

}