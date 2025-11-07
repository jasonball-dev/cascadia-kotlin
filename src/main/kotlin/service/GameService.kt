package service

import entity.*

/** gameService class is responsible for the methods involving the game itself */

class GameService(private val rootService: RootService) : AbstractRefreshingService() {
    var game: CascadiaGame? = null

    /**
     * The method starts a new game with the specified players.
     * The method initializes all necessary resources and sets the game to a ready-to-start state.
     *
     * Preconditions
     * - The playerNames list must not be empty.
     * - Each player name in the list must be unique (no duplicates).
     * - The allowed number of players must be adhered to (1-4 players).
     * - There must be no ongoing game existing.
     *
     * Postconditions
     * - Each player receives 20 Habitats + 1 Habitat.
     * - A random player from the list is selected as the starting player.
     * - The table center is initially occupied with a StartHabitat.
     * - The DiscardStack is empty.
     *
     * @param playerNames List of player names.
     * @param playersType List of player types (e.g. Human or Bot)
     * @return The method does not return any value (Unit).
     *
     * @throws IllegalStateException if a game already exists.
     * @throws IllegalArgumentException when the playerName list contains more than 4 players.
     * */

    fun startGame(
        playerNames: List<String>,
        playersType: List<PlayerType>,
        selectedScoringCards: List<Boolean> = listOf(true, true, true, true, true)
    ) {
        require(playerNames.size == playersType.size) { "Player names and types must have the same size." }
        require(playerNames.size in 2..4) { "The number of players must be between 2 and 4." }
        require(selectedScoringCards.size == 5) { "5 scoring cards must be selected." }

        // Check for duplicate names
        require(playerNames.size == playerNames.distinct().size) { "Player names must be unique." }

        // Create players (Also, Spieler erstellen und die Namem mit den Typen kombinieren)
        val currentPlayers = playerNames.zip(playersType).map { (name, type) ->
            Player(name, type)
        }.toTypedArray()

        val game = CascadiaGame(currentPlayers) // Initialize the game
        rootService.game = game

        // Pick scoring cards
        game.scoringCards.add(ScoringCard(selectedScoringCards[0], Animal.BEAR))
        game.scoringCards.add(ScoringCard(selectedScoringCards[1], Animal.ELK))
        game.scoringCards.add(ScoringCard(selectedScoringCards[2], Animal.FOX))
        game.scoringCards.add(ScoringCard(selectedScoringCards[3], Animal.HAWK))
        game.scoringCards.add(ScoringCard(selectedScoringCards[4], Animal.SALMON))

        rootService.habitatTileService.distributeInitialHabitats() // distribute starter habitat tiles for each player
        rootService.habitatTileService.placeInitialHabitats() // Assign starter habitat tiles for each player
        rootService.habitatTileService.createDrawStackHabitatTile()
        rootService.habitatTileService.createDrawStackWildlifeToken()
        rootService.habitatTileService.createPlayStack()//  4 Paare

        game.currentPlayer = 0 // Set first player

        onAllRefreshables { refreshAfterStartNewGame() }

        if (!rootService.isNetworkGame()) {
            startTurn()
        }
    }

    /**The method ends the ongoing game and calculates the final scores of the players.
     *
     * Preconditions:
     * - The game must be started and in a running state.
     *
     * Postconditions:
     * - The state of the game is set to "ended."
     * - The player's scores are calculated and displayed.
     * - Players' Habitat Tiles and Wildlife Tokens, as well as all stacks, are reset.
     *
     * @throws IllegalStateException if there is no current game.
     */
    fun endGame() {
        rootService.scoringCardService.calculateScoreForAllPlayers()
        onAllRefreshables { refreshAfterGameEnd() }
        rootService.game = null // there is no game
    }

    /**
     * The method begins the turn for the specified player and prepares all necessary game resources and states,
     * allowing the player to execute their turn, such as StartHabitatTile, HabitatTile, and WildlifeToken.
     *
     * Preconditions
     * - The game must be started (the startGame method has been successfully executed).
     * - No other player may currently be active in the turn, i.e., only one player is taking their turn.
     *
     * Postconditions
     * - The game status is updated to mark the player as "active."
     * - All temporary resources for the current player's turn have been initialized, e.g.,
     * StartHabitatTile, HabitatTile, and WildlifeToken stacks.
     *
     *
     *
     * @Throws IllegalStateException if the game is not started.
     * @Throws IllegalArgumentException if the player is invalid or if another player is currently active.
     */
    fun startTurn() {
        val game = rootService.game // Initialize the game
        checkNotNull(game) { "No active game to start a turn." }

        //require(game.players[game.currentPlayer] == player)
        // { "It's not the player's turn." } // checking if it is the player's turn

        removeOverPopulationAutomatic()
        val currentPlayer = game.players[game.currentPlayer]
        if (currentPlayer.playerType == PlayerType.BOTEZ || currentPlayer.playerType == PlayerType.BOTHARD) {
            rootService.botService.makeAIMove(currentPlayer)
        }


        onAllRefreshables { onAllRefreshables { refreshAfterStartTurn() }} //
    }

