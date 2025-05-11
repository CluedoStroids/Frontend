package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

class BasicCard {
    @SerializedName("cardName") val cardName: String = "";
    @SerializedName("type") val type: CardType? = null;
}

enum class CardType {
    WEAPON,
    ROOM,
    CHARACTER
}
