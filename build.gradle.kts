// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.projectKey", "CluedoStroids_Frontend")
        property("sonar.organization", "cluedostroids")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/at/aau/se2/cluedo/data/Player.kt",
                "**/at/aau/se2/cluedo/data/AccusationRequest.kt",
                "**/at/aau/se2/cluedo/data/CellType.kt",
                "**/at/aau/se2/cluedo/data/AccusationRequest.kt",
                "**/at/aau/se2/cluedo/data/DiceResult.kt",
                "**/at/aau/se2/cluedo/data/GameBoard.kt",
                "**/at/aau/se2/cluedo/data/GameBoardCell.kt",
                "**/at/aau/se2/cluedo/data/Room.kt",
                "**/at/aau/se2/cluedo/data/SkipTurnRequest.kt",
                "**/at/aau/se2/cluedo/data/SuspectCheating.kt",
                "**/at/aau/se2/cluedo/data/TurnActionReques.kt",
                "**/at/aau/se2/cluedo/data/TurnStateResponse.kt",
                "**/at/aau/se2/cluedo/network/TurnBasedWebSocketService.kt",
                "**/at/aau/se2/cluedo/network/WebSocketService.kt",
                "**/at/aau/se2/cluedo/ui/screens/AccusationFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/EliminationScreenFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/EliminationUpdateFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/GameBoardFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/GameFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/InvestigationUpdateFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/JoinLobbyFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/LobbyFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/MainMenuFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/NotesFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/SettingsFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/SplashActivity.kt",
                "**/at/aau/se2/cluedo/ui/screens/SuggestionFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/WinScreenFragment.kt",
                "**/at/aau/se2/cluedo/ui/screens/EliminationUpdateFragment.kt",
                "**/at/aau/se2/cluedo/ui/MainActivity.kt",
                "**/at/aau/se2/cluedo/ui/ShakeEventListener.kt",
            ).joinToString(","))
    }
}