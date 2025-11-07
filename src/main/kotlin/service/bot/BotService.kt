package service.bot
import entity.*
import service.AbstractRefreshingService
import service.ConnectionState
import service.RootService
import kotlin.random.Random

/**bot service implements the bots and its own playing strategy  */
class BotService(private val rootService: RootService) : AbstractRefreshingService() {
    private var coordinateWildLifeToken: Coordinate? = null
    private var coordinateHabitatTile: Coordinate? = null
    private var indexHabitatWildLifeToken: Int= 0

    /**
     * Bot make an action in his turn.
     * @param currentPlayer current player which it is a bot
     */
    fun makeAIMove(currentPlayer: Player) {
        when (currentPlayer.playerType) {PlayerType.BOTEZ -> { val game = rootService.game
            checkNotNull(game) { "No game found" }
                drawRandomPair(game.selectPairs, currentPlayer) // choose between the 4 pairs randomly
                // The bot places the HabitatTile and Wildlife Token randomly
                makeRandomMove(currentPlayer)
                onAllRefreshables { refreshAfterBotPlayer() }}
            PlayerType.BOTHARD -> { val game = rootService.game
                checkNotNull(game) { "No game found" }
                drawRandomPair(game.selectPairs, currentPlayer)
                val validPositionsHabitatTile = getAllValidLocationsForHabitatTile(currentPlayer)
                val validPositionsWildlifeToken = getAllValidLocationsForWildlifeToken(currentPlayer)
                if (validPositionsWildlifeToken.isEmpty()) {
                    //game.wildlifeTokens.add(currentPlayer.chosenWildlifeToken.first())
                    val bestPosition = findBestHabitatTilePosition(currentPlayer, validPositionsHabitatTile)
                    if (bestPosition != null) { currentPlayer.grid[bestPosition] =
                        currentPlayer.chosenHabitatTile.first()
                        game.wildlifeTokens.add(currentPlayer.chosenWildlifeToken.first())
                        if (rootService.isNetworkGame() && rootService.networkService.connectionState==
                            ConnectionState.PLAYING_MY_TURN){ rootService.networkService.sendPlacedMessage(
                            indexHabitatWildLifeToken, coordinateHabitatTile!!, indexHabitatWildLifeToken,
                            null, currentPlayer.usedNatureToken, 0, game.wildlifeTokens) }
                        currentPlayer.chosenWildlifeToken.clear()
                        currentPlayer.chosenHabitatTile.clear()
                        onAllRefreshables { refreshAfterBotPlayHabitatTile() } } } else {
                    /**
                     * all possible combinations of Habitat Tile positions and Wildlife Token positions will be  found
                     * and stored in validMoves
                     */
                    val validMoves = mutableListOf<Pair<Coordinate, Coordinate>>()
                    for (habitatPosition in validPositionsHabitatTile) { for (wildlifePosition in
                    validPositionsWildlifeToken) { validMoves.add(habitatPosition to wildlifePosition) } }
                    /**
                     * of all valid moves the best move is selected based on the rating
                     *
                     */
                    var bestMove: Pair<Coordinate, Coordinate>? = null
                    var bestScore = Int.MIN_VALUE
                    for (move in validMoves) { currentPlayer.grid[move.first] = currentPlayer.chosenHabitatTile.first()
                        currentPlayer.grid[move.second]?.wildlifeToken = currentPlayer.chosenWildlifeToken.first()
                        rootService.scoringCardService.calculateScoreForAllPlayers()
                        if (currentPlayer.score > bestScore) { bestScore = currentPlayer.score
                            bestMove = move}
                        currentPlayer.grid.remove(move.first)
                        currentPlayer.grid[move.second]?.wildlifeToken = null
                        currentPlayer.score = 0 }
                    if (bestMove != null) { val (habitatPosition, wildlifePosition) = bestMove
                        coordinateHabitatTile = bestMove.first
                        coordinateWildLifeToken = bestMove.second
                        val habitatTile = currentPlayer.chosenHabitatTile.first()
                        val wildlifeToken = currentPlayer.chosenWildlifeToken.first()
                        currentPlayer.grid[Pair(habitatPosition.first, habitatPosition.second)] = habitatTile
                        onAllRefreshables { refreshAfterBotPlayHabitatTile() }
                        currentPlayer.chosenWildlifeToken.clear()
                        val habitatTileAtPosition = currentPlayer.grid[wildlifePosition]
                        if (habitatTileAtPosition != null) {
                            habitatTileAtPosition.wildlifeToken = wildlifeToken
                            onAllRefreshables { refreshAfterBotPlayWildlifeToken() }
                            if (rootService.isNetworkGame() &&
                                rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN) {
                                rootService.networkService.sendPlacedMessage(indexHabitatWildLifeToken,
                                    coordinateHabitatTile!!, indexHabitatWildLifeToken, coordinateWildLifeToken,
                                    false, 0, game.wildlifeTokens)}
                            currentPlayer.chosenHabitatTile.clear() } } else {
                        throw IllegalArgumentException("BOTHARD-Bot konnte keinen gÃ¼ltigen Zug finden.") } }
                onAllRefreshables { refreshAfterBotPlayer() } }else -> {
                    throw IllegalArgumentException("player is not a bot ") } } }
    /**
     * get every position where the bot can do a move.
     *
     * @param currentPlayer current player
     *
     * @return List<Coordinate>
     */
    fun getAllValidLocationsForWildlifeToken(currentPlayer: Player): List<Coordinate> {

        val currentToken = currentPlayer.chosenWildlifeToken.first()
        /** the parameter must be not null so the method can search in the grid if the actual can be placed somewhere*/
        requireNotNull(currentPlayer.chosenWildlifeToken)

        val currentGame = rootService.game
        val currentBoard = currentPlayer.grid
        val listOfCoordinates = mutableListOf<Coordinate>()

        /**the game must exist  */
        requireNotNull(currentGame) { "current game is empty" }

        val wildLifeTokensBag = currentGame.wildlifeTokens

        /**the board must have at least one Habitat*/
        require(currentBoard.isNotEmpty()) { "the Board is Empty" }

        /**iteration between every single coordinate in the grid. not sure if it works*/
        for ((coordinates, _) in currentBoard) {

            /** after testing it we must also test if this Habitat in these coordinates
             * accept the current wildLifeToken **/
            if (currentBoard[coordinates]?.animals?.contains(currentToken.animal) == true) {

                if (currentBoard[coordinates]?.wildlifeToken == null) {
                    /** adding the coordinates to the List*/
                    coordinates.first?.let {
                        coordinates.second?.let { it1 -> Coordinate(it, it1) }
                    }?.let {
                        listOfCoordinates.add(it)
                    }
                }
            }
        }
        /** if there is no valid Coordinate to put the current Wild life token,
         * it will be placed in the wildLifeTokensBag
         * */
        if (listOfCoordinates.size == 0) {
            wildLifeTokensBag.add(currentToken)
        }
        return listOfCoordinates
    }

