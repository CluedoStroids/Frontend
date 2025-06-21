package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private var _suggestionNotificationData = MutableStateFlow<SuggestionRequest?>(null)
    val suggestionNotificationData: StateFlow<SuggestionRequest?> = _suggestionNotificationData

    private var _processingSuggestion = MutableStateFlow<Boolean>(false)
    val processingSuggestion: StateFlow<Boolean> = _processingSuggestion

    init {
        viewModelScope.launch {
            turnBasedWebSocketService.suggestionData.collect { suggestion ->
                _suggestionNotificationData.value = suggestion
            }

            turnBasedWebSocketService.processSuggestion.collect { processing ->
                _processingSuggestion.value = processing
            }

        }
    }

}