package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

/**  class of tests for the game service  */
class GameServiceTest {

    private val rootService = RootService()
    private val gameService = GameService(rootService)

    /** setting up the environment so the game service can be tested properly */
    @BeforeEach
    fun setUp() {
        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)

        gameService.startGame(playerNames, playersType)

        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)
        val h2= HabitatTile(2, arrayOf(
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE), mutableListOf(Animal.SALMON,Animal.HAWK), false)
        val h3= HabitatTile(3, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE), mutableListOf(Animal.ELK), true)
        val h4= HabitatTile(4, arrayOf(
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN), mutableListOf(Animal.FOX), false)
        val hList: MutableList<HabitatTile> = mutableListOf(h1, h2, h3, h4)

        val t1 = WildlifeToken(Animal.ELK)
        val t2 = WildlifeToken(Animal.HAWK)
        val t3 = WildlifeToken(Animal.SALMON)
        val t4 = WildlifeToken(Animal.FOX)
        val tList: MutableList<WildlifeToken> = mutableListOf(t1, t2, t3, t4)

        game.habitatTiles.addAll(hList)
        game.wildlifeTokens.addAll(tList)
    }

    /** game is null after initialization **/
    @Test
    fun cascadiaGameTest() {
        val rootService = RootService()
        assertNull(rootService.game, "The game should be null after initialization.")
    }

//startGame Tests
    /** startGame with valid input initializes game correctly **/
    @Test
    fun startGameCorrectly() {
        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)

        val game = rootService.game  // check if the game is initialized correctly
        assertNotNull(game)

        assertEquals(2, game.players.size, "The number of players should match the input.")

        game.players.forEachIndexed { index, player ->
            assertEquals(playerNames[index], player.name, "Player name at index $index should match.")
            assertEquals(playersType[index], player.playerType, "Player type at index $index should match.")
        }

        assertTrue(game.starterHabitatTiles.isNotEmpty(), "Starter habitat tiles should be distributed.")
        assertEquals(15, game.starterHabitatTiles.size, "There must be 15 starter habitats.")
        assertTrue(game.habitatTiles.isNotEmpty(), "Habitat tiles stack should be created.")
        assertTrue(game.wildlifeTokens.isNotEmpty(), "Wildlife tokens stack should be created.")
        assertEquals(0, game.currentPlayer, "The first player should be set as the current player.")
    }

    /** startGame throws exception if player names and types size mismatch **/
    @Test
    fun differentSize() {

        val playerNames = listOf("Lea", "Bob")
        val playersType = listOf(PlayerType.LOCAL)

        val exception = assertThrows<IllegalArgumentException> {
            gameService.startGame(playerNames, playersType)
        }
        assertEquals("Player names and types must have the same size.", exception.message)
    }

    /** startGame throws exception for invalid number of players **/
    @Test
    fun invalidNumberOfPlayers() {

        val playerNamesFew = listOf("Alice")
        val playersTypeFew = listOf(PlayerType.LOCAL)

        val exceptionFew = assertThrows<IllegalArgumentException> {
            gameService.startGame(playerNamesFew, playersTypeFew)
        }
        assertEquals("The number of players must be between 2 and 4.", exceptionFew.message)

        val playerNamesMany = listOf("Alice", "Bob", "Lea", "Dave", "Eve")
        val playersTypeMany = listOf(PlayerType.LOCAL, PlayerType.LOCAL,
            PlayerType.LOCAL, PlayerType.LOCAL, PlayerType.LOCAL)

        val exceptionMany = assertThrows<IllegalArgumentException> {
            gameService.startGame(playerNamesMany, playersTypeMany)
        }
        assertEquals("The number of players must be between 2 and 4.", exceptionMany.message)
    }

    /** startGame distributes habitat and wildlife tokens correctly **/
    @Test
    fun distributesCorrectly () {

        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)

        gameService.startGame(playerNames, playersType)
        val game = rootService.game

        assertNotNull(game, "Game should not be null.")
        assertTrue(game.starterHabitatTiles.isNotEmpty(), "Starter habitat tiles should be present.")
        assertTrue(game.habitatTiles.isNotEmpty(), "Habitat tiles should be present.")
        assertTrue(game.wildlifeTokens.isNotEmpty(), "Wildlife tokens should be present.")
    }

