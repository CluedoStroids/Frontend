package at.aau.se2.cluedo.data.models


class Room {
    private var name: String? = null
    private var playersInRoom: MutableList<Player?>? = null

    fun Room(name: String?) {
        this.name = name
        this.playersInRoom = ArrayList<Player?>()
    }

    fun playerEntersRoom(player: Player?) {
        playersInRoom!!.add(player)
    }

    fun playerLeavesRoom(player: Player?) {
        playersInRoom!!.remove(player)
    }

    fun getPlayersInRoom(): MutableList<Player?> {
        return ArrayList<Player?>(playersInRoom)
    }
    fun getName():String{
        return name.toString();
    }

}