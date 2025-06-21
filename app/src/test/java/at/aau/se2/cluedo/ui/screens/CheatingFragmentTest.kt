package at.aau.se2.cluedo.ui.screens

import android.widget.ArrayAdapter
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import at.aau.se2.cluedo.R
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Mock ViewModelStoreOwner for FragmentScenario to provide a ViewModelStore.
 * This is necessary because `activityViewModels()` requires an `Activity` or `Fragment`
 * with a `ViewModelStoreOwner`. In isolated Fragment tests, we need to provide one.
 */
class TestViewModelStoreOwner : ViewModelStoreOwner {

    override val viewModelStore: ViewModelStore
        get() = viewModelStore

    fun clearViewModelStore() {
        viewModelStore.clear()
    }
}

@ExperimentalCoroutinesApi
class CheatingFragmentTest {

    // Mock dependencies
    private lateinit var mockLobbyViewModel: LobbyViewModel
    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var lobbyStateFlow: MutableStateFlow<Lobby?>
    private lateinit var errorMessagesFlow: MutableStateFlow<String?>

    // Needed for FragmentScenario, as activityViewModels requires a ViewModelStoreOwner
    private lateinit var testViewModelStoreOwner: TestViewModelStoreOwner

    @Before
    fun setup() {
        // Initialize mock objects
        mockWebSocketService = mockk(relaxed = true)
        mockLobbyViewModel = mockk(relaxed = true)

        // Initialize MutableStateFlows for controlling ViewModel behavior
        lobbyStateFlow = MutableStateFlow(null)
        errorMessagesFlow = MutableStateFlow(null)

        // Stub the ViewModel properties to return our MutableStateFlows
        every { mockLobbyViewModel.lobbyState } returns lobbyStateFlow
        every { mockLobbyViewModel.errorMessages } returns errorMessagesFlow
        every { mockLobbyViewModel.webSocketService } returns mockWebSocketService

        // Provide a TestViewModelStoreOwner for the FragmentScenario
        testViewModelStoreOwner = TestViewModelStoreOwner()

        // Mock the `activityViewModels` delegate to return our mocked ViewModel
        // This is a common pattern for testing Fragments with shared ViewModels
        mockkObject(at.aau.se2.cluedo.viewmodels.LobbyViewModel_HiltModules_KeyInjection_class) // Replace with actual generated class if different
        every {
            at.aau.se2.cluedo.viewmodels.LobbyViewModel_HiltModules_KeyInjection_class.provideLobbyViewModel(any()) // Replace `any()` with the actual ViewModelProvider.Factory type if needed
        } returns mockLobbyViewModel
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll() // Unmock any static/object mocks
        testViewModelStoreOwner.clearViewModelStore()
    }

