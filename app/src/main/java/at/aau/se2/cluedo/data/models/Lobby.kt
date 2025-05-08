package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class Lobby(
    @SerializedName("id") val id: String = "",
    @SerializedName("host") val host: String = "",
    @SerializedName("participants") val participants: List<String> = listOf(),
    @SerializedName("players") val players: List<Player> = listOf(),
    @SerializedName("winnerUsername") val winnerUsername: String? = null
)


data class CreateLobbyRequest(
    @SerializedName("username") val username: String
)

data class JoinLobbyRequest(
    @SerializedName("username") val username: String
)

data class LeaveLobbyRequest(
    @SerializedName("username") val username: String
)


data class Player(
    val username: String,
    val characterName: String,
    val isEliminated: Boolean = false,
    val hasWon: Boolean = false
)




