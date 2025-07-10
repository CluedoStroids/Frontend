package at.aau.se2.cluedo.data.models

import com.google.gson.annotations.SerializedName

/**
 * Base interface for requests that contain a player field
 */
interface PlayerRequest {
    @get:SerializedName("player")
    val player: Player
} 