    private fun launchFragment(): FragmentScenario<CheatingFragment> {
        // Use a ViewModelStoreOwner for the FragmentScenario.
        // This is a workaround for activityViewModels in isolated fragment testing.
        val scenario = launchFragmentInContainer<CheatingFragment>(
            fragmentArgs = null,
            factory = object : FragmentScenario.FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    val fragment = CheatingFragment()
                    // Manually inject the ViewModel, or use a custom factory if using Hilt/Koin
                    // For now, we've mocked the Hilt module directly.
                    return fragment
                }
            }
        )
        scenario.moveToState(Lifecycle.State.STARTED)
        return scenario
    }

    @Test
    fun testFragmentLaunches() = runTest {
        val scenario = launchFragment()
        scenario.onFragment { fragment ->
            // Verify that the fragment's root view is not null
            assert(fragment.view != null)
            // Verify that the binding is not null
            assert(fragment.binding != null)
            // Verify that the UI elements are displayed
            onView(withId(R.id.cheaterSpinner)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonSusbectCheating)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonCancelCheating)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testSpinnerPopulation() = runTest {
        val scenario = launchFragment()

        val currentPlayer = Player("PlayerA", "Mr. Green")
        val otherPlayer1 = Player("PlayerB", "Mrs. Peacock")
        val otherPlayer2 = Player("PlayerC", "Miss Scarlett")

        val lobby = Lobby(
            id = "testLobby",
            players = mutableListOf(currentPlayer, otherPlayer1, otherPlayer2),
            maxPlayers = 3
        )

        // Mock getCurrentPlayer to return the current player
        every { mockWebSocketService.getPlayer() } returns currentPlayer

        // Update the lobby state to trigger spinner population
        lobbyStateFlow.value = lobby

        scenario.onFragment { fragment ->
            val spinner = fragment.binding.cheaterSpinner
            val adapter = spinner.adapter as ArrayAdapter<String>

            // Expected items: "Select player to report...", "PlayerB (Mrs. Peacock)", "PlayerC (Miss Scarlett)"
            assert(adapter.count == 3)
            assert(adapter.getItem(0) == "Select player to report...")
            assert(adapter.getItem(1) == "PlayerB (Mrs. Peacock)")
            assert(adapter.getItem(2) == "PlayerC (Miss Scarlett)")
        }
    }

    @Test
    fun testCancelButtonDismissesFragment() = runTest {
        val scenario = launchFragment()

        // Perform click on cancel button
        onView(withId(R.id.buttonCancelCheating)).perform(click())

        // Verify that the fragment is effectively gone from the back stack
        // This can be checked by moving to DESTROYED state or asserting the view is not present
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun testSubmitSuspicion_noPlayerSelected() = runTest {
        val scenario = launchFragment()
        var decorView: View? = null
        scenario.onFragment { fragment ->
            decorView = fragment.requireActivity().window.decorView
        }

        // Mock getCurrentPlayer
        every { mockWebSocketService.getPlayer() } returns Player("PlayerA", "Mr. Green")

        // Click submit without selecting a player (default is position 0)
        onView(withId(R.id.buttonSusbectCheating)).perform(click())

        // Verify that a Toast message is displayed
        onView(withText("Please select a player to report"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))

        // Verify that reportCheating was NOT called
        verify(exactly = 0) { mockWebSocketService.reportCheating(any(), any(), any()) }
    }

    @Test
    fun testSubmitSuspicion_success() = runTest {
        val scenario = launchFragment()

        val currentPlayer = Player("PlayerA", "Mr. Green")
        val suspectedPlayer = Player("PlayerB", "Mrs. Peacock")
        val lobbyId = "testLobby123"

        val lobby = Lobby(
            id = lobbyId,
            players = mutableListOf(currentPlayer, suspectedPlayer),
            maxPlayers = 2
        )

        // Mock current player and lobby state
        every { mockWebSocketService.getPlayer() } returns currentPlayer
        lobbyStateFlow.value = lobby

        // Select the suspected player in the spinner
        onView(withId(R.id.cheaterSpinner)).perform(click()) // Open spinner
        onView(withText("PlayerB (Mrs. Peacock)")).inRoot(isPlatformPopup()).perform(click()) // Select item

        // Get decorView for Toast verification
        var decorView: View? = null
        scenario.onFragment { fragment ->
            decorView = fragment.requireActivity().window.decorView
        }

        // Click submit button
        onView(withId(R.id.buttonSusbectCheating)).perform(click())

        // Verify that reportCheating was called with correct arguments
        verify(exactly = 1) { mockWebSocketService.reportCheating(lobbyId, suspectedPlayer.name, currentPlayer.name) }

        // Verify Toast message
        onView(withText("Reporting ${suspectedPlayer.name} for cheating..."))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))

        // Verify that the fragment is dismissed (popBackStack() was called)
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun testSubmitSuspicion_currentPlayerNotFound() = runTest {
        val scenario = launchFragment()
        var decorView: View? = null
        scenario.onFragment { fragment ->
            decorView = fragment.requireActivity().window.decorView
        }

        val suspectedPlayer = Player("PlayerB", "Mrs. Peacock")
        val lobbyId = "testLobby123"
        val lobby = Lobby(
            id = lobbyId,
            players = mutableListOf(suspectedPlayer), // No current player in this mock
            maxPlayers = 1
        )

        // Mock getCurrentPlayer to return null
        every { mockWebSocketService.getPlayer() } returns null
        lobbyStateFlow.value = lobby

        // Select a player (even if current player is null, we simulate selection)
        onView(withId(R.id.cheaterSpinner)).perform(click())
        onView(withText("PlayerB (Mrs. Peacock)")).inRoot(isPlatformPopup()).perform(click())

        // Click submit button
        onView(withId(R.id.buttonSusbectCheating)).perform(click())

        // Verify Toast message
        onView(withText("Error: Current player data not found"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))

        // Verify that reportCheating was NOT called
        verify(exactly = 0) { mockWebSocketService.reportCheating(any(), any(), any()) }
    }

    @Test
    fun testSubmitSuspicion_selectedPlayerNotFoundInCurrentPlayers() = runTest {
        val scenario = launchFragment()
        var decorView: View? = null
        scenario.onFragment { fragment ->
            decorView = fragment.requireActivity().window.decorView
        }

        val currentPlayer = Player("PlayerA", "Mr. Green")
        val lobbyId = "testLobby123"
        val lobby = Lobby(
            id = lobbyId,
            players = mutableListOf(currentPlayer), // Only current player in lobby
            maxPlayers = 1
        )

        // Mock current player and lobby state
        every { mockWebSocketService.getPlayer() } returns currentPlayer
        lobbyStateFlow.value = lobby

        // Simulate selecting a player that is *not* in `currentPlayers` (e.g., from an old list)
        // We can't directly simulate this via the spinner as the adapter is updated.
        // Instead, we'll setup the spinner adapter with an extra item and then click it.
        scenario.onFragment { fragment ->
            val spinner = fragment.binding.cheaterSpinner
            val adapter = ArrayAdapter(
                fragment.requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Select player to report...", "NonExistentPlayer (Character)")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(1) // Select the "NonExistentPlayer"
        }

        // Click submit button
        onView(withId(R.id.buttonSusbectCheating)).perform(click())

        // Verify Toast message
        onView(withText("Error: Selected player data not found"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))

        // Verify that reportCheating was NOT called
        verify(exactly = 0) { mockWebSocketService.reportCheating(any(), any(), any()) }
    }


    @Test
    fun testSubmitSuspicion_lobbyIdNotFound() = runTest {
        val scenario = launchFragment()
        var decorView: View? = null
        scenario.onFragment { fragment ->
            decorView = fragment.requireActivity().window.decorView
        }

        val currentPlayer = Player("PlayerA", "Mr. Green")
        val suspectedPlayer = Player("PlayerB", "Mrs. Peacock")

        val lobby = Lobby(
            id = null, // Simulate null lobby ID
            players = mutableListOf(currentPlayer, suspectedPlayer),
            maxPlayers = 2
        )

        // Mock current player and lobby state (with null lobby ID)
        every { mockWebSocketService.getPlayer() } returns currentPlayer
        lobbyStateFlow.value = lobby

        // Select the suspected player in the spinner
        onView(withId(R.id.cheaterSpinner)).perform(click())
        onView(withText("PlayerB (Mrs. Peacock)")).inRoot(isPlatformPopup()).perform(click())

        // Click submit button
        onView(withId(R.id.buttonSusbectCheating)).perform(click())

        // Verify Toast message
        onView(withText("Error: Lobby ID not available"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))

        // Verify that reportCheating was NOT called
        verify(exactly = 0) { mockWebSocketService.reportCheating(any(), any(), any()) }
    }
}