package service

import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

/**
 * Class that provides tests for the [NetworkService]. It will connect to the
 * SoPra BGW-Net server (twice) and actually send messages through that server. This
 * might fail if the server is offline or [NetworkService.NETWORK_SECRET] is outdated.
 */
class NetworkServiceTest {
    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService

    /**
     * Initialize both connections and start the game, so that the players of both games
     * (represented by [rootServiceHost] and [rootServiceGuest]) are in their turns.
     *
     * Populates the game with some initial playable tiles and tokens.
     */
    @BeforeEach
    fun initConnections() {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame(Player("Test Host", PlayerType.NETWORK))
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        val sessionID = rootServiceHost.networkService.client?.sessionID
        assertNotNull(sessionID)

        rootServiceGuest.networkService.joinGame(Player("Test Guest", PlayerType.NETWORK), sessionID)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)
    }

    /**
     * Tests [NetworkService.hostGame], [NetworkService.joinGame]
     * and if the [ConnectionState] gets properly set for host / guest.
     */
    @Test
    fun testHostAndJoinGame() {
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        assertEquals(2, rootServiceHost.networkService.client?.players?.size)
        assertEquals(2, rootServiceGuest.networkService.client?.players?.size)
    }

    /**
     * Tests [NetworkService.disconnect] after the lobby has been hosted and a guest has joined.
     * State expected to be [ConnectionState.DISCONNECTED]
     * afterward and the [NetworkService.client]s must be null.
     */
    @Test
    fun testDisconnect() {
        rootServiceGuest.networkService.disconnect()
        rootServiceHost.networkService.disconnect()
        rootServiceGuest.waitForState(ConnectionState.DISCONNECTED)
        rootServiceHost.waitForState(ConnectionState.DISCONNECTED)

        assertNull(rootServiceHost.networkService.client)
        assertNull(rootServiceGuest.networkService.client)
    }

    /**
     * Tests [NetworkService.startNewHostedGame]
     */
    @Test
    fun testStartNewHostedGame() {
        rootServiceHost.networkService.startNewHostedGame()

        val hostGame = rootServiceHost.game

        assertNotNull(hostGame)

        if (rootServiceHost.networkService.client?.player == hostGame.players[hostGame.currentPlayer]) {
            rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        } else rootServiceHost.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        if (rootServiceGuest.networkService.client?.player == hostGame.players[hostGame.currentPlayer]) {
            rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)
        } else rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        val guestGame = rootServiceGuest.game
        assertNotNull(guestGame)

        assertEquals(hostGame.habitatTiles.size, guestGame.habitatTiles.size)
        hostGame.habitatTiles.forEachIndexed { index, tile ->
            assertEquals(tile.id, guestGame.habitatTiles[index].id)
        }
        hostGame.wildlifeTokens.forEachIndexed { index, token ->
            assertEquals(token.animal.name, guestGame.wildlifeTokens[index].animal.name)
        }

        assertEquals(hostGame.wildlifeTokens.size, guestGame.wildlifeTokens.size)

        // Test start tiles
        assertEquals(hostGame.getCurrentPlayer().grid.values.size, guestGame.getCurrentPlayer().grid.values.size)
        // Check all grid tiles are equal
        hostGame.players.forEachIndexed { idx, player, ->
            val guestPlayer = guestGame.players[idx]
            assertEquals(player.grid.size, guestGame.players[idx].grid.size)
            player.grid.forEach { (coord, tile) ->
                val guestTile = guestPlayer.grid[coord]
                assertEquals(tile.id, guestTile?.id)
                println("Tile $tile, GuestTile $guestTile")
                guestTile?.types?.forEach {
                    assertTrue(tile.types.contains(it))
                }
            }
        }
    }

    /**
     * Tests [NetworkService.startNewJoinedGame]
     */
    @Test
    fun testStartNewJoinedGame() {
        rootServiceHost.networkService.startNewHostedGame() // This calls startNewJoinedGame

        val hostGame = rootServiceHost.game
        assertNotNull(hostGame)

        if (rootServiceGuest.networkService.client?.player == hostGame.players[hostGame.currentPlayer]) {
            rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)
        } else rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        val guestGame = rootServiceGuest.game
        assertNotNull(guestGame)
    }

    /**
     * busy waiting for the game represented by this [RootService] to reach the desired network [state].
     * Polls the desired state every 100 ms until the [timeout] is reached.
     *
     * This is a simplification hack for testing purposes, so that tests can be linearized on
     * a single thread.
     *
     * @param state the desired network state to reach
     * @param timeout maximum milliseconds to wait (default: 5000)
     *
     * @throws IllegalStateException if desired state is not reached within the [timeout]
     */
    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 5000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connectionState == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }
}