package service

import edu.udo.cs.sopra.ntf.messages.*
import edu.udo.cs.sopra.ntf.entity.Animal
import edu.udo.cs.sopra.ntf.entity.ScoringCards
import entity.*

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games. Bridges between the [CascadiaNetWorkClient] and the other services.
 *
 * @property [rootService]
 */
class NetworkService(
    private val rootService: RootService
) : AbstractRefreshingService() {
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Cascadia"

        /** Network secret */
        const val NETWORK_SECRET = "cascadia24d"
    }

    /** Network client. Nullable for offline games. */
    var client: CascadiaNetWorkClient? = null
        private set

    /**
     * current state of the connection in a network game.
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Connects to server and creates a new game session.
     *
     * @param player Local host player. Name of the player can not be empty.
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     * */
    fun hostGame(player: Player) {
        if (!connect(NETWORK_SECRET, player)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.createGame(GAME_ID, "Welcome!")

        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to server and joins a game session as guest player.
     *
     * @param player Local player joining to the game. Name of the player can not be empty.
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(player: Player, sessionID: String) {
        if (!connect(NETWORK_SECRET, player)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, "Hello!")

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * set up the game using [GameService.startGame] and send the game init message
     * to the guest player. [connectionState] needs to be [ConnectionState.WAITING_FOR_GUEST].
     * This method should be called from the [CascadiaNetWorkClient] when the guest joined notification
     * arrived.
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_GUEST]
     * or client is null or player list in client is empty
     */
    fun startNewHostedGame() {
        check(connectionState == ConnectionState.WAITING_FOR_GUEST)
        { "currently not prepared to start a new hosted game." }

        check(client?.players?.size!! in 2..4)
        { "currently not prepared to start a new hosted game." }

        val playerNames = client?.players!!.map { it.name }
        val playerTypes = client?.players!!.map { it.playerType }

        rootService.gameService.startGame(playerNames, playerTypes)

        val game = rootService.game
        checkNotNull(game) { "game should not be null right after starting it." }

        val message = createGameInitMessage(game)

        client?.sendGameActionMessage(message)

        if (client?.player == game.players[game.currentPlayer]) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        if (connectionState == ConnectionState.PLAYING_MY_TURN) {
            rootService.gameService.startTurn()
        }

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    private fun createGameInitMessage(game: CascadiaGame): GameInitMessage {
        val gameRules: MutableMap<Animal, ScoringCards> = mutableMapOf()

        val habitatTiles = mutableListOf<Int>()
        habitatTiles.addAll(
            game.habitatTiles.map { it.id }
        )
        habitatTiles.addAll(
            game.selectPairs.reversed().map { it.first.id }
        )

        // Convert local scoring cards into network gameRules object
        game.scoringCards.forEach { card ->
            val anim = Animal.values().find { value -> value.name == card.animal.name }
            checkNotNull(anim) { "Mismatched animal enum in conversion to network layer: $card" }
            gameRules[anim] = if (card.isRuleA) ScoringCards.A else ScoringCards.B
        }

        val initWildlifeTokens = mutableListOf<Animal>()

        initWildlifeTokens.addAll(
            game.wildlifeTokens.mapNotNull { token ->
                Animal.values().find { value -> value.name == token.animal.name }
            }.toMutableList()
        )

        initWildlifeTokens.addAll(
            game.selectPairs.reversed().mapNotNull {
                Animal.values().find { value -> value.name == it.second.animal.name }
            }.toMutableList()
        )

        val startTiles: MutableList<Int> = mutableListOf()

        game.players.forEach {
            val tile = it.grid.values.toList()[0].id // Get first tile id from the player
            startTiles.add(tile.floorDiv(10)) // Floor Divide by 10, 11 -> 1, 22 -> 2 etc.
        }

        val message = GameInitMessage(
            habitatTileList = habitatTiles,
            playerList = game.players.map { it.name },
            gameRules = gameRules,
            /**
             * StartTiles Logic: List of max 4 ints between 1-5.
             * List<5,1,4,3> Represents the first player gets 50, 51, 52,
             * second player (2nd index) gets 10,11,12 etc.
             */
            startTiles = startTiles,
            initWildlifeTokens = initWildlifeTokens
        )

        return message
    }

    /**
     * Initializes the [CascadiaGame] from the [GameInitMessage] data.
     * [connectionState] needs to be [ConnectionState.WAITING_FOR_INIT].
     * This method should be called from the [CascadiaNetWorkClient] when the host sends the init message.
     *
     * @throws IllegalStateException if not currently waiting for an init message
     */
    fun startNewJoinedGame(message: GameInitMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not waiting for game init message. " }
        checkNotNull(client) { "Client can not be null when starting a joined game." }

        createGameFromGameInitMessage(message)
        val game = rootService.game

        checkNotNull(game)

        if (client?.player == game.players[game.currentPlayer]) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        if (connectionState == ConnectionState.PLAYING_MY_TURN) {
            rootService.gameService.startTurn()
        }

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    private fun createGameFromGameInitMessage(message: GameInitMessage) {
        val playerList = message.playerList.map { p ->
            if (p == client!!.playerName)
                client!!.player
            else
                Player(p, PlayerType.NETWORK)
        }.toTypedArray()

        val game = CascadiaGame(playerList)
        rootService.game = game

        val allTiles = rootService.habitatTileService.getAllHabitatTiles()

        val habitatTiles = mutableListOf<HabitatTile>()
        habitatTiles.addAll(
            message.habitatTileList.mapNotNull { tile ->
                allTiles.find { tile == it.id }
            }.toMutableList()
        )

        game.habitatTiles = habitatTiles

        game.scoringCards = message.gameRules.mapNotNull { rule ->
            entity.Animal.values().find { rule.key.name == it.name }?.let {
                ScoringCard(isRuleA = rule.value.name == "A", animal = it)
            }
        }.toMutableList()
        val defaultStartTiles = rootService.habitatTileService.getStarterHabitatTiles().chunked(3)

        val startTiles = message.startTiles.map { idx ->
            defaultStartTiles[idx - 1]
        }.toMutableList()

        game.wildlifeTokens = message.initWildlifeTokens.mapNotNull { token ->
            entity.Animal.values().find { anim ->
                anim.name == token.name
            }
        }.map { animal ->
            WildlifeToken(animal)
        }.toMutableList()

        rootService.habitatTileService.placeInitialHabitats(startTiles, false)
        rootService.habitatTileService.createPlayStack()

        onAllRefreshables {
            refreshAfterCreateDrawStackHabitatTile()
            refreshAfterCreateDrawStackWildlifeToken()
        }
    }


    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param secret Network secret. Must not be blank (i.e. empty or only whitespaces)
     * @param player Current local Player. Name of the player can not be blank.
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(secret: String, player: Player): Boolean {
        check(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(player.name.isNotBlank()) { "player name must be given" }

        val newClient =
            CascadiaNetWorkClient(
                player = player,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * send a [PlaceMessage] to the opponent
     *
     * @param placedTile Required if a tile is placed, index of the selected tile in [CascadiaGame.selectPairs]
     * @param tileCoord Required, axial coordinates of the tile
     * @param selectedToken Required if a token is placed, index of the selected token in [CascadiaGame.selectPairs]
     * @param tokenCoord Required if a token is placed, axial coordinates of the token
     * @param usedNatureToken If the player has used a nature token
     * @param rotation
     * Denotes how many times the tile was rotated clockwise, with 0 being the original orientation.
     * Ex. rotated 1 time -> Edge 5 is upper right. rotated 2 times -> Edge 4 is upper right.
     * 0 -> edge 0 is upper edge right
     * 1 -> edge 5 is upper edge right
     * 2 -> edge 4 is upper edge right
     * @param wildlifeTokens
     *
     * @throws IllegalStateException if it's not currently my turn.
     */
    fun sendPlacedMessage(
        placedTile: Int?,
        tileCoord: Coordinate,
        selectedToken: Int,
        tokenCoord: Coordinate?,
        usedNatureToken: Boolean,
        rotation: Int,
        wildlifeTokens: List<WildlifeToken>
    ) {
        check(connectionState == ConnectionState.PLAYING_MY_TURN) { "Not my turn." }
        require(tileCoord.first != null && tileCoord.second != null) { "Placed tile coordinates are null" }

        val message = PlaceMessage(
            placedTile = placedTile ?: 0,
            qcoordTile = tileCoord.first!!,
            rcoordTile = tileCoord.second!!,
            selectedToken = selectedToken,
            qcoordToken = tokenCoord?.first,
            rcoordToken = tokenCoord?.second,
            usedNatureToken = usedNatureToken,
            tileRotation = rotation,
            wildlifeTokens = wildlifeTokens.mapNotNull { token ->
                Animal.values().find { anim ->
                    token.animal.name == anim.name
                }
            },
        )

        client?.sendGameActionMessage(message)
        rootService.gameService.endTurn()
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
    }

    /**
     * Play the opponent's turn by handling the [PlaceMessage] sent through the server.
     *
     * @param message the message to handle
     *
     * @throws IllegalStateException if not currently expecting an opponent's turn
     */
    fun receivePlaceMessage(message: PlaceMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT)

        val tile = getTileFromArea(message.placedTile)
        val token = getTokenFromArea(message.selectedToken)
        if (tile == null || token == null) {
            error("Invalid tile or token index in PlaceMessage")
        }

        rootService.playerActionService.pickOnePair(Pair(tile, token))

        rootService.playerActionService.playHabitatTile(Coordinate(message.qcoordTile, message.rcoordTile))

        if (message.tileRotation != 0) {
            repeat(message.tileRotation) {
                rootService.playerActionService
                    .rotateHabitatTile(Coordinate(message.qcoordTile, message.rcoordTile))
            }
        }

        if (message.qcoordToken != null && message.rcoordToken != null) {
            rootService.playerActionService.playWildlifeToken(Coordinate(message.qcoordToken, message.rcoordToken))
        }

        if (message.usedNatureToken) {
            rootService.game?.getCurrentPlayer()?.natureToken = rootService.game?.getCurrentPlayer()?.natureToken!! - 1
        }

        // adjust the remaining drawable wildlife tokens
        // The new wildlife tokens in the message are the remaining ones
        rootService.game?.wildlifeTokens = message.wildlifeTokens.map { it.convertTokenFromNtf() }.toMutableList()

        rootService.gameService.endTurn()

        if (isMyTurn()) updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        else updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        rootService.gameService.startTurn()
    }

    private fun getTileFromArea(tileIndex: Int): HabitatTile? {
        checkNotNull(rootService.game)
        if (tileIndex < 0 || tileIndex >= rootService.game!!.selectPairs.size) return null
        return rootService.game!!.selectPairs[tileIndex].first
    }

    private fun Animal.convertTokenFromNtf(): WildlifeToken {
        return WildlifeToken(entity.Animal.valueOf(this.name))
    }

//    private fun Animal.getTokenFromNtf(): HabitatTile {
//        val game = rootService.game
//        checkNotNull(game)
//        val tile = game.habitatTiles.map {  }
//    }

    private fun getTokenFromArea(tokenIndex: Int): WildlifeToken? {
        checkNotNull(rootService.game)
        if (tokenIndex < 0 || tokenIndex >= rootService.game!!.selectPairs.size) return null
        return rootService.game!!.selectPairs[tokenIndex].second
    }

    private fun isMyTurn(): Boolean {
        val game = rootService.game
        checkNotNull(game)
        return client?.player?.name == game.getCurrentPlayer().name
    }

    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshables
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    /**
     * Removes a player from the game and the client.
     * Ends the game if only one player is left.
     * @param playerName Name of the player to remove.
     */
    fun removePlayer(playerName: String) {
        checkNotNull(client)
        client!!.players = client!!.players.filter {
            it.name != playerName
        }.toMutableList()

        if (client!!.players.isEmpty()) {
            disconnect()
        }

        rootService.game?.players = rootService.game?.players!!.filter {
            it.name != playerName
        }.toTypedArray()

        if (rootService.game?.players?.size == 1) {
            rootService.gameService.endGame()
        }
    }

    /**
     * Sends the network message for communicating about shuffling wildlife tokens
     * Message will be sent after the end of Overpopulation (Game Phase 1 of Game Rules)
     */
    fun sendShuffleWildlifeTokensMessage() {
        check(connectionState == ConnectionState.SWAPPING_WILDLIFE_TOKENS) { "Not my turn." }
        val game = rootService.game
        checkNotNull(game)

        val tokens = game.wildlifeTokens.mapNotNull { token ->
            Animal.values().find { anim ->
                token.animal.name == anim.name
            }
        }

        val message = ShuffleWildlifeTokensMessage(tokens)
        client?.sendGameActionMessage(message)

        // If overpopulation is resolved, set state to playing my turn
        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
    }

    /**
     * Sends the network message for communicating about resolving the overpopulation
     * Will be sent after every ResolveOverpopulation-Trial (of three or four)
     */
    fun sendResolveOverpopulationMessage() {
        check(connectionState == ConnectionState.PLAYING_MY_TURN) { "Not my turn." }
        val message = ResolveOverpopulationMessage()
        client?.sendGameActionMessage(message)
        updateConnectionState(ConnectionState.SWAPPING_WILDLIFE_TOKENS)
    }

    /**
     * Sends the network message for communicating about swapping by using a nature token
     * Will only be sent in second case (see game rules)
     * @param message SwappedWithNatureTokenMessage
     */
    fun sendSwapWithNatureTokenMessage(message: SwappedWithNatureTokenMessage) {
        check(connectionState == ConnectionState.PLAYING_MY_TURN) { "Not my turn." }
        client?.sendGameActionMessage(message)
    }

    /**
     * Receives the network message for swapping by using a nature token
     */
    fun receiveSwapWithNatureTokenMessage(message: SwappedWithNatureTokenMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT)
        { "Must be waiting for the opponent to receive a message." }

        val game = rootService.game
        checkNotNull(game)

        // Needs a List<MutablePair<HabitatTile, WildlifeToken>> to work
        // Convert message to the required format
        val swappedSelectedTokens = message.swappedSelectedTokens.map {
            game.selectPairs[it]
        }.toMutableList()
        rootService.playerActionService.useNatureTokenReplace(swappedSelectedTokens)

        // Updated list of wildlife tokens
        val swappedWildlifeTokens = message.swappedWildlifeTokens.map { it.convertTokenFromNtf() }

        game.wildlifeTokens = swappedWildlifeTokens.toMutableList()

        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
    }

    /**
     * Receives the network message for shuffling wildlife tokens
     */
    fun receiveShuffleWildlifeTokensMessage(message: ShuffleWildlifeTokensMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT)
        { "Must be waiting for the opponent to receive a message." }

        val game = rootService.game
        checkNotNull(game)

        game.wildlifeTokens = message.shuffledWildlifeTokens.map { it.convertTokenFromNtf() }.toMutableList()

        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
    }

    /**
     * Receives the network message for resolving the overpopulation
     */
    fun receiveResolveOverpopulationMessage() {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT)
        { "Must be waiting for the opponent to receive a message." }

        val game = rootService.game
        checkNotNull(game)

        if (rootService.gameService.hasOverPopulationThree()) {
            rootService.gameService.removeOverPopulationThree()
        } else if (rootService.gameService.hasOverPopulationFour()) {
            rootService.gameService.removeOverPopulationAutomatic()
        }

        updateConnectionState(ConnectionState.SWAPPING_WILDLIFE_TOKENS)
    }
}