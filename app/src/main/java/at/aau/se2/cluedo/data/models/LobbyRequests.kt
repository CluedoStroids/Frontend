package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class CreateLobbyRequest(
    @SerializedName("player") val player: Player
)

data class JoinLobbyRequest(
    @SerializedName("player") val player: Player
)

data class LeaveLobbyRequest(
    @SerializedName("player") val player: Player
)

data class GetActiveLobbiesRequest(
    @SerializedName("dummy") val dummy: String = ""
)

data class ActiveLobbiesResponse(
    @SerializedName("lobbies") val lobbies: List<LobbyInfo> = listOf()
) {
    data class LobbyInfo(
        @SerializedName("id") val id: String = "",
        @SerializedName("hostName") val hostName: String = "",
        @SerializedName("playerCount") val playerCount: Int = 0
    )
}
