package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class ActiveLobbiesResponse(
    @SerializedName("lobbies") val lobbies: List<LobbyInfo> = listOf()
) {
    data class LobbyInfo(
        @SerializedName("id") val id: String = "",
        @SerializedName("hostName") val hostName: String = "",
        @SerializedName("playerCount") val playerCount: Int = 0
    )
}

data class CanStartGameResponse(
    @SerializedName("canStart") val canStart: Boolean = false
)

data class GameStartedResponse(
    @SerializedName("lobbyId") val lobbyId: String = "",
    @SerializedName("players") var players: List<Player> = listOf()
)

data class PerformMoveResponse(
    @SerializedName("player") var player: Player = Player(),
    @SerializedName("moves") var moves: List<String> = arrayListOf()
) 