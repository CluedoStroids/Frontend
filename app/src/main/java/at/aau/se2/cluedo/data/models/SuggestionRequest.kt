package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class SuggestionRequest(
    @SerializedName("playerID")
    val playerId: String = "",

    @SerializedName("suspect")
    val suspect: String = "",

    @SerializedName("weapon")
    val weapon: String = "",

    @SerializedName("room")
    val room: String = ""
)
