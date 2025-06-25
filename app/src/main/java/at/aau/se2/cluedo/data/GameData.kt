package at.aau.se2.cluedo.data

import at.aau.se2.cluedo.data.models.GameBoardCell
import at.aau.se2.cluedo.data.models.Player
import com.google.gson.annotations.SerializedName

data class GameData(
    @SerializedName("id") val id: String = "",
    @SerializedName("players") val players: List<Player> = listOf(),
    @SerializedName("playingPlayer") val player:Player= Player(),
    @SerializedName("grid") var grid: Array<Array<GameBoardCell>> = emptyArray()
)
