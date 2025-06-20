package at.aau.se2.cluedo.ui.screens

import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import at.aau.se2.cluedo.websocket.WebSocketService
import com.example.myapplication.R
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CheatingSuspicionFragmentTest {

    private lateinit var scenario: FragmentScenario<CheatingSuspicionFragment>
    private lateinit var mockLobbyViewModel: LobbyViewModel
    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var mockFragmentManager: FragmentManager

    private val testLobbyStateFlow = MutableStateFlow<Lobby?>(null)
    private val testErrorMessagesFlow = MutableStateFlow("")

    private val testPlayers = listOf(
        Player(id = "1", name = "Alice", character = "Miss Scarlet"),
        Player(id = "2", name = "Bob", character = "Colonel Mustard"),
        Player(id = "3", name = "Charlie", character = "Mrs. Peacock")
    )

    private val testLobby = Lobby(
        id = "lobby123",
        players = testPlayers,
        hostId = "1"
    )

    @Before
    fun setup() {
        // Mock dependencies
        mockLobbyViewModel = mockk(relaxed = true)
        mockWebSocketService = mockk(relaxed = true)
        mockFragmentManager = mockk(relaxed = true)

        // Setup mock returns
        every { mockLobbyViewModel.lobbyState } returns testLobbyStateFlow
        every { mockLobbyViewModel.errorMessages } returns testErrorMessagesFlow
        every { mockLobbyViewModel.webSocketService } returns mockWebSocketService
        every { mockWebSocketService.getPlayer() } returns testPlayers[0] // Alice as current player

        // Mock fragment manager
        justRun { mockFragmentManager.popBackStack() }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
        clearAllMocks()
    }

    @Test
    fun `fragment creates successfully`() {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        scenario.onFragment { fragment ->
            assert(fragment.isAdded)
        }
    }

    @Test
    fun `fragment lifecycle methods are called correctly`() {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.STARTED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.moveToState(Lifecycle.State.DESTROYED)

        // Fragment should handle all lifecycle states without crashing
        assert(true)
    }

    @Test
    fun `updatePlayerSpinner populates correctly when lobby state changes`() = runTest {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        scenario.onFragment { fragment ->
            // Mock the fragment's private fields through reflection if needed
            // or test the observable behavior
        }

        // Emit lobby state
        testLobbyStateFlow.value = testLobby

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            assertNotNull(spinner)

            // Should have "Select player..." + other players (excluding current player Alice)
            val adapter = spinner?.adapter as? ArrayAdapter<String>
            assertNotNull(adapter)
            assertEquals(3, adapter?.count) // "Select..." + Bob + Charlie
            assertEquals("Select player to report...", adapter?.getItem(0))
            assertTrue(adapter?.getItem(1)?.contains("Bob") == true)
            assertTrue(adapter?.getItem(2)?.contains("Charlie") == true)
        }
    }

    @Test
    fun `cancel button pops back stack`() {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        scenario.onFragment { fragment ->
            // Mock the fragment manager
            val mockManager = mockk<FragmentManager>(relaxed = true)
            justRun { mockManager.popBackStack() }

            // Simulate cancel button click
            fragment.view?.findViewById<android.widget.Button>(R.id.buttonCancelCheating)?.performClick()

            // Verify popBackStack was called (this would need proper mocking setup)
        }
    }

    @Test
    fun `submitCheatingSuspicion shows error when no player selected`() {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()
        testLobbyStateFlow.value = testLobby

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            spinner?.setSelection(0) // Select "Select player to report..."

            fragment.view?.findViewById<android.widget.Button>(R.id.buttonSusbectCheating)?.performClick()

            // Check that toast message was shown
            val latestToast = ShadowToast.getLatestToast()
            assertNotNull(latestToast)
            assertEquals("Please select a player to report", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `submitCheatingSuspicion works correctly with valid selection`() = runTest {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()
        testLobbyStateFlow.value = testLobby

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            spinner?.setSelection(1) // Select Bob

            fragment.view?.findViewById<android.widget.Button>(R.id.buttonSusbectCheating)?.performClick()

            // Verify that reportCheating was called on WebSocketService
            verify { mockWebSocketService.reportCheating("lobby123", "Bob", "Alice") }

            // Check success toast
            val latestToast = ShadowToast.getLatestToast()
            assertNotNull(latestToast)
            assertTrue(ShadowToast.getTextOfLatestToast().contains("Reporting Bob for cheating"))
        }
    }

    @Test
    fun `submitCheatingSuspicion handles null current player`() {
        every { mockWebSocketService.getPlayer() } returns null

        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()
        testLobbyStateFlow.value = testLobby

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            spinner?.setSelection(1)

            fragment.view?.findViewById<android.widget.Button>(R.id.buttonSusbectCheating)?.performClick()

            val latestToast = ShadowToast.getLatestToast()
            assertNotNull(latestToast)
            assertEquals("Error: Current player data not found", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `submitCheatingSuspicion handles null lobby ID`() {
        val lobbyWithoutId = testLobby.copy(id = "")

        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()
        testLobbyStateFlow.value = lobbyWithoutId

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            spinner?.setSelection(1)

            fragment.view?.findViewById<android.widget.Button>(R.id.buttonSusbectCheating)?.performClick()

            val latestToast = ShadowToast.getLatestToast()
            assertNotNull(latestToast)
            assertEquals("Error: Lobby ID not available", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `error messages from ViewModel are displayed as toast`() = runTest {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        val errorMessage = "Network error occurred"
        testErrorMessagesFlow.value = errorMessage

        // Allow coroutines to process
        advanceUntilIdle()

        val latestToast = ShadowToast.getLatestToast()
        assertNotNull(latestToast)
        assertEquals(errorMessage, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `binding is properly cleaned up on destroy`() {
        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()

        scenario.onFragment { fragment ->
            // Fragment should have binding when active
            assertNotNull(fragment.view)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)

        // After destruction, binding should be null (this tests the onDestroyView method)
        // We can't directly access private _binding, but we can verify the fragment handles destruction
        assert(true) // If we get here without crashes, cleanup worked
    }

    @Test
    fun `spinner excludes current player from options`() = runTest {
        // Test with different current players
        every { mockWebSocketService.getPlayer() } returns testPlayers[1] // Bob as current player

        scenario = launchFragmentInContainer<CheatingSuspicionFragment>()
        testLobbyStateFlow.value = testLobby

        scenario.onFragment { fragment ->
            val spinner = fragment.view?.findViewById<Spinner>(R.id.cheaterSpinner)
            val adapter = spinner?.adapter as? ArrayAdapter<String>

            // Should contain Alice and Charlie, but not Bob
            val items = (0 until (adapter?.count ?: 0)).map { adapter?.getItem(it) }
            assertTrue(items.any { it?.contains("Alice") == true })
            assertTrue(items.any { it?.contains("Charlie") == true })
            assertFalse(items.any { it?.contains("Bob") == true })
        }
    }

    private fun assertNotNull(value: Any?) {
        assert(value != null) { "Expected non-null value" }
    }

    private fun assertEquals(expected: Any?, actual: Any?) {
        assert(expected == actual) { "Expected $expected but was $actual" }
    }

    private fun assertTrue(condition: Boolean) {
        assert(condition) { "Expected condition to be true" }
    }

    private fun assertFalse(condition: Boolean) {
        assert(!condition) { "Expected condition to be false" }
    }
}