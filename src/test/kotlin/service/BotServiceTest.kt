package service

import entity.*
import service.bot.BotService
import kotlin.test.*

/**  class of tests for the bot service */
class BotServiceTest {

    /** testing draw random pair method */
    @Test
    fun testDrawRandomPair() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")

        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)

        gameService.startGame(playerNames, playersType)

        requireNotNull(rootService.game)
        val currentPlayer = rootService.game!!.players[rootService.game!!.currentPlayer]
        rootService.game!!.selectPairs.clear()
        val habitatTile1 = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.BEAR), isKeystone = false, id = 1)

        val habitatTile2 = HabitatTile(
            types = arrayOf(
                HabitatType.RIVER, HabitatType.RIVER, HabitatType.RIVER, HabitatType.RIVER,
                HabitatType.RIVER,
                HabitatType.RIVER), animals = mutableListOf(Animal.ELK), isKeystone = false, id = 1)

        val habitatTile3 = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.FOX), isKeystone = true, id = 1)
        val habitatTile4 = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.BEAR), isKeystone = true, id = 1
        )
        val wildlifeToken1 = WildlifeToken(Animal.BEAR)

        val wildlifeToken2 = WildlifeToken(Animal.FOX)

        val wildlifeToken3 = WildlifeToken(Animal.HAWK)

        val wildlifeToken4 = WildlifeToken(Animal.ELK)

        rootService.game!!.selectPairs = mutableListOf(
            Pair(habitatTile1, wildlifeToken1),
            Pair(habitatTile2, wildlifeToken2), Pair(habitatTile3, wildlifeToken3), Pair(habitatTile4, wildlifeToken4)
        )
        assertTrue { currentPlayer.chosenHabitatTile.isEmpty() }
        assertTrue { currentPlayer.chosenWildlifeToken.isEmpty() }
        botService.drawRandomPair(rootService.game!!.selectPairs, currentPlayer)
        assertFalse(currentPlayer.chosenHabitatTile.isEmpty())
        assertEquals(1, currentPlayer.chosenHabitatTile.size)
        assertFalse(currentPlayer.chosenWildlifeToken.isEmpty())
    }

    /** testing make random move for bot ez  */
    @Test
    fun testMakeRandomMove() {
        val rootService = RootService()
        val botService = BotService(rootService)
        val currentPlayer = Player(name = "BotPlayer1", playerType = PlayerType.BOTEZ)
        val player2 = Player(name = "BotPlayer2", playerType = PlayerType.BOTEZ)
        val players = arrayOf(currentPlayer, player2)
        val habitatTile = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST
            ), animals = mutableListOf(Animal.ELK), isKeystone = false, id = 1
        )
        val habitatTile3 = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST
            ), animals = mutableListOf(Animal.SALMON), isKeystone = false, id = 3
        )
        val habitatTile4 = HabitatTile(
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST
            ), animals = mutableListOf(Animal.BEAR), isKeystone = true, id = 2
        )
        val wildlifeToken = WildlifeToken(Animal.ELK)
        currentPlayer.grid[Coordinate(0, 0)] = habitatTile3
        currentPlayer.grid[Coordinate(0, 1)] = habitatTile4
        rootService.game = CascadiaGame(players, 0)
        currentPlayer.chosenHabitatTile.add(habitatTile)
        currentPlayer.chosenWildlifeToken.add(wildlifeToken)
        botService.makeRandomMove(currentPlayer)

        val hasHabitatTile = currentPlayer.grid.values.contains(habitatTile)
        val hasWildlifeToken = currentPlayer.grid.values.any { it.wildlifeToken == wildlifeToken }
        assertTrue(hasHabitatTile, "The habitat tile should be placed in the grid.")
        assertTrue(hasWildlifeToken, "The wildlife token should be placed on the grid.")
    }

    /** testing get all valid locations for wil life token method */
    @Test
    fun getAllValidLocationsWildLifeTokenTest() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)

        gameService.startGame(playerNames, playersType)// initializing the game

        val game = rootService.game//putting game in a value

        val currentPlayer = game?.players?.get(0) // choosing the player which the method will work with

        requireNotNull(currentPlayer)// current player cant be empty

        currentPlayer.grid.clear() // taking out the initial tiles

        val salmon = WildlifeToken(Animal.SALMON)// creating the animal Token

        currentPlayer.chosenWildlifeToken.add(salmon) //putting the animal in players hand

        // testing if the salmon is now in current player chosen
        assertEquals(currentPlayer.chosenWildlifeToken.first().animal, Animal.SALMON)

        assertEquals(0, currentPlayer.grid.size) // testing the actual grid, it must be empty now

        val test1Tile = HabitatTile(// creating a Habitat Tile WITHOUT Salmon
            1,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.FOX, Animal.ELK, Animal.BEAR), false)

        val test2Tile = HabitatTile(// creating a Habitat Tile with salmon
            2,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)

        val test3Tile = HabitatTile( // creating a Habitat Tile with salmon
            3,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false

        )
        currentPlayer.grid[Coordinate(0, 0)] = test2Tile // putting the test1Tile into the grid

        assertEquals(1, currentPlayer.grid.size)// testing if the tile1 is now on the grid

        // calling the method and putting its output as a value
        val list = botService.getAllValidLocationsForWildlifeToken(currentPlayer)

        assertEquals(1, list.size)//testing if the size is now right

        currentPlayer.grid[Coordinate(0, 1)] = test1Tile // putting the tile2 on the grid

        // calling the method and putting its output as a value
        val list2 = botService.getAllValidLocationsForWildlifeToken(currentPlayer)

        assertEquals(2, currentPlayer.grid.size)// testing if the tile1 and tile 2 is now on the grid

        assertEquals(1, list2.size) // it must be also one

        currentPlayer.grid[Coordinate(1, 0)] = test3Tile// putting the tile3 on the grid

        // calling the method and putting its output as a value
        val list3 = botService.getAllValidLocationsForWildlifeToken(currentPlayer)

        assertEquals(3, currentPlayer.grid.size) // testing if the tile2 and tile 3 are now on the grid

        assertEquals(2, list3.size) // it must be  two because tile 1 and tile 3 are valid locations

        // now the possible places are not empty, so they are not valid anymore
        test3Tile.wildlifeToken = WildlifeToken(Animal.SALMON)
        test2Tile.wildlifeToken = WildlifeToken(Animal.SALMON)

        assertNotNull(test3Tile.wildlifeToken) // making sure both test Tiles are now with one wildLifeToken

        assertNotNull(test2Tile.wildlifeToken) // making sure both test Tiles are now with one wildLifeToken

        // creating a new list( this list is now empty, there is no place to play the token
        val list4 = botService.getAllValidLocationsForWildlifeToken(currentPlayer)

        assertEquals(0, list4.size)// there is no Place to play our wild Life Token

        // the Token in Hand must be putted in the cloth bag( the size was 96 now it must be 97)
        assertEquals(97, game.wildlifeTokens.size)
        assertEquals(0, currentPlayer.chosenHabitatTile.size)// player hand must be empty
    }

    /** testing again get all valid locations for wild life token but now with a more complete example  */
    @Test
    fun getAllValidLocationsWildLifeTokenComplexExample() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")

        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)

        gameService.startGame(playerNames, playersType) // initializing the game

        val game = rootService.game   //putting game in a value

        val currentPlayer = game?.players?.get(0)// choosing the player which the method will work with

        requireNotNull(currentPlayer) // current player cant be empty

        currentPlayer.grid.clear() // taking out the initial tiles

        assertEquals(0, currentPlayer.grid.size)// testing the actual grid, it must be empty now

        val salmon = WildlifeToken(Animal.SALMON)// creating the animal Token

        currentPlayer.chosenWildlifeToken.add(salmon)//putting the animal in players hand

        val test1Tile = HabitatTile( // creating a Habitat Tile WITHOUT Salmon
            1,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST, HabitatType.PRAIRIE),
            mutableListOf(Animal.FOX, Animal.ELK, Animal.BEAR), false)
        // creating a Habitat Tile with salmon
        val test2Tile = HabitatTile(
            2,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST, HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false
        )
        // creating a Habitat Tile with salmon
        val test3Tile = HabitatTile(
            3,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        // creating a Habitat Tile WITHOUT Salmon
        val test4Tile = HabitatTile(
            4,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE),
            mutableListOf(Animal.FOX, Animal.ELK, Animal.BEAR), false)
        // creating a Habitat Tile with salmon
        val test5Tile = HabitatTile(
            5,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        // creating a Habitat Tile with salmon
        val test6Tile = HabitatTile(
            6,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        // creating a Habitat Tile WITHOUT Salmon
        val test7Tile = HabitatTile(
            7,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.FOX, Animal.ELK, Animal.BEAR), false)
        // creating a Habitat Tile with salmon
        val test8Tile = HabitatTile(
            8,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE),
            mutableListOf(Animal.SALMON), false)
        // creating a Habitat Tile with salmon
        val test9Tile = HabitatTile(
            9,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON), false)
        // creating a Habitat Tile WITHOUT Salmon
        val test10Tile = HabitatTile(10,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.FOREST, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.FOX, Animal.BEAR), false)
        // 9 and 6 are now not valid
        // There is 6 valid locations but only 2 are not full
        test6Tile.wildlifeToken = WildlifeToken(Animal.SALMON)
        test9Tile.wildlifeToken = WildlifeToken(Animal.SALMON)

        // every single tile is now on currentPlayer grid
        currentPlayer.grid[Pair(1, 0)] = test1Tile
        currentPlayer.grid[Pair(1, 1)] = test2Tile
        currentPlayer.grid[Pair(1, 2)] = test3Tile
        currentPlayer.grid[Pair(1, 3)] = test4Tile
        currentPlayer.grid[Pair(1, 4)] = test5Tile
        currentPlayer.grid[Pair(1, 5)] = test6Tile
        currentPlayer.grid[Pair(0, 1)] = test7Tile
        currentPlayer.grid[Pair(0, 2)] = test8Tile
        currentPlayer.grid[Pair(0, 3)] = test9Tile
        currentPlayer.grid[Pair(0, 4)] = test10Tile
        val list = botService.getAllValidLocationsForWildlifeToken(currentPlayer)
        // testing the actual grid, it must be empty now
        assertEquals(4, list.size)
        assertEquals(Pair(1, 1), list[0])
        assertEquals(Pair(1, 2), list[1])
        assertEquals(Pair(1, 4), list[2])
        assertEquals(Pair(0, 2), list[3])
    }
    /** testing again get all valid locations for habitat tile method   */
    @Test
    fun getAllValidLocationsHabitatTileTest() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)
        // initializing the game
        gameService.startGame(playerNames, playersType)
        //putting game in a value
        val game = rootService.game
        // choosing the player which the method will work with
        val currentPlayer = game?.players?.get(0)
        // current player cant be empty
        requireNotNull(currentPlayer)
        // taking out the initial tiles
        currentPlayer.grid.clear()
        val test1Tile = HabitatTile(1,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE), mutableListOf(Animal.SALMON, Animal.HAWK), false)
        // putting one tile to the grid
        currentPlayer.grid[Pair(0, 0)] = test1Tile
        // calling the method and saving the output
        val list = botService.getAllValidLocationsForHabitatTile(currentPlayer)
        // it must give 6 neighbours because there is no other tile in the grid
        assertEquals(6, list.size)
        //testing if every single right  Coordinate is actually in the list
        assertEquals(Pair(0, -1), list[0])
        assertEquals(Pair(1, -1), list[1])
        assertEquals(Pair(1, 0), list[2])
        assertEquals(Pair(0, 1), list[3])
        assertEquals(Pair(-1, 1), list[4])
        assertEquals(Pair(-1, 0), list[5])
    }

    /** testing again get all valid locations for wild life token but now with a more complete example  */
    @Test
    fun getAllValidLocationsHabitatTileComplexExample() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)
        // initializing the game
        gameService.startGame(playerNames, playersType)

        //putting game in a value
        val game = rootService.game

        // choosing the player which the method will work with
        val currentPlayer = game?.players?.get(0)

        // current player cant be empty
        requireNotNull(currentPlayer)

        // taking out the initial tiles
        currentPlayer.grid.clear()

        // creating the test tiles
        val test1Tile = HabitatTile(
            1,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE),
            mutableListOf(Animal.SALMON, Animal.HAWK), false)
        val test2Tile = HabitatTile(
            2,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE),
            mutableListOf(Animal.SALMON, Animal.HAWK), false)
        val test3Tile = HabitatTile(
            3,
            arrayOf(HabitatType.MOUNTAIN, HabitatType.PRAIRIE, HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST,
                HabitatType.PRAIRIE),
            mutableListOf(Animal.SALMON, Animal.HAWK), false)

        currentPlayer.grid[Pair(0, 0)] = test1Tile
        currentPlayer.grid[Pair(1, 0)] = test2Tile
        currentPlayer.grid[Pair(2, 0)] = test3Tile

        val list = botService.getAllValidLocationsForHabitatTile(currentPlayer)

        // it must give 10 valid locations because there is also some redundant pair that we eliminated in the method
        assertEquals(10, list.size)

        //testing if every single right  Coordinate is actually in the list (0,0)
        assertEquals(Pair(0, -1), list[0])
        assertEquals(Pair(1, -1), list[1])
        assertEquals(Pair(0, 1), list[2])
        assertEquals(Pair(-1, 1), list[3])
        assertEquals(Pair(-1, 0), list[4])

        //testing if every single right Coordinate is actually in the list ( 1,0)
        assertEquals(Pair(2, -1), list[5])
        assertEquals(Pair(1, 1), list[6])

        //testing if every single right Coordinate is actually in the list (2,0)
        assertEquals(Pair(3, -1), list[7])
        assertEquals(Pair(3, 0), list[8])
        assertEquals(Pair(2, 1), list[9])
    }

    /** testing make ai move for bot ez   */
    @Test
    fun makeAiMoveTestBotEzTest() {
        val rootService = RootService()
        val botService = BotService(rootService)
        val gameService = GameService(rootService)
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")// creating players and
        val playersType = listOf(PlayerType.BOTEZ, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)

        gameService.startGame(playerNames, playersType)  // initializing the game

        val game = rootService.game

        requireNotNull(game)

        game.selectPairs.clear()

        // crating the current player
        val currentPlayer = game.players[0]

        // creating the tiles
        val habitatTile1 = HabitatTile(
            types = arrayOf(HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.SALMON), isKeystone = false, id = 7)
        val habitatTile2 = HabitatTile(
            types = arrayOf(HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.SALMON), isKeystone = false, id = 7)
        val habitatTile3 = HabitatTile(
            types = arrayOf(HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.SALMON), isKeystone = true, id = 7)
        val habitatTile4 = HabitatTile(
            types = arrayOf(HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.SALMON), isKeystone = true, id = 7)
        // creating new wild life tokens so it can be possible to select a pair
        val wildlifeToken1 = WildlifeToken(Animal.SALMON)
        val wildlifeToken2 = WildlifeToken(Animal.SALMON)
        val wildlifeToken3 = WildlifeToken(Animal.SALMON)
        val wildlifeToken4 = WildlifeToken(Animal.BEAR)

        // putting the pairs in the selectPairs structure
        game.selectPairs = mutableListOf(Pair(habitatTile1, wildlifeToken1), Pair(habitatTile2, wildlifeToken2),
            Pair(habitatTile3, wildlifeToken3), Pair(habitatTile4, wildlifeToken4))

        val habitatTile5 = HabitatTile(
            types = arrayOf(HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,
                HabitatType.FOREST), animals = mutableListOf(Animal.BEAR), isKeystone = true, id = 2)

        val habitatTile6 = HabitatTile(
            types = arrayOf(HabitatType.RIVER, HabitatType.RIVER, HabitatType.RIVER, HabitatType.RIVER,
                HabitatType.RIVER,
                HabitatType.RIVER), animals = mutableListOf(Animal.SALMON), isKeystone = true, id = 2)

        // clearing the grid before the test
        currentPlayer.grid.clear()
        currentPlayer.chosenWildlifeToken.clear()
        currentPlayer.chosenHabitatTile.clear()

        // adding the habitat Tiles into the grid
        currentPlayer.grid[Coordinate(0, 0)] = habitatTile6
        currentPlayer.grid[Coordinate(0, 1)] = habitatTile5

        //grid size before the function call
        val gridSize = currentPlayer.grid.size
        assertEquals(2, currentPlayer.grid.size)

        // calling the function
        botService.makeAIMove(currentPlayer)

        // the grid must be now bigger because one new Habitat tile was placed there
        assertEquals(gridSize + 1, currentPlayer.grid.size)

        // creating the boolean depending on the method placing the random pair in the grid
        val hasHabitat = currentPlayer.grid.values.any { it.id == 7 }
        val hasWildLifeToken = currentPlayer.grid.values.any { it.wildlifeToken == wildlifeToken1 || it.wildlifeToken==
                wildlifeToken2 ||it.wildlifeToken == wildlifeToken3 || it.wildlifeToken == wildlifeToken4 }

        // assert so we can test if both booleans are true as expected
        assertEquals(true, hasHabitat)
        assertEquals(true, hasWildLifeToken)

        // testing if players hand is now empty after the turn
        assertEquals(0, currentPlayer.chosenHabitatTile.size)
        assertEquals(0, currentPlayer.chosenWildlifeToken.size)
    }

    /** testing the make Ai move method for bot hard */
    @Test
    fun makeAiMoveTestBotHard() {

        val rootService = RootService()
        val gameService = GameService(rootService)


        // creating players and
        val playerNames = listOf("Alice", "Bob", "Max", "Lea")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.NETWORK, PlayerType.LOCAL, PlayerType.NETWORK)

        // initializing the game
        gameService.startGame(playerNames, playersType)


    }
}









