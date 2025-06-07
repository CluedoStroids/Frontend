package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName


data class SkipTurnRequest(
    @SerializedName("playerName") 
    val playerName: String = "",
    
    @SerializedName("reason") 
    val reason: String = "Player manually skipped turn"
)
