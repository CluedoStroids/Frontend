package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class SuggestionResponse(
    @SerializedName("playerId")
    val playerId: String = "",

    @SerializedName("playerName")
    val playerName: String = "",

    @SerializedName("cardName")
    val cardName: String = ""
)