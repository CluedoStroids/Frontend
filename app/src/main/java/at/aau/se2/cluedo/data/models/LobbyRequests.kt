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
data class IsWallRequest(
    @SerializedName("x") var x: Int = 0,
    @SerializedName("y") var y: Int = 0
)