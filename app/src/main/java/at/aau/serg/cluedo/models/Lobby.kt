package at.aau.serg.cluedo.models

import com.google.gson.annotations.SerializedName

data class Lobby(
    @SerializedName("id") val id: String = "",
    @SerializedName("host") val host: String = "",
    @SerializedName("participants") val participants: List<String> = listOf()
)

data class CreateLobbyRequest(
    @SerializedName("username") val username: String
)

data class JoinLobbyRequest(
    @SerializedName("username") val username: String
)
