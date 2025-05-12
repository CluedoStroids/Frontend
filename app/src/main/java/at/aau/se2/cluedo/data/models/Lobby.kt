package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class Lobby(
    @SerializedName("id") val id: String = "",
    @SerializedName("host") val host: Player = Player(),
    @SerializedName("participants") val participants: List<String> = listOf(),
    @SerializedName("players") var players: List<Player> = listOf(),
    @SerializedName("winnerUsername") val winnerUsername: String? = null
)

enum class LobbyStatus(val text: String) {
    CREATING("Creating..."),
}
