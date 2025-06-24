package at.aau.se2.cluedo.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.BasicCard
import at.aau.se2.cluedo.data.models.SuggestionRequest
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

}