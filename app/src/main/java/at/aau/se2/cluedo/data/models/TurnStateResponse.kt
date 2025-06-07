package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class TurnStateResponse(
    @SerializedName("lobbyId")
    val lobbyId: String = "",
    
    @SerializedName("currentPlayerName")
    val currentPlayerName: String = "",
    
    @SerializedName("turnState")
    val turnState: String = "",
    
    @SerializedName("diceValue")
    val diceValue: Int = 0,
    
    @SerializedName("canMakeSuggestion")
    val canMakeSuggestion: Boolean? = null,
    
    @SerializedName("canMakeAccusation")
    val canMakeAccusation: Boolean? = null,
    
    @SerializedName("message")
    val message: String? = null
)
