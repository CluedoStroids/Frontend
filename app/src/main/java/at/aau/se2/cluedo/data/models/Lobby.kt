package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class Lobby(
    @SerializedName("id") val id: String = "",
    @SerializedName("host") val host: Player = Player(),
    @SerializedName("players") val players: List<Player> = listOf()
)
