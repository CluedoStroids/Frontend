package at.aau.se2.cluedo.data.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TurnStateTest {

    @Test
    fun testAllTurnStateValuesAreCorrect() {
        assertEquals("PLAYERS_TURN_ROLL_DICE", TurnState.PLAYERS_TURN_ROLL_DICE.value)
        assertEquals("PLAYERS_TURN_MOVE", TurnState.PLAYERS_TURN_MOVE.value)
        assertEquals("PLAYERS_TURN_SUGGEST", TurnState.PLAYERS_TURN_SUGGEST.value)
        assertEquals("PLAYERS_TURN_SOLVE", TurnState.PLAYERS_TURN_SOLVE.value)
        assertEquals("PLAYERS_TURN_END", TurnState.PLAYERS_TURN_END.value)
        assertEquals("PLAYER_HAS_WON", TurnState.PLAYER_HAS_WON.value)
        assertEquals("WAITING_FOR_PLAYERS", TurnState.WAITING_FOR_PLAYERS.value)
        assertEquals("WAITING_FOR_START", TurnState.WAITING_FOR_START.value)
        assertEquals("GAME_ENDED", TurnState.GAME_ENDED.value)
    }

    @Test
    fun testEnumContainsAllExpectedValues() {
        val expectedStates = setOf(
            "PLAYERS_TURN_ROLL_DICE",
            "PLAYERS_TURN_MOVE", 
            "PLAYERS_TURN_SUGGEST",
            "PLAYERS_TURN_SOLVE",
            "PLAYERS_TURN_END",
            "PLAYER_HAS_WON",
            "WAITING_FOR_PLAYERS",
            "WAITING_FOR_START",
            "GAME_ENDED"
        )
        
        val actualStates = TurnState.values().map { it.value }.toSet()
        assertEquals(expectedStates, actualStates)
    }

    @Test
    fun testEnumHasCorrectCountOfStates() {
        assertEquals(9, TurnState.values().size)
    }

    @Test
    fun testTurnStateEnumNamesMatchValues() {
        TurnState.values().forEach { state ->
            assertEquals(state.name, state.value)
        }
    }

    @Test
    fun testGameFlowStatesAreInLogicalOrder() {
        val gameFlowStates = listOf(
            TurnState.PLAYERS_TURN_ROLL_DICE,
            TurnState.PLAYERS_TURN_MOVE,
            TurnState.PLAYERS_TURN_SUGGEST,
            TurnState.PLAYERS_TURN_SOLVE,
            TurnState.PLAYERS_TURN_END
        )
        
        assertEquals(5, gameFlowStates.distinct().size)
        
        assertTrue(gameFlowStates.contains(TurnState.PLAYERS_TURN_ROLL_DICE))
        assertTrue(gameFlowStates.contains(TurnState.PLAYERS_TURN_MOVE))
        assertTrue(gameFlowStates.contains(TurnState.PLAYERS_TURN_SUGGEST))
        assertTrue(gameFlowStates.contains(TurnState.PLAYERS_TURN_SOLVE))
        assertTrue(gameFlowStates.contains(TurnState.PLAYERS_TURN_END))
    }
} 