    /**
     * get every position where the bot can do a move.
     *
     * @param player current player
     *
     * @return List<Coordinate>
     */
    fun getAllValidLocationsForHabitatTile(player: Player): List<Coordinate> {

        val validLocations = mutableListOf<Coordinate>()

        for ((coord, _) in player.grid) {
            val neighbors = getHexNeighbors(coord.first!!, coord.second!!)
            for (neighbor in neighbors) {
                if (neighborsInGrid(neighbor)) {
                    if (!player.grid.containsKey(neighbor)) {
                        validLocations.add(neighbor)
                    }
                }
            }
        }
        val validLocationsDistinct = validLocations.distinct()
        return validLocationsDistinct
    }

    private fun neighborsInGrid(coordinate: Coordinate): Boolean {
        return !((coordinate.first == 1 && coordinate.second == 3) ||
                (coordinate.first == 2 && coordinate.second == 2) ||
                (coordinate.first == 3 && coordinate.second == 1) ||
                (coordinate.first == -3 && coordinate.second == -1) ||
                (coordinate.first == 2 && coordinate.second == -2) ||
                (coordinate.first == -1 && coordinate.second == -3) ||
                (coordinate.first!! > 3 || coordinate.second!! > 3) ||
                coordinate.first!! < -3 || coordinate.second!! < -3)
    }

    /**
     * getHexNeighbors returns  all the 6 neighbors for a given hexagon
     * @param q and r-coordinate of the current hexagon
     * @return A list of coordinates representing the positions of the neighboring hexagons
     */
    private fun getHexNeighbors(q: Int, r: Int): List<Coordinate> {
        val neighbors = mutableListOf<Coordinate>()
        val neighborList = listOf(
            Pair(0, -1),
            Pair(1, -1),
            Pair(1, 0),
            Pair(0, 1),
            Pair(-1, 1),
            Pair(-1, 0),

            )
        for ((q1, r1) in neighborList) {

            neighbors.add(entity.Coordinate(q + q1, r + r1))
        }
        return neighbors
    }


