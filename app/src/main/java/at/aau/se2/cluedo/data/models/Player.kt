package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Player(
    @SerializedName("name") val name: String = "",
    @SerializedName("character") val character: String = "",
    @SerializedName("playerID") val playerID: String = UUID.randomUUID().toString(),
    @SerializedName("x") val x: Int = 0,
    @SerializedName("y") val y: Int = 0,
    @SerializedName("isCurrentPlayer") val isCurrentPlayer: Boolean = false,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("hasWon") val hasWon: Boolean = false,
    @SerializedName("color") val color: PlayerColor = PlayerColor.RED,
    val active: Boolean = true

)
