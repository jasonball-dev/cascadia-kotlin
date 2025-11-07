package service

import entity.CascadiaGame
import gui.Refreshable
import service.bot.BotService


/**
 * This base service class provides access to all other services.
 *
 * @property gameService The connected [GameService]
 * @property playerActionService The connected [PlayerActionService]
 * @property cardService The connected [CardService]
 * @property currentGame The currently active [entity.CascadiaGame]. Can be `null`, if no game has started yet.
 */
class RootService {
    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    val botService = BotService(this)
    val habitatTileService = HabitatTileService(this)
    val networkService = NetworkService(this)
    val scoringCardService = ScoringCardService(this)
    /**
     * The currently active game. Can be `null`, if no game has started yet.
     */
    var game : CascadiaGame ?=null

    /**
     * Adds the provided [newRefreshable] to all services connected to this root service
     *
     * @param newRefreshable The [Refreshable] to be added
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        botService.addRefreshable(newRefreshable)
        habitatTileService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshable : Refreshable) {
        newRefreshable.forEach {addRefreshable(it)}
    }

    /**
     * Helper function to check if the game is a network game.
     */
    fun isNetworkGame() = networkService.connectionState != ConnectionState.DISCONNECTED
}