package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

data class EmojiRequest (
    @SerializedName("username") val username: String = "",
    @SerializedName("text") val text: String = ""
)