//endGame Tests
    /** after endGame set game to null again **/
    @Test
    fun setsGameToNull() {

        val playerNames = listOf("Lea", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK)
        rootService.gameService.startGame(playerNames, playersType)

        rootService.gameService.endGame()

        assertEquals(rootService.game, null)
        assertNull(rootService.game, "Game should be set to null after endGame is called.")
    }

//startTurn Tests

    /** No active game. **/
    @Test
    fun testStartTurn_NoActiveGame() {
        rootService.game = null

        val exception = assertThrows<IllegalStateException> {
            rootService.gameService.startTurn()
        }
        assertEquals("No active game to start a turn.", exception.message)
    }

    /** startTurn starts the turn correctly for the current player **/
    @Test
    fun startTurnTest() {

        val playerNames = listOf("Lea", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)
        gameService.startGame(playerNames, playersType)

        val game = rootService.game
        assertNotNull(game)

        //startTurn throws exception when called for the wrong player
        assertDoesNotThrow {
            gameService.startTurn()
        }

        rootService.game = null
        val exceptionNoGame = assertThrows<IllegalStateException> {
            gameService.startTurn()
        }
        assertEquals("No active game to start a turn.", exceptionNoGame.message)
    }

    /** startTurn throws exception when game is null **/
    @Test
    fun startTurnNoGame() {
        rootService.game = null
        val exception = assertThrows<IllegalStateException> {
            gameService.startTurn()
        }
        assertEquals("No active game to start a turn.", exception.message)
    }

