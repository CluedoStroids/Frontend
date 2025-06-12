package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class TurnActionRequest(
    @SerializedName("playerName")
    val playerName: String = "",
    
    @SerializedName("actionType")
    val actionType: String = "",
    
    @SerializedName("diceValue")
    val diceValue: Int = 0
)
