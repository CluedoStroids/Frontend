package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

enum class PlayerColor {
    @SerializedName("RED")
    RED,
    
    @SerializedName("BLUE")
    BLUE,
    
    @SerializedName("GREEN")
    GREEN,
    
    @SerializedName("YELLOW")
    YELLOW,
    
    @SerializedName("PURPLE")
    PURPLE,
    
    @SerializedName("WHITE")
    WHITE
}
