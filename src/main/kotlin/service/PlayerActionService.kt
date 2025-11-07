package service

import edu.udo.cs.sopra.ntf.messages.SwappedWithNatureTokenMessage
import entity.*
import edu.udo.cs.sopra.ntf.entity.Animal

/**
 * This class is related to player actions, including playing cards, using nature tokens, and rotating tiles...
 *
 * @param rootService The root service to which this service belongs
 * */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {
    private var tileIndex:Int = 0
    private var tokenIndex:Int = 0
    private var currentTileCoordinate:Coordinate = Pair(0,0)

    /**
     * Pick habitatTile from playStack(4combination) to ChosenHabitatTile and ChosenWildlifeToken (player's hand).
     *
     * @param selectedPair a Pair of selected HabitatTile and WildlifeToken.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun pickOnePair(selectedPair: Pair<HabitatTile,WildlifeToken>) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        // find index of selectedPair.
        for (i in game.selectPairs.indices){
            if (game.selectPairs[i] == selectedPair){
                tileIndex=i
                tokenIndex=i
            }
        }

        val currentPlayer = game.players[game.currentPlayer]

        //Add two elements to the corresponding ChosenHabitatTile and ChosenWildlifeToken.
        currentPlayer.chosenHabitatTile.add(selectedPair.first)
        currentPlayer.chosenWildlifeToken.add(selectedPair.second)

        // If the drawStack is empty, then the player at moment is on their last turn.
        // and the game is over , after player plays this tile.
        if (game.habitatTiles.size==0){
            //If the drawStack is empty, just jump.(not remove,not refill.)
            // therefore the selectPairs remains the same as before,
            // so the habitatTile("selectedPair.first") exists not only in "ChosenHabitatTile" but also in selectPairs,
            // and we can determine the lastTurn from this situation (see end of "playHabitatTile").
            println("This is last the Turn!")
        }else {
            // If it's not empty. Remove selectPair from this list “selectPairs”.
            game.selectPairs.remove(selectedPair)

            //Adding a new pair to this list “selectPairs”
            game.selectPairs.add(game.habitatTiles.removeLast() to game.wildlifeTokens.removeLast())
            //game.habitatTiles.removeFirst()
            //game.wildlifeTokens.removeFirst()
        }

        if(currentPlayer.playerType==PlayerType.BOTEZ||currentPlayer.playerType==PlayerType.BOTHARD){
            onAllRefreshables { refreshAfterBotDrawPair() }
        }

        onAllRefreshables { refreshAfterPickOnePair() }
    }

    /**
     * Allows the player to place a habitat tile from their hand onto their game board.
     * FIRST playHabitatTile, THEN playWildlifeToken !!!
     *
     * @param coordinate the coordinate, where habitat placed.
     *
     * @throws IllegalStateException if no game is currently active.
     * @throws IllegalStateException if the tile is already occupied.
     * @throws IllegalStateException if the coordinate has no neighbors.
     */
    fun playHabitatTile( coordinate: Coordinate) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val currentPlayer = game.getCurrentPlayer()

        // isPlaceEmpty
        if (currentPlayer.grid.containsKey(coordinate)) {
            throw IllegalStateException("The position $coordinate is already occupied.")
        }

        // If hasNoNeighbor throw IllegalArgumentException.
        if (rootService.habitatTileService.hasNoNeighbor(coordinate, currentPlayer.grid)) {
            throw IllegalStateException("The position $coordinate cannot be placed due to no neighbors.")
        }

        // Associate tile and coordinate to grid.
        currentPlayer.grid[coordinate] =
            currentPlayer.chosenHabitatTile.first()

        currentTileCoordinate=coordinate

        onAllRefreshables { refreshAfterPlayHabitatTile() }
    }



    /**
     * Allows the player to place a wildlife token from their hand onto a habitat tile on their game board.
     * FIRST playHabitatTile, THEN playWildlifeToken !!!
     *
     * @param coordinate the coordinate, where habitat placed.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun playWildlifeToken(coordinate: Coordinate) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val currentPlayer = game.getCurrentPlayer()

        val selectedToken = currentPlayer.chosenWildlifeToken.first()

        // Extract the corresponding tile from the grid.
        val habitatTile = currentPlayer.grid[coordinate]
 /*
        // If there are no tiles on the target coordinates, the token cannot be placed.
        if (!currentPlayer.grid.containsKey(coordinate)){
            throw IllegalStateException("The position $coordinate has no tile to place token.")}

        //if there is already a token on this tile.
        if (habitatTile!!.wildlifeToken!=null){
            throw IllegalStateException("There is already a token on this tile! ")
        }
*/

        // If there are no tiles on the target coordinates, the token cannot be placed.
        // and if there is already a token on this tile, the token cannot be placed.
        if (!currentPlayer.grid.containsKey(coordinate) || habitatTile!!.wildlifeToken != null) {
            throw IllegalStateException(
                if (!currentPlayer.grid.containsKey(coordinate)) {
                    "The position $coordinate has no tile to place token."
                } else {
                    "There is already a token on this tile at position $coordinate!"
                }
            )
        }


        //If animals on the tile contain the token's animal, place the token on this tile,
        // if not，throw IllegalArgumentException.
        if (habitatTile.animals.contains(selectedToken.animal)) {
            habitatTile.wildlifeToken = selectedToken
        }
        else{ throw IllegalStateException("This token can not be placed on this tile!") }

        // clear ChosenHabitatTile.
        currentPlayer.chosenWildlifeToken.clear()

        //If there is a NaturToken on the tile, then player gets a NaturToken.
        if (habitatTile.isKeystone){currentPlayer.natureToken +=1 }

        // call sendPlacedMessage in Network.
        if (rootService.isNetworkGame() && rootService.networkService.connectionState==ConnectionState.PLAYING_MY_TURN){
            rootService.networkService.sendPlacedMessage(tileIndex, currentTileCoordinate, tokenIndex, coordinate,
                currentPlayer.usedNatureToken, currentPlayer.chosenHabitatTile.first().rotation, game.wildlifeTokens)}

        //(see line 35).If there is a situation,
        // where there is the same habitatTile in "ChosenHabitatTile" and "selectPairs",
        // it means that this is the last turn,
        // and also the tile has already been played (because "ChosenHabitatTile" is empty), can directly EndGame().
        if ( game.selectPairs.any { it.first == currentPlayer.chosenHabitatTile.first()} ){
            currentPlayer.chosenHabitatTile.clear()
            rootService.gameService.endGame()
        }
        // clear ChosenHabitatTile.
        currentPlayer.chosenHabitatTile.clear()

        onAllRefreshables { refreshAfterPlayWildlifeToken() }
    }

    /**
     * Allows the player to use one of their nature tokens to perform a special action .
     * The player can choose a different habitat tile and wildlife token.
     *
     * @param habitatInPair one combination in 4 combinations.
     * @param tokenInPair another combination in 4 combinations.
     *
     * @throws IllegalStateException if no game is currently active.
     * @throws IllegalStateException if the player does not have any nature tokens
     */
    fun useNatureTokenFreeSelect(habitatInPair: Pair<HabitatTile,WildlifeToken>,
                                 tokenInPair: Pair<HabitatTile,WildlifeToken>) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val currentPlayer = game.getCurrentPlayer()

        //find index of habitatInPair.
        for (i in game.selectPairs.indices){
            if (game.selectPairs[i] == habitatInPair){
                tileIndex=i
            }
        }

        // find index of tokenInPair.
        for (i in game.selectPairs.indices){
            if (game.selectPairs[i] == tokenInPair){
                tokenIndex=i
            }
        }

        currentPlayer.natureToken -= 1
        //it means in this turn one natureToken used.
        currentPlayer.usedNatureToken=true

        //Add two elements to the corresponding ChosenHabitatTile and ChosenWildlifeToken.
        currentPlayer.chosenHabitatTile.add(habitatInPair.first)
        currentPlayer.chosenWildlifeToken.add(tokenInPair.second)

        // If the drawStack is empty, then the player at moment is on their last turn.
        if (game.habitatTiles.size==0){
            //If the drawStack is empty, just jump.(not remove,not refill.)
            // therefore the selectPairs remains the same as before,
            // so the habitatTile("selectedPair.first") exists not only in "ChosenHabitatTile" but also in selectPairs,
            // and we can determine the lastTurn from this situation (see end of "playHabitatTile").
            println("This is the last Turn!")
        }
        // If drawStack not empty, Automatic filling of vacancies after removal two elements.
        for (i in game.selectPairs.indices) {
            if (habitatInPair.first.id == game.selectPairs[i].first.id) {
                game.selectPairs[i] = game.selectPairs[i].copy(first = game.habitatTiles.removeLast())
            }
            if (tokenInPair.first.id == game.selectPairs[i].first.id) {
                game.selectPairs[i] = game.selectPairs[i].copy(second = game.wildlifeTokens.removeLast())
            }
        }
        onAllRefreshables { refreshAfterUseNatureTokenFreeSelect() }
    }

    /**
     *
     * Allows the player to use one of their nature tokens to perform a special action .
     * Any number of tokens can be replaced.
     *
     * @param replaceList the mutableList of pairs, that token in this pairs will be placed.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun useNatureTokenReplace(replaceList: MutableList<Pair<HabitatTile,WildlifeToken>>){
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val currentPlayer = game.getCurrentPlayer()

        currentPlayer.natureToken -= 1
        //it means in this turn one natureToken used.
        currentPlayer.usedNatureToken=true

        // Find out which tokens in the pair need to be replaced and replace those tokens.
        for (i in game.selectPairs.indices) {
            if (replaceList.contains(game.selectPairs[i])) {
                //First put the token into a discardStack that temporarily holds the discarded token.
                rootService.habitatTileService.discardTokens.add(game.selectPairs[i].second)
                game.selectPairs[i]= game.selectPairs[i].copy(second = game.wildlifeTokens.removeLast())
            }
        }
        // After ending the "for" loop, add discarded tokens collected in the discardStack back to the bag.
        game.wildlifeTokens.addAll(rootService.habitatTileService.discardTokens)
        rootService.habitatTileService.discardTokens.clear()

        game.wildlifeTokens.shuffled()


        if (rootService.isNetworkGame()) {
            rootService.networkService.sendShuffleWildlifeTokensMessage()
            rootService.networkService.sendSwapWithNatureTokenMessage(
                SwappedWithNatureTokenMessage(
                    swappedSelectedTokens = replaceList.map { game.selectPairs.indexOf(it) },
                    swappedWildlifeTokens = game.wildlifeTokens.map { it.toNtfAnimal() }
                )
            )
        }
        onAllRefreshables { refreshAfterUseNatureTokenReplace() }
    }

    private fun WildlifeToken.toNtfAnimal(): Animal {
        return Animal.values().first { it.name == this.animal.name }
    }

    /**
     * Players can choose to giving up play WildlifeToken and instead put it back in the bag “wildlifeTokens”.
     *
     * @param wildlifeToken is the token which the player has selected in the current turn
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun discardWildlifeToken(wildlifeToken: WildlifeToken) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }
        val currentPlayer = game.getCurrentPlayer()
        game.wildlifeTokens.add(wildlifeToken)

        game.wildlifeTokens.shuffled()
        if (rootService.isNetworkGame()) { rootService.networkService.sendShuffleWildlifeTokensMessage()}

        if (rootService.isNetworkGame() && rootService.networkService.connectionState==ConnectionState.PLAYING_MY_TURN){
            rootService.networkService.sendPlacedMessage(tileIndex, currentTileCoordinate, tokenIndex, null,
                currentPlayer.usedNatureToken, currentPlayer.chosenHabitatTile.first().rotation, game.wildlifeTokens) }
        onAllRefreshables { refreshAfterDiscardWildlifeToken() }
    }

    /**
     * this function will rotate the "habitatTile" 60° clockwise one time.
     *
     * Because “rotateHabitatTile” follows the action of “playHabitatTiles”,two inputs ("habitatTile" and "Coordinate")
     * of “playHabitatTiles” can be used directly in this method.
     *
     * @param coordinate the coordinate, where this habitat placed.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun rotateHabitatTile(coordinate: Coordinate){
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val currentPlayer = game.getCurrentPlayer()

        //if no tile on this coordinate,it will return null.
        val selectedTile = currentPlayer.grid[coordinate]
            ?: throw IllegalStateException("The position $coordinate has no tile to rotate.")

        //Put the last element of the array “types” into the first position of the array.
        val oneRotateArray = arrayOf(selectedTile.types.last()) + selectedTile.types.dropLast(1).toTypedArray()
        val rotatedHabitat = selectedTile.copy(types = oneRotateArray)
        rotatedHabitat.rotation += (selectedTile.rotation + 1) % 6
        currentPlayer.chosenHabitatTile.first().rotation = rotatedHabitat.rotation
        currentPlayer.grid[coordinate]= rotatedHabitat
        onAllRefreshables { refreshAfterRotateHabitatTile() }
    }
}