    /**
     * The method ends the turn of the specified player and updates the game status,
     * checks compliance with the rules, and prepares for the next player's turn.
     *
     *Preconditions
     * - The turn of the specified player must be active (the startTurn method has been called for this player).
     *
     *Postconditions
     * - The player is marked as "inactive" and the game status is updated for the next turn.
     * - The player's resources, such as chosen tiles or animal markers, are processed and saved.
     *
     * @throws IllegalStateException Thrown if the game is not started.
     * @throws IllegalArgumentException Thrown if the player is invalid.
     */
    fun endTurn() {
        val game = rootService.game // Initialize the game
        checkNotNull(game) { "No active game to end a turn." }

        if (game.habitatTiles.size == 0 || game.wildlifeTokens.size == 0) {
            rootService.gameService.endGame()
        }
        // checking if it is the player's turn
        //require(game.players[game.currentPlayer] == player) { "It's not the player's turn." }

        game.currentPlayer = (game.currentPlayer + 1) % game.players.size //
        // change to next player
        val currentPlayer = game.players[game.currentPlayer]

        if (!rootService.isNetworkGame() &&
            (currentPlayer.playerType == PlayerType.BOTEZ || currentPlayer.playerType == PlayerType.BOTHARD)) {
            rootService.botService.makeAIMove(currentPlayer)
        }

        currentPlayer.usedNatureToken = false

        currentPlayer.chosenWildlifeToken.clear()
        currentPlayer.chosenHabitatTile.clear()
        removeOverPopulationAutomatic()
        onAllRefreshables { refreshAfterEndTurn() } //
    }

    /**
     * the method checks if there is any overpopulation in the current turn, so it can be taken out
     */
    fun hasOverPopulationFour(): Boolean {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        return game.selectPairs.map { it.second }.distinct().size == 1
    }

    /**
     * in the situation which there is many wildlife tokens with the same animal.
     * This methode removes the tokens that have the same animal in it and bring it in discardStack.
     *
     * @return listOfPairs without overPopulation
     * */
    fun removeOverPopulationAutomatic() {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        if (hasOverPopulationFour()) {
            for (i in game.selectPairs.indices) {
                val oldTile = game.selectPairs[i].first
                val replaceToken = game.selectPairs[i].second
                rootService.habitatTileService.discardTokens.add(replaceToken)
                game.selectPairs[i] = Pair(oldTile, game.wildlifeTokens.removeFirst())
                //game.wildlifeTokens.removeFirst()
            }

            game.wildlifeTokens.addAll(rootService.habitatTileService.discardTokens)
            rootService.habitatTileService.discardTokens.clear()

            if (rootService.isNetworkGame()) { rootService.networkService.sendResolveOverpopulationMessage()}

            game.wildlifeTokens.shuffled()
            if (rootService.isNetworkGame()) { rootService.networkService.sendShuffleWildlifeTokensMessage()}
        }
    }

    /**
     * this method will check if there is any trio of equal animals in the select pairs structure.
     * it works as a helping method for remove overpopulation method
     */
    fun hasOverPopulationThree(): Boolean {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        //val typ1 = game.wildlifeTokens
        //val typ2 = game.wildlifeTokens
        //if (typ1.size != typ2.size)

        val tokenCount = mutableMapOf<WildlifeToken, Int>()
        for (pair in game.selectPairs) {
            val token = pair.second
            tokenCount[token] = tokenCount.getOrDefault(token, 0) + 1
        }
        return tokenCount.values.any { it == 3 }
    }

    /**
     * this method removes the overpopulation for the case with 3 equal animals.
     * Those animals will be removed from the select pair structure and be placed into discard tokens.
     * New tokens from wild life token bag will replace the old ones
     */
    fun removeOverPopulationThree() {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        for (i in game.selectPairs.indices) {
            val oldTile = game.selectPairs[i].first
            val replaceToken = game.selectPairs[i].second
            rootService.habitatTileService.discardTokens.add(replaceToken)
            game.selectPairs[i] = Pair(oldTile, game.wildlifeTokens.removeFirst())
            //game.wildlifeTokens.removeFirst()
        }
        game.wildlifeTokens.addAll(rootService.habitatTileService.discardTokens)
        rootService.habitatTileService.discardTokens.clear()
        if (rootService.isNetworkGame()) {rootService.networkService.sendResolveOverpopulationMessage()}

        game.wildlifeTokens.shuffled()
        if (rootService.isNetworkGame()) {rootService.networkService.sendShuffleWildlifeTokensMessage()}

        onAllRefreshables {
            refreshAfterRemoveOverpopulation()
        }
    }

    /** this method selects the player that will be playing first in the first round
     *
     * @param isRandomOrder is an input during the configuration of the game so initial player can also be randomized
     *
     * @return it returns the player which will be the first one to play
     * */
    fun selectStartingPlayer(isRandomOrder: Boolean): Player {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        require(game.players.isNotEmpty()) { "Player array must not be empty." }
        require(game.players.size in 2..4) { "A maximum of 4 players are allowed." }

        setOrder(isRandomOrder)
        return game.players[0]
    }

    // erstelle neue Reihenfolge
    private fun setOrder(isRandomOrder: Boolean): List<Player> {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        val players = game.players
        check(players.isNotEmpty()) { "there are no players!" }

        return if (isRandomOrder) {
            players.toMutableList().shuffled() // Random order of players
        } else {
            players.toMutableList() // Maintaining the current order
        }
    }
}