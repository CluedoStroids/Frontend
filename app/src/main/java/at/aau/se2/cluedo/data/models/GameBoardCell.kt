package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class GameBoardCell (
    @SerializedName("x") val x: Int = 0,
    @SerializedName("y") val y: Int = 0,
    @SerializedName("cellType") val cellType: CellType,
    @SerializedName("room") val room:Room
)