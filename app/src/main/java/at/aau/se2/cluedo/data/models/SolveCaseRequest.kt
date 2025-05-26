package at.aau.se2.cluedo.data.models

// fixme consider the LobbyRequests file, or extract each req/res in a single file
data class SolveCaseRequest(
    val lobbyId: String,
    val suspect: String,
    val room: String,
    val weapon: String,
    val username: String
)



