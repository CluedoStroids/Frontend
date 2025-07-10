package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import com.google.gson.annotations.SerializedName

enum class Card(
    @SerializedName("cardName") val cardName: String,
    val type: CardType,
    val imageResId: Int
) {
    PIPE("Pipe", CardType.WEAPON, R.drawable.card_pipe),
    ROPE("Rope", CardType.WEAPON, R.drawable.card_rope),
    WRENCH("Wrench", CardType.WEAPON, R.drawable.card_wrench),
    PISTOL("Pistol", CardType.WEAPON, R.drawable.card_pistol),
    DAGGER("Dagger", CardType.WEAPON, R.drawable.card_dagger),
    CANDLESTICK("Candlestick", CardType.WEAPON, R.drawable.card_candlestick),

    KITCHEN("Kitchen", CardType.ROOM, R.drawable.kitchen),
    WINTERGARDEN("Wintergarden", CardType.ROOM, R.drawable.wintergarden),
    MUSIC_ROOM("Music room", CardType.ROOM, R.drawable.music_room),
    BILLIARD_ROOM("Billard room", CardType.ROOM, R.drawable.billard_room),
    DINING_ROOM("Dining room", CardType.ROOM, R.drawable.dining_room),
    HALL("Hall", CardType.ROOM, R.drawable.hall),
    LIBRARY("Library", CardType.ROOM, R.drawable.library),
    SALON("Salon", CardType.ROOM, R.drawable.salon),
    OFFICE("Office", CardType.ROOM, R.drawable.office),

    MISS_SCARLET("Miss Scarlet", CardType.CHARACTER, R.drawable.playercard_red),
    COLONEL_MUSTARD("Colonel Mustard", CardType.CHARACTER, R.drawable.playercard_yellow),
    MRS_WHITE("Mrs. White", CardType.CHARACTER, R.drawable.playercard_white),
    MR_GREEN("Mr. Green", CardType.CHARACTER, R.drawable.playercard_green),
    MRS_PEACOCK("Mrs. Peacock", CardType.CHARACTER, R.drawable.playercard_blue),
    PROFESSOR_PLUM("Professor Plum", CardType.CHARACTER, R.drawable.playercard_purple);

    companion object {
        fun fromCardName(name: String): Card? = entries.find { it.cardName == name }
    }
}

enum class CardType {
    WEAPON,
    ROOM,
    CHARACTER
}