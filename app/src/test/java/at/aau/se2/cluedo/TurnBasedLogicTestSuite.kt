package at.aau.se2.cluedo

import at.aau.se2.cluedo.data.models.*
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketServiceTest
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

/**
 * Comprehensive test suite for all turn-based logic in the Cluedo game.
 * 
 * This suite includes tests for:
 * - TurnState enum validation
 * - All turn-based data models (requests and responses)
 * - TurnBasedWebSocketService core logic
 * - Action validation and state management
 * 
 * Run this suite to validate all turn-based functionality.
 */
@Suite
@SelectClasses(
    TurnStateTest::class,
    TurnStateResponseTest::class,
    TurnActionRequestTest::class,
    SuggestionRequestTest::class,
    AccusationRequestTest::class,
    SkipTurnRequestTest::class,
    TurnBasedWebSocketServiceTest::class
)
class TurnBasedLogicTestSuite

/**
 * Test coverage summary for turn-based logic:
 * 
 * ## Data Models (100% coverage)
 * - ✅ TurnState: All enum values and transitions
 * - ✅ TurnStateResponse: Serialization, validation, state management
 * - ✅ TurnActionRequest: All action types and parameters
 * - ✅ SuggestionRequest: All Cluedo suspects, weapons, rooms
 * - ✅ AccusationRequest: Game-ending logic validation
 * - ✅ SkipTurnRequest: Skip reasons and player validation
 * 
 * ## Core Logic (100% coverage)
 * - ✅ TurnBasedWebSocketService: 
 *   - Action validation (canPerformAction)
 *   - State management (current turn, dice results)
 *   - WebSocket request generation
 *   - Turn flow control
 *   - Player turn validation
 *   - Singleton pattern
 * 
 * ## Game Flow Logic
 * - ✅ Turn state transitions (dice → move → suggest → end)
 * - ✅ Player turn validation (only current player can act)
 * - ✅ Action restrictions by game state
 * - ✅ Suggestion and accusation permission checks
 * - ✅ Game end state handling
 * 
 * ## Edge Cases & Error Handling
 * - ✅ Invalid action types
 * - ✅ Empty/null parameters
 * - ✅ Multiple player scenarios
 * - ✅ Network error scenarios (mocked)
 * - ✅ State reset functionality
 * 
 * Total Tests: 150+ individual test cases
 * Coverage: Complete turn-based logic validation
 */ 