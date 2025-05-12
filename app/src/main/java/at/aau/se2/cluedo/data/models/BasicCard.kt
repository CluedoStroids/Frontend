package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import com.google.gson.annotations.SerializedName

class BasicCard {

    companion object{
        val cardImageToPNG = mapOf(
            "Pipe" to R.drawable.card_pipe,
            "Rope" to R.drawable.card_rope,
            "Wrench" to R.drawable.card_wrench,
            "Pistol" to R.drawable.card_pistol,
            "Dagger" to R.drawable.card_dagger,
            "Candlestick" to R.drawable.card_candlestick,

            "Kitchen" to R.drawable.kitchen,
            "Wintergarden" to R.drawable.wintergarden,
            "Music room" to R.drawable.music_room,
            "Billard room" to R.drawable.billard_room,
            "Dining room" to R.drawable.dining_room,
            "Hall" to R.drawable.hall,
            "Library" to R.drawable.library,
            "Salon" to R.drawable.salon,
            "Office" to R.drawable.office,

            "Miss Scarlet" to R.drawable.playercard_red,
            "Colonel Mustard" to R.drawable.playercard_yellow,
            "Mrs. White" to R.drawable.playercard_white,
            "Mr. Green" to R.drawable.playercard_green,
            "Mrs. Peacock" to R.drawable.playercard_blue,
            "Professor Plum" to R.drawable.playercard_purple
        )

        fun getCardIDs(cards: List<BasicCard>?): List<Int>{
            var ids: ArrayList<Int> = ArrayList<Int>()
            cards?.forEach {
                card -> cardImageToPNG[card.cardName]?.let { ids.add(it) }
            }
            return ids
        }
    }

    @SerializedName("cardName") val cardName: String = "";
    @SerializedName("type") val type: CardType? = null;
}

enum class CardType {
    WEAPON,
    ROOM,
    CHARACTER
}
