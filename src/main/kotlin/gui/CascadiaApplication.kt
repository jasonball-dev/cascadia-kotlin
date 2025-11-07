package gui

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * Implementation of the BGW [BoardGameApplication] for the game "Cascadia"
 */

class CascadiaApplication : BoardGameApplication("Cascadia Game"), Refreshable {
    private val rootService = RootService()

    val newGameScene = NewGameScene(rootService, this)
    val joinHostGameScene = JoinHostGameScene(rootService, this)
    val gameScene = GameScene(rootService, this)
    val lobbyScene = LobbyScene(rootService, this)
    val scoringCardSelectionScene = ScoringCardSelectionScene(rootService, this)
    val resultScene = ResultScene(rootService, this)
    val scoreboardScene = ScoreboardScene(rootService, this)

    init {
        loadFont("fonts/JacquesFrancoisShadow-Regular.ttf")
        loadFont("fonts/Jaini-Regular.ttf")
        loadFont("fonts/Itim-Regular.ttf")

        rootService.addRefreshables(this, newGameScene, joinHostGameScene, gameScene, lobbyScene)

        showMenuScene(newGameScene, 0)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene()
        showGameScene(gameScene)
    }

    override fun refreshAfterGameEnd() {
        println("in application")
        showMenuScene(resultScene)
    }
}



