package at.aau.se2.cluedo.data.models

data class AccusationRequest(
    val lobbyId: String,
    val suspect: String,
    val room: String,
    val weapon: String,
    val username: String
)



