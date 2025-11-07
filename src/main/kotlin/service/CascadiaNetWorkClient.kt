package service

import edu.udo.cs.sopra.ntf.messages.*
import entity.Player
import entity.PlayerType
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.*

/** this class is responsible for the Cascadia NetWork Client */

class CascadiaNetWorkClient(
    val networkService: NetworkService,
    val player: Player,
    host: String,
    secret: String,
) : BoardGameClient(
    playerName = player.name,
    host = host,
    secret = secret,
    networkLoggingBehavior = NetworkLogging.VERBOSE
) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /**
     * Used to create a game object from the players in the list after joining / hosting a game
     * by [NetworkService.startNewJoinedGame] and [NetworkService.startNewHostedGame] respectively.
     */
    var players: MutableList<Player> = mutableListOf()

    /**
     * Add the initial player that is creating the client to the players list.
     */
    init {
        players.add(player)
    }

    /**
     * @param response
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Cascadia game, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    sessionID = response.sessionID
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                }

                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * @param response
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Cascadia, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    // Add opponents to player list
                    players = response.opponents.map { Player(it, PlayerType.NETWORK) }.toMutableList()
                    players.add(player) // Add the joining player itself
                    sessionID = response.sessionID
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                }

                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * @param notification
     * Handle a [PlayerJoinedNotification] sent by the server.
     *
     * @throws IllegalStateException if not currently expecting any guests to join.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            //check(networkService.connectionState == ConnectionState.WAITING_FOR_GUEST)
            { "not awaiting any guests." }

            players.add(Player(notification.sender, PlayerType.NETWORK))

            if (networkService.connectionState == ConnectionState.WAITING_FOR_GUEST)
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST) // Required to update the GUI
            else
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
        }
    }

    /**
     * @param notification
     * Handle a [PlayerLeftNotification] sent by the server.
     *
     * @throws IllegalStateException if not currently expecting any guests to leave.
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        BoardGameApplication.runOnGUIThread {
           // check(networkService.connectionState == ConnectionState.WAITING_FOR_GUEST)
            { "not awaiting any guests." }

            networkService.removePlayer(notification.sender)

            if (networkService.connectionState == ConnectionState.WAITING_FOR_GUEST)
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST) // Required to update the GUI
            else
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
        }
    }

    /**
     * handle a [GameInitMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: GameInitMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.startNewJoinedGame(message)
        }
    }

    /**
     * handle a [PlaceMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onPlaceReceived(message: PlaceMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receivePlaceMessage(message)
        }
    }

    /**
     * handle a [ShuffleWildlifeTokensMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onShuffleWildlifeToken(message: ShuffleWildlifeTokensMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveShuffleWildlifeTokensMessage(message)
        }
    }

    /**
     * handle a [ShuffleWildlifeTokensMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onResolveOverpopulation(message: ResolveOverpopulationMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveResolveOverpopulationMessage()
        }
    }

    /**
     * Disconnect from the server and error with the given message.
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        networkService.client?.players = mutableListOf()
        error(message)
    }
}