package at.aau.se2.cluedo.data.models

import androidx.collection.IntIntPair
import at.aau.se2.cluedo.data.network.WebSocketService
import java.security.SecureRandom
import java.util.Vector
import kotlin.math.max

class GameBoard {
    var roomDefinitions: Array<Array<String?>?> = arrayOf<Array<String?>?>(
        arrayOf<String?>("Kitchen", "0", "1", "6", "6"),
        arrayOf<String?>("Ballroom", "8", "1", "8", "7"),
        arrayOf<String?>("Conservatory", "18", "1", "6", "5"),
        arrayOf<String?>("Dining Room", "0", "9", "8", "7"),
        arrayOf<String?>("Billiard Room", "18", "8", "7", "5"),
        arrayOf<String?>("Library", "17", "14", "8", "5"),
        arrayOf<String?>("Lounge", "0", "19", "7", "6"),
        arrayOf<String?>("Hall", "9", "18", "6", "7"),
        arrayOf<String?>("Study", "17", "21", "8", "4"),
    )

    fun placeInRoom(player: Player,room: Room,players: List<Player>): IntIntPair {
       room.playerEntersRoom(player);

        for (i:Int in 0 until roomDefinitions.size){
            if(roomDefinitions[i]?.get(0)?.equals(room.getName())!!){

                var maxX:Int =roomDefinitions[i]?.get(3)?.toInt()!!;
                var minX:Int = roomDefinitions[i]?.get(1)?.toInt()!!;
                var minY:Int=roomDefinitions[i]?.get(4)?.toInt()!!;
                var maxY:Int = roomDefinitions[i]?.get(2)?.toInt()!!;

                generateRandom(minX,maxX,minY,maxY,player)
                for (p:Player in players){
                    if(p.x == player.x&&p.y == player.y){
                        placeInRoom(player,room,players)
                    }
                }
            }
        }
        return IntIntPair(player.x,player.y);
    }
    fun generateRandom(minX:Int,maxX:Int,minY:Int,maxY:Int,player: Player){
        var rand: SecureRandom= SecureRandom();
        val randomX = minX + rand.nextInt(maxX - minX)
        val randomY = minY + rand.nextInt(maxY - minY)
        player.x = randomX
        player.y = randomY
    }
}

