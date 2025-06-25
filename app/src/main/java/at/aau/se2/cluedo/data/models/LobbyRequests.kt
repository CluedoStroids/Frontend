package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class CreateLobbyRequest(
    @SerializedName("player") override val player: Player
) : PlayerRequest

data class JoinLobbyRequest(
    @SerializedName("player") override val player: Player
) : PlayerRequest

data class LeaveLobbyRequest(
    @SerializedName("player") override val player: Player
) : PlayerRequest

data class GetActiveLobbiesRequest(
    @SerializedName("dummy") val dummy: String = ""
)

data class StartGameRequest(
    @SerializedName("player") override val player: Player
) : PlayerRequest

data class CanStartGameResponse(
    @SerializedName("canStart") val canStart: Boolean = false
)

data class GameStartedResponse(
    @SerializedName("lobbyId") val lobbyId: String = "",
    @SerializedName("players") var players: List<Player> = listOf()
)
data class PerformMoveResponse(
    @SerializedName("player") var player:Player = Player(),
    @SerializedName("moves") var moves:List<String> = arrayListOf<String>()
)
