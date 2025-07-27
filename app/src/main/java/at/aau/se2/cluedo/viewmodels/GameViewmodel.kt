package at.aau.se2.cluedo.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.BasicCard
import at.aau.se2.cluedo.data.models.EmojiRequest
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.SuggestionRequest
import at.aau.se2.cluedo.data.models.SuggestionResponse
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    val webSocketService: WebSocketService = WebSocketService.getInstance(),
    val turnBasedWebSocketService: TurnBasedWebSocketService = TurnBasedWebSocketService.getInstance()
) : ViewModel() {

    private val _suggestionNotificationData = MutableStateFlow<SuggestionRequest?>(null)
    val suggestionNotificationData: StateFlow<SuggestionRequest?> = _suggestionNotificationData

    private val _processingSuggestion = MutableStateFlow<Boolean>(false)
    val processingSuggestion: StateFlow<Boolean> = _processingSuggestion

    private val _resultSuggestion = MutableStateFlow<SuggestionResponse?>(null)
    val resultSuggestion: StateFlow<SuggestionResponse?> = _resultSuggestion

    private val _receivedEmojis = MutableStateFlow<EmojiRequest?>(null)
    val receivedEmojis: StateFlow<EmojiRequest?> = _receivedEmojis

    init {
        viewModelScope.launch {
            turnBasedWebSocketService.suggestionData.collect { suggestion ->
                _suggestionNotificationData.value = suggestion
            }
        }

        viewModelScope.launch {
            turnBasedWebSocketService.processSuggestion.collect { processing ->
                _processingSuggestion.value = processing
            }
        }

        viewModelScope.launch {
            turnBasedWebSocketService.resultSuggestion.collect { result ->
                _resultSuggestion.value = result
            }
        }

        viewModelScope.launch {
            turnBasedWebSocketService.receivedEmojis.collect { result ->
                _receivedEmojis.value = result
            }
        }

    }

    fun getMatchingCards(): List<BasicCard> {
        var room = _suggestionNotificationData.value?.room
        var suspect = _suggestionNotificationData.value?.suspect
        var weapon = _suggestionNotificationData.value?.weapon
        var suggestionCards = listOf(room,suspect,weapon)

        var playerCards = webSocketService.player.value?.cards
        var matchingCards = mutableListOf<BasicCard>()

        playerCards?.forEach { card ->
            if(suggestionCards.contains(card.cardName)){
                matchingCards.add(card)
            }
        }

        return matchingCards
    }

    fun sendSuggestionResponse(lobbyId: String, cardName: String){
        var playerId: String = suggestionNotificationData.value?.playerId.toString()
        turnBasedWebSocketService.makeSuggestionResponse(lobbyId, playerId,cardName)
    }

    fun sendEmojis(lobbyId: String, player: Player?, text: String){
        if(player != null){
            Log.d("EMOJI","VM: Send $text to ${player.name}")
            turnBasedWebSocketService.sendEmojis(lobbyId = lobbyId,
                                                 playerName = player.name,
                                                 playerId = player.playerID,
                                                 text = text)
        }
    }

}