package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import com.google.gson.annotations.SerializedName

class BasicCard(
    @SerializedName("cardName") val cardName: String = "",
    @SerializedName("type") val type: CardType? = null
) {

    val imageResId: Int?
        get() = Card.fromCardName(cardName)?.imageResId

    companion object {
        fun getCardIDs(basicCards: List<BasicCard>?): List<Int> {
            return basicCards.orEmpty()
                .mapNotNull { it.imageResId } // Use the computed property
        }
    }

}