    /**
     * Bot has many options and can choose one of them randomly.
     * @param player current player
     */

    fun makeRandomMove(player: Player) {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        val habitatTile = player.chosenHabitatTile.first()
        val wildlifeToken = player.chosenWildlifeToken.first()

        val validPositionsHabitatTile = getAllValidLocationsForHabitatTile(player)

        if (validPositionsHabitatTile.isNotEmpty()) {
            val randomPositionHabitatTile = validPositionsHabitatTile.random()
            player.grid[randomPositionHabitatTile] = habitatTile
            coordinateHabitatTile = randomPositionHabitatTile
            onAllRefreshables { refreshAfterBotPlayHabitatTile() }


        } else {
            throw IllegalArgumentException("No valid positions available for the HabitatTile.")
        }

        val validPositionsWildlifeToken = getAllValidLocationsForWildlifeToken(player)

        if (validPositionsWildlifeToken.isEmpty()) {

            game.wildlifeTokens.add(wildlifeToken)
            // sendplaceMessage discard token // todo
            if (rootService.isNetworkGame() && rootService.networkService.connectionState==ConnectionState.PLAYING_MY_TURN){
                rootService.networkService.sendPlacedMessage(indexHabitatWildLifeToken,
                    coordinateHabitatTile!!,
                    indexHabitatWildLifeToken,
                    null,
                    player.usedNatureToken,
                    0,
                    game.wildlifeTokens)
            }
            player.chosenWildlifeToken.clear()
            player.chosenHabitatTile.clear()

        } else {

            val randomPositionWildlifeToken = validPositionsWildlifeToken.random()
            coordinateWildLifeToken = randomPositionWildlifeToken
            val habitatTileAtPosition = player.grid[randomPositionWildlifeToken]
            if (habitatTileAtPosition != null) {
                habitatTileAtPosition.wildlifeToken = wildlifeToken

                // clear ChosenHabitatTile.
                player.chosenWildlifeToken.clear()

                if (rootService.isNetworkGame() &&
                    rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN) {
                    rootService.networkService.sendPlacedMessage(
                        indexHabitatWildLifeToken,
                        coordinateHabitatTile!!,
                        indexHabitatWildLifeToken,
                        coordinateWildLifeToken,
                        false,
                        0,
                        game.wildlifeTokens
                    )
                }
                // clear ChosenHabitatTile.
                player.chosenHabitatTile.clear()

                onAllRefreshables { refreshAfterBotPlayWildlifeToken() }
            }
        }
    }


    /** drawRandomPair choose randomly ein pair from the four centered pairs
     * @param centeredTokensAndHabitats the pair
     * @param currentPlayer is the current player
     */
    fun drawRandomPair(

        centeredTokensAndHabitats: MutableList<Pair<HabitatTile, WildlifeToken>>,
        currentPlayer: Player
    ) {
        val game = rootService.game
// Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        if (centeredTokensAndHabitats.isEmpty()) {
            throw IllegalArgumentException("Player muss ein Pair auswÃ¤hlen")
        }

        val randomIndex = Random.nextInt(centeredTokensAndHabitats.size)

        indexHabitatWildLifeToken= randomIndex

        val randomPair = centeredTokensAndHabitats[randomIndex]

        currentPlayer.chosenHabitatTile.add(randomPair.first)
        currentPlayer.chosenWildlifeToken.add(randomPair.second)

        game.selectPairs.remove(randomPair)

        game.selectPairs.add(game.habitatTiles.removeLast() to game.wildlifeTokens.removeLast())

        onAllRefreshables { refreshAfterBotDrawPair() }


    }

    /**
     * the method give back the best possible position for habitat tile with the best score
     * @param currentPlayer the current player
     * @param validPositions list of all valid positions
     * @return Coordinate
     */
    private fun findBestHabitatTilePosition(currentPlayer: Player, validPositions: List<Coordinate>): Coordinate? {
        var bestPosition: Coordinate? = null
        var bestScore = Int.MIN_VALUE

        for (position in validPositions) {

            currentPlayer.grid[position] = currentPlayer.chosenHabitatTile.first()

            rootService.scoringCardService.calculateScoreForAllPlayers()

            val score = currentPlayer.score
            if (score >= bestScore) {
                bestScore = score
                bestPosition = position
            }

            currentPlayer.grid.remove(position)

            currentPlayer.score = 0
        }

        return bestPosition
    }
}
