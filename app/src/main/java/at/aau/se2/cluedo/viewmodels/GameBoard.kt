package at.aau.se2.cluedo.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.collection.IntIntPair
import at.aau.se2.cluedo.data.GameData
import at.aau.se2.cluedo.data.models.CellType
import at.aau.se2.cluedo.data.models.GameBoard
import at.aau.se2.cluedo.data.models.GameBoardCell
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.Room
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
    private var gridScale:Float= 0.8f
    private var playerPosX=24
    private var playerPosY=10
    var gridSize= sizeX*gridScale
    private var playerSizeX=(gridSize/25).toInt()
    private var playerSizeY=(gridSize/25).toInt()


    private var players:List<Player>?= ArrayList()
    private var moves= arrayListOf<String>()

    private var playerArrPos:Int=0
    private var firstRound:Boolean=true
    private var inRoom: Boolean=false
    private var inDoor: Boolean=false
    private var leaveRoom: Boolean=false
    private var outsideCoordX=0
    private var outsideCoordY=0
    var id:String? =""
    //Button

    fun init() {
        //WebSocketService.getInstance().players()
        id = WebSocketService.getInstance().lobbyState.value?.id
        var player = WebSocketService.getInstance().getPlayer()
        WebSocketService.getInstance().subscribeGetGameData(id!!) { gameData ->
            post {
                updateGameData(gameData)
            }
        }

        WebSocketService.getInstance().subscribeToMovementUpdates() { gameData ->
            post {
                updateGameData(gameData)
                if (inRoom&&leaveRoom){
                    updateGameData(gameData)
                }
            }
        }

        WebSocketService.getInstance().getGameBoard(id!!)

        WebSocketService.getInstance().subscribeGetGameBoard(id!!){
            gameBoard-> post{
            println("Gameboard found")
        }
        }

        if (player != null&&id!=null) {
            WebSocketService.getInstance().getGameData(id!!,player)
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
        if(inRoom&&!inDoor) {
            leaveRoom()
            moves.add("W")
            WebSocketService.getInstance().performMovement(moves)
            invalidate()
            return
        }
        else{
            inDoor=false
        }
        playerPosY--
        // Einmalige Antwort verarbeiten
            if(isWall()) {
                playerPosY++
            }else{
                moves.add("W") // Bewegung merken
                // Grenzen prüfen
                if (playerPosY < 0) {
                    playerPosY = 0
                }
                WebSocketService.getInstance().performMovement(moves)
                if(!inRoom){
                    safeCoord()
                }
            }
            moves=ArrayList()
            invalidate() // Zeichenfläche aktualisieren
    }

    fun moveDown(){

        if(inRoom&&!inDoor) {
            leaveRoom()
            moves.add("S")
            WebSocketService.getInstance().performMovement(moves)
            invalidate()
            return
        }
        else{
            inDoor=false
        }
        playerPosY++
        if (playerPosY > 25) {
            playerPosY = 24
        }
        // Einmalige Antwort verarbeite
            if (isWall()) {
                playerPosY--
            }else{
                moves.add("S") // Bewegung merken
                WebSocketService.getInstance().performMovement(moves)
                if(!inRoom){
                    safeCoord()
                }
            }
            moves=ArrayList()
            invalidate() // Zeichenfläche aktualisieren

    }

    fun moveLeft(){

        if(inRoom&&!inDoor) {
            leaveRoom()
            moves.add("A")
            WebSocketService.getInstance().performMovement(moves)

            invalidate()
            return
        }
        else{
            inDoor=false
        }
        playerPosX--
        if (playerPosX < 0) {
            playerPosX = 0
        }
            if (isWall()) {
                playerPosX++
            }
            else{
                moves.add("A") // Bewegung merken
                // Grenzen prüfen
                WebSocketService.getInstance().performMovement(moves)
                if(!inRoom){
                    safeCoord()
                }
            }
            moves=ArrayList()
            invalidate() // Zeichenfläche aktualisieren
    }

    fun moveRight(){

        if(inRoom&&!inDoor) {
            leaveRoom()
            moves.add("D")
            WebSocketService.getInstance().performMovement(moves)
            invalidate()
            return
        }
        else{
            inDoor=false
        }
        playerPosX++
        // Grenzen prüfen
        if (playerPosX > 25) {
            playerPosX = 24
        }
        // Einmalige Antwort verarbeiten
            if (isWall()) {
                playerPosX--
            }else{
                moves.add("D") // Bewegung merken
                WebSocketService.getInstance().performMovement(moves)
                if(!inRoom){
                    safeCoord()
                }
            }
            moves=ArrayList()
            invalidate() // Zeichenfläche aktualisieren
        }

    fun performMoveClicked(){
        //gridScale=2f
        //move=true
        calcSize()
        moves=ArrayList()
        invalidate()
    }


    fun isWall(): Boolean{
        val gameData = WebSocketService.getInstance().gameDataState.value
        val grid = gameData?.grid // Get the grid, can be null
        // Überprüfe, ob das Grid vorhanden und nicht leer ist
        if (grid == null || grid.isEmpty()) {
            WebSocketService.getInstance().subscribeGetGameBoard(id!!) { gameBoard ->
                post {
                    println("Gameboard found")

                }
            }
            WebSocketService.getInstance().getGameBoard(id.toString())
            Log.d("Debug", "GameBoard nicht vorhanden")
            return isWall() // Verhindere Bewegung, wenn das Board noch nicht geladen ist.
        }



        val column = grid[playerPosX] // Jetzt ist column ein Array

        // Und jetzt die y-Koordinate prüfen
        val y = playerPosY+1  // Beachten Sie die Anmerkung zu +1


        // Jetzt ist der Zugriff sicher
        val targetCell = grid[playerPosX][y]

        if(targetCell.cellType.equals(CellType.DOOR)){
            walkiIntoRoom(targetCell.room)
            inDoor=true
            Log.d("Debug", "Bewegdich")
        }
        return (targetCell.cellType.equals(CellType.ROOM))||(targetCell.cellType.equals(CellType.WALL));
    }


    fun walkiIntoRoom(room: Room)
    {
        //val gm: GameBoard = GameBoard();
        //val coord: IntIntPair=gm.placeInRoom(players?.get(playerArrPos)!!,room, players!!)
        inRoom=true
    }

    fun leaveRoom(){
        //WebSocketService.getInstance().gameDataState.value?.grid[playerPosX][playerPosY]?.room!!.playerLeavesRoom(players?.get(playerArrPos)!!)
        inRoom=false;
        leaveRoom=true
        invalidate()
    }

    fun safeCoord(){
        outsideCoordY=playerPosY;
        outsideCoordX=playerPosX;
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
            WebSocketService.getInstance().performMovement(moves)
        }

        invalidate()
    }

    fun updateGameData(gameData: GameData?) {
        this.players = gameData?.players ?: emptyList()
        if((firstRound||inRoom||leaveRoom)&& players != null && players!!.isNotEmpty()) {
            playerPosX = players!!.get(playerArrPos).x
            playerPosY = players!!.get(playerArrPos).y
            firstRound=false
            //if(leaveRoom==true)
                //inDoor=true;
            leaveRoom=false

        }
        if(playerPosX != players!!.get(playerArrPos).x || playerPosY != players!!.get(playerArrPos).y ){
            playerPosX = players!!.get(playerArrPos).x
            playerPosY = players!!.get(playerArrPos).y
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

}