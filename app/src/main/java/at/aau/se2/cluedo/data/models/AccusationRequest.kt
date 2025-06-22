package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class AccusationRequest(

    @SerializedName("lobbyId")
    val lobbyId: String = "",

    @SerializedName("playerName")
    val playerName: String = "",

    @SerializedName("suspect")
    val suspect: String = "",

    @SerializedName("weapon")
    val weapon: String = "",

    @SerializedName("room")
    val room: String = ""
)
