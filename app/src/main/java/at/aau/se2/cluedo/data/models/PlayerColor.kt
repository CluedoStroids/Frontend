package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import com.google.gson.annotations.SerializedName

enum class PlayerColor(val img:Int) {
    @SerializedName("RED")
    RED(R.drawable.figure_red),

    @SerializedName("BLUE")
    BLUE(R.drawable.figure_blue),
    
    @SerializedName("GREEN")
    GREEN(R.drawable.figure_green),
    
    @SerializedName("YELLOW")
    YELLOW(R.drawable.figure_yellow),
    
    @SerializedName("PURPLE")
    PURPLE(R.drawable.figure_purple),
    
    @SerializedName("WHITE")
    WHITE(R.drawable.figure_white)
}