//endTurn Tests
    /** endTurn throws exception when it's not the player's turn **/
    @Test
    fun endTurnTest() {
        val player1 = Player("Lea", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        rootService.gameService.startGame(listOf(player1.name, player2.name), listOf(player1.playerType, player2.playerType))

        val game = rootService.game
        assertNotNull(game)

        // check if the current player is correctly placed
        assertEquals(0, game.currentPlayer, "The initial current player should be 0 (Player 1).")

        // call endTurn() and check if the player changes
        gameService.endTurn()
        assertEquals(1, game.currentPlayer, "The current player index should be updated to Player 2.")

        // call endTurn() again and check if it switches back to Player 1
        gameService.endTurn()
        assertEquals(0, game.currentPlayer, "The current player index should loop back to Player 1.")

        // negative test cases: Exception in case of missing game
        rootService.game = null
        val exception = assertThrows<IllegalStateException> {
            gameService.endTurn()
        }
        assertEquals("No active game to end a turn.", exception.message)
    }

    /** endTurn throws exception when game is null **/
    @Test
    fun endTurnNoGame() {
        rootService.game = null
        val exception = assertThrows<IllegalStateException> {
            gameService.endTurn()
        }
        assertEquals("No active game to end a turn.", exception.message)
    }

//hasOverPopulationFour (through startTurn) Tests
    /** testing star turn with overpopulation */
    @Test
    fun testStartTurn_withOverPopulation() {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        game.selectPairs.clear()
        val h1 = HabitatTile(1, arrayOf(
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)
        val h2 = HabitatTile(2, arrayOf(
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        val h3 = HabitatTile(3, arrayOf(
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN), mutableListOf(Animal.ELK), false)
        val h4 = HabitatTile(4, arrayOf(
            HabitatType.RIVER,
            HabitatType.RIVER,
            HabitatType.RIVER,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        val token1 = WildlifeToken(Animal.ELK)
        val token2 = WildlifeToken(Animal.ELK)
        val token3 = WildlifeToken(Animal.ELK)
        val token4 = WildlifeToken(Animal.ELK)

        val token5 = WildlifeToken(Animal.SALMON)
        val token6 = WildlifeToken(Animal.FOX)
        val token7 = WildlifeToken(Animal.HAWK)
        val token8 = WildlifeToken(Animal.BEAR)
        game.selectPairs.addAll(
            listOf(
                Pair(h1, token1),
                Pair(h2, token2),
                Pair(h3, token3),
                Pair(h4, token4)
            )
        )

        game.wildlifeTokens.addAll(listOf(token5,token6,token7,token8))

        rootService.gameService.startTurn()

        // verification: Indirectly tests the removeOverPopulationAutomatic-logic
        assertTrue(rootService.habitatTileService.discardTokens.isEmpty(),
            "Discard-Stack sollte nach dem Mischen leer sein.")
        assertTrue(
            actual = game.selectPairs.map { it.second }.distinct().size>1,
            message = "Alle überpopulierten Tiere (ELK) sollten ersetzt worden sein."
        )
    }

//removeOverPopulationAutomatic tests
    /** testing the stat turn method */
    @Test
    fun startTurn_noGameTest() {
        rootService.game = null

        //check if the correct exception is thrown
        assertThrows<IllegalStateException> {
            gameService.startTurn()
        }
    }

// hasOverPopulationThree Tests
    /** testing if there is overpopulation without a game */
    @Test
    fun testHasOverPopulationThree_NoGameActive() {
        // Arrange: Reset the game to zero
        rootService.game = null

        //check if the exception is thrown correctly
        val exception = assertThrows<IllegalStateException> {
            rootService.gameService.hasOverPopulationThree()
        }

        //verify that the error message is correct
        assertEquals("No game is currently active", exception.message)
    }
    /** testing if there is  over population with 3 equal animal tokens */
    @Test
    fun testHasOverPopulationThree_True() {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        val token1 = WildlifeToken(Animal.ELK)
        val token2 = WildlifeToken(Animal.ELK)
        val token3 = WildlifeToken(Animal.ELK)
        val token4 = WildlifeToken(Animal.FOX)

        game.selectPairs.clear()
        game.selectPairs.addAll(
            listOf(
                Pair(game.habitatTiles[0], token1),
                Pair(game.habitatTiles[1], token2),
                Pair(game.habitatTiles[2], token3),
                Pair(game.habitatTiles[3], token4)
            )
        )

        //check if the method detects overpopulation
        val result = rootService.gameService.hasOverPopulationThree()

        //the result should be true because there are three identical animals
        assertTrue(result, "hasOverPopulationThree sollte true zurückgeben, " +
                "wenn drei gleiche Tiere existieren.")
    }
    /** testing if there is actual 3 equal animal tokens */
    @Test
    fun testTokenCountValuesMatchesThree_False() {
        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        val token1 = WildlifeToken(Animal.ELK)
        val token2 = WildlifeToken(Animal.ELK)
        val token3 = WildlifeToken(Animal.FOX)
        val token4 = WildlifeToken(Animal.SALMON)

        game.selectPairs.clear()
        game.selectPairs.addAll(
            listOf(
                Pair(game.habitatTiles[0], token1),
                Pair(game.habitatTiles[1], token2),
                Pair(game.habitatTiles[2], token3),
                Pair(game.habitatTiles[3], token4)
            )
        )

        //check if any { it == 3 } is recognized correctly
        val result = rootService.gameService.hasOverPopulationThree()

        //it should return false because there are only two identical animals
        assertFalse(result, "Die Bedingung any { it == 3 } sollte false sein," +
                " wenn weniger als drei gleiche Tiere vorhanden sind.")
    }

// removeOverPopulationThree
    /** No active game. **/
    @Test
    fun removeOverPopulationThree_noGameTest() {
        rootService.game = null
        val exception = assertThrows<IllegalStateException> {
            gameService.removeOverPopulationThree()
        }
        assertEquals("No game is currently active", exception.message)
    }

    /** selectPairs-Stack is empty **/
    @Test
    fun removeOverPopulationThree_empty() {
        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)
        gameService.startGame(playerNames, playersType)

        val game = rootService.game
        assertNotNull(game)

        game.selectPairs.clear() // Ensuring selectPairs is empty

        gameService.removeOverPopulationThree()

        assertTrue(game.selectPairs.isEmpty(), "selectPairs should remain empty.")
        assertTrue(rootService.habitatTileService.discardTokens.isEmpty(),
            "Discard tokens should remain empty.")
    }
    /** testing the over population with 3 equal wild life tokens */
    @Test
    fun testRemoveOverPopulationThreeLoop() {
        // Initialisierung des Spiels
        val player1 = Player("Alice", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val players = arrayOf(player1, player2)
        val game = CascadiaGame(players)
        rootService.game = game

        // Mock-Daten für selectPairs und wildlifeTokens
        val h1 = HabitatTile(1, arrayOf(
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.FOREST,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)
        val h2 = HabitatTile(2, arrayOf(
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE,
            HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        val wildlifeToken1 = WildlifeToken(Animal.HAWK)
        val wildlifeToken2 = WildlifeToken(Animal.FOX)

        game.selectPairs.add(Pair(h1, wildlifeToken1))
        game.selectPairs.add(Pair(h2, wildlifeToken2))
        game.wildlifeTokens.add(WildlifeToken(Animal.BEAR))
        game.wildlifeTokens.add(WildlifeToken(Animal.ELK))

        // Aufruf der Methode
        rootService.habitatTileService.discardTokens.clear()
        gameService.removeOverPopulationThree()

        // Überprüfung der Ergebnisse
        // Prüfe, ob Tokens aus selectPairs in discardTokens verschoben wurden
        assertEquals(0, rootService.habitatTileService.discardTokens.size)
        //assertTrue(rootService.habitatTileService.discardTokens.contains(wildlifeToken1))
        //assertTrue(rootService.habitatTileService.discardTokens.contains(wildlifeToken2))

        // Prüfe, ob selectPairs korrekt aktualisiert wurde
        assertEquals(2, game.selectPairs.size)
        assertEquals(h1, game.selectPairs[0].first)
        assertEquals(h2, game.selectPairs[1].first)
        assertNotEquals(wildlifeToken1, game.selectPairs[0].second)
        assertNotEquals(wildlifeToken2, game.selectPairs[1].second)

        // Prüfe, ob discardTokens nach der Schleife zurück zu wildlifeTokens hinzugefügt wurde
        //assertEquals(2, game.wildlifeTokens.size)
    }
/*

    @Test
    fun removeOverPopulationThree_emptyWildlifeTokensTest() {
        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)
        gameService.startGame(playerNames, playersType)

        val game = rootService.game
        assertNotNull(game)

        game.wildlifeTokens.clear() // Ensuring wildlifeTokens is empty

        assertDoesNotThrow {
            gameService.removeOverPopulationThree()
        }
        assertTrue(game.wildlifeTokens.isEmpty(), "WildlifeTokens should remain empty.")
    }

*/

//selectStartingPlayer tests
    /** testing the valid player with randomized move */
    @Test
    fun testValidPlayerArrayWithRandom() {
        val player1 = Player("Lea", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val player3 = Player("Charlie", PlayerType.LOCAL)


        val allPlayers = arrayOf(player1, player2, player3)

        val game = CascadiaGame(allPlayers)
        rootService.game = game
        val selectedPlayer = gameService.selectStartingPlayer(true)

        //check if the selected player is included in the array
        assertTrue(allPlayers.contains(selectedPlayer) )
        assertNotNull(rootService.game)
        //check if the selected player matches the current player in the game
        assertEquals(selectedPlayer, allPlayers[rootService.game?.currentPlayer!!])
    }
    /** testing the valid player*/
    @Test
    fun testValidPlayerArray() {
        val player1 = Player("Lea", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val player3 = Player("Charlie", PlayerType.LOCAL)


        val allPlayers = arrayOf(player1, player2, player3)

        val game = CascadiaGame(allPlayers)
        rootService.game = game
        val selectedPlayer = gameService.selectStartingPlayer(false)

        //check if the selected player is included in the array
        assertTrue(allPlayers.contains(selectedPlayer) )
        assertNotNull(rootService.game)
        //check if the selected player matches the current player in the game
        assertEquals(player1, allPlayers[rootService.game?.currentPlayer!!])
    }

/*

    @Test
    fun testEmptyPlayerArray() {
        val players = arrayOf<Player>()
*/
/*

        val exception = assertThrows<IllegalArgumentException> {
            gameService.selectStartingPlayer(false)
        }
*//*


        val game = CascadiaGame(players)
        rootService.game = game
        //assertEquals("Player array must not be empty.", exception.message)
        assertThrows<IllegalStateException> { rootService.gameService.selectStartingPlayer(false) }
    }
*/
/*
    @Test
    fun testLessThanTwoPlayers() {
        val player1 = Player("Lea", PlayerType.LOCAL)
        val players = arrayOf(player1)
        val exception = assertThrows<IllegalArgumentException> {
            gameService.selectStartingPlayer(false)
        }

        assertEquals("A maximum of 4 players are allowed.", exception.message)
    }

    @Test
    fun testMoreThanFourPlayers() {
        val player1 = Player("Lea", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val player3 = Player("Charlie", PlayerType.LOCAL)
        val player4 = Player("Dave", PlayerType.LOCAL)
        val player5 = Player("Eve", PlayerType.LOCAL)

        val players = arrayOf(player1, player2, player3, player4, player5)
        val exception = assertThrows<IllegalArgumentException> {
            gameService.selectStartingPlayer(true)
        }

        assertEquals("A maximum of 4 players are allowed.", exception.message)
    }
    */

/*

//setOrder tests
    @Test
    fun testSetOrderRandomOrder() {
        val player1 = Player("Alice", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val player3 = Player("Charlie", PlayerType.LOCAL)
        val player4 = Player("Dave", PlayerType.LOCAL)

        rootService.game = CascadiaGame(arrayOf(player1, player2, player3, player4))

        val randomOrder = gameService.setOrder(isRandomOrder = true) // Test random order

        // Ensure all players are present in the random order
        assertTrue(randomOrder.containsAll(listOf(player1, player2, player3, player4)))
        assertEquals(4, randomOrder.size, "The random order should include all players.")
    }

    @Test
    fun testSetOrderMaintainedOrder() {
        val player1 = Player("Alice", PlayerType.LOCAL)
        val player2 = Player("Bob", PlayerType.LOCAL)
        val player3 = Player("Charlie", PlayerType.LOCAL)
        val player4 = Player("Dave", PlayerType.LOCAL)

        rootService.game = CascadiaGame(arrayOf(player1, player2, player3, player4))

        // Test maintained order
        val maintainedOrder = gameService.setOrder(isRandomOrder = false)

        // Ensure the order is maintained
        assertEquals(listOf(player1, player2, player3, player4), maintainedOrder)
    }
*/
    /** testing in a situation which there is no game active  */
    @Test
    fun testNoGameActive() {
        rootService.game = null
        // Test when no game is active
        val exception = assertThrows<IllegalStateException> {
            gameService.startTurn()
        }
        assertEquals("No active game to start a turn.", exception.message)

        val exceptionEndTurn = assertThrows<IllegalStateException> {
            gameService.endTurn()
        }
        assertEquals("No active game to end a turn.", exceptionEndTurn.message)
        }
/*
    @Test
    fun testSetOrderNoPlayers() {
        // Set up a game with no players
        rootService.game = CascadiaGame(players = emptyArray(), currentPlayer = 0)

        // Test when there are no players
        val exception = assertThrows<IllegalStateException> {
            gameService.setOrder(isRandomOrder = false)
        }

        // Verify the exception message
        assertEquals("there are no players!", exception.message)
    }

*/

}