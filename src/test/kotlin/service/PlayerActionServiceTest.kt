package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**  class of tests for the player action service  */
class PlayerActionServiceTest {
    private val rootService = RootService()
    private val gameService = GameService(rootService)

    /**setting up the environment so the tests can be done properly   */
    @BeforeEach
    fun setUp() {
        val playerNames = listOf("Alice", "Bob")
        val playersType = listOf(PlayerType.LOCAL, PlayerType.LOCAL)
        val selectedScoringCards = listOf(true,true,true,true,true)

        gameService.startGame(playerNames, playersType,selectedScoringCards)

        val game = rootService.game
        checkNotNull(game) { "No game is currently active" }

        rootService.networkService.connectionState != ConnectionState.DISCONNECTED

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


    /** testing the method for picking a chosen pair from select Pair structure */
    @Test
    fun pickOnePairTest(){
        val game = rootService.game!!

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)

        val t1 = WildlifeToken(Animal.ELK)

        repeat(2){
            game.selectPairs[3].copy(first =game.habitatTiles.first(), second = game.wildlifeTokens.first() )
            //game.selectPairs.add(game.habitatTiles.first() to game.wildlifeTokens.first())
            game.habitatTiles.removeFirst()
            game.wildlifeTokens.removeFirst()
        }

        assertTrue(game.habitatTiles.isNotEmpty(),"drawStack of habitatTiles should not empty!")

        rootService.playerActionService.pickOnePair(h1 to t1)

        assertTrue(game.players[game.currentPlayer].chosenHabitatTile.size==1,
            "ChosenHabitatTile should 1 tile exist")
        assertTrue(game.players[game.currentPlayer].chosenWildlifeToken.size==1,
            "ChosenWildlifeToken should 1 token exist")
        assertEquals(40,game.habitatTiles.size,"habitatTiles should still 40 !")
        assertEquals(97,game.wildlifeTokens.size,"wildlifeTokens should still 96+1 !")
    }

    /** testing if pack pair methods is working properly during the last turn */
    @Test
    fun pickOnePairLastTurnTest(){
        val game = rootService.game!!

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)

        val t1 = WildlifeToken(Animal.ELK)

        repeat(43){
            game.habitatTiles.removeFirst()
        }
        repeat(100){
            game.wildlifeTokens.removeFirst()
        }
/*

        repeat(4){
            game.selectPairs.add(game.habitatTiles.first() to game.wildlifeTokens.first())
            game.habitatTiles.removeFirst()
            game.wildlifeTokens.removeFirst()
        }
*/

        assertTrue(game.habitatTiles.isEmpty(),"drawStack of habitatTiles should empty!")

        rootService.playerActionService.pickOnePair(h1 to t1)

        assertTrue(game.players[game.currentPlayer].chosenHabitatTile.size==1,
            "ChosenHabitatTile should 1 tile exist")
        assertTrue(game.players[game.currentPlayer].chosenWildlifeToken.size==1,
            "ChosenWildlifeToken should 1 token exist")
    }

    /** testing the function play habitat tile in a situation which the place is already occupied */
    @Test
    fun playHabitatTileOccupiedTest(){
        val game = rootService.game!!

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)

        val c1=Coordinate(0,0)
        assertThrows<IllegalStateException> { rootService.playerActionService.playHabitatTile(c1) }
    }

    /** testing the function play habitat tile in a situation which there is no neighbours around the place  */
    @Test
    fun playHabitatTileNoNeighborTest() {
        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.ELK), false
        )
        val coordinate=Coordinate(4,4)
        assertThrows<IllegalStateException> { rootService.playerActionService.playHabitatTile(coordinate) }
    }

    /** testing the function play habitat tile in a conventional situation */
    @Test
    fun playHabitatTileTest() {
        val game = rootService.game!!

        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.ELK), false
        )
        val t1 = WildlifeToken(Animal.ELK)

        game.selectPairs.add(Pair(h1,t1))

        game.players[game.currentPlayer].chosenHabitatTile.add(h1)

        val coordinate=Coordinate(0,-1)

        rootService.playerActionService.playHabitatTile(coordinate)
        assertTrue(game.players[game.currentPlayer].grid.containsKey(coordinate),
            "this coordinate should be placed!")
        assertTrue(game.players[game.currentPlayer].chosenHabitatTile.isNotEmpty(),
            "ChosenHabitatTile after playTile should not be empty!")
    }

    /** testing the function play wild life token for a situation which there is no habitat tile in the location  */
    @Test
    fun playWildlifeTokenNoTileTest(){
        val game = rootService.game!!

        val t1 = WildlifeToken(Animal.ELK)

        game.players[game.currentPlayer].chosenWildlifeToken.add(t1)
        val coordinate=Coordinate(0,-1)
        //game.players[game.currentPlayer].grid[coordinate]
        assertThrows<IllegalStateException> { rootService.playerActionService.playWildlifeToken(coordinate)}
    }

    /** testing the function play wild life token placing the token where already exists a wild life token  */
    @Test
    fun playWildlifeTokenTileHasTokenTest(){
        val game = rootService.game!!

        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.ELK,Animal.HAWK), true
        )
        val t1 = WildlifeToken(Animal.ELK)
        val coordinate = Coordinate(0,-1)

        game.players[game.currentPlayer].grid[coordinate] = h1
        h1.wildlifeToken=t1

        game.players[game.currentPlayer].chosenWildlifeToken.add(t1)
        //game.players[game.currentPlayer].grid[coordinate]
        assertThrows<IllegalStateException> { rootService.playerActionService.playWildlifeToken(coordinate)}
    }

    /** testing the function play wild life token in a valid situation */
    @Test
    fun playWildlifeTokenTest(){
        val game = rootService.game!!

        //rootService.networkService.connectionState != ConnectionState.DISCONNECTED
        //rootService.isNetworkGame()

        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.ELK,Animal.HAWK), true
        )
        val t1 = WildlifeToken(Animal.ELK)
        val coordinate = Coordinate(0,-1)

        game.players[game.currentPlayer].chosenHabitatTile.add(h1)
        game.players[game.currentPlayer].chosenWildlifeToken.add(t1)
        game.players[game.currentPlayer].grid[coordinate] = h1
        rootService.playerActionService.playWildlifeToken(coordinate)
        assertEquals(t1, game.players[game.currentPlayer].grid[coordinate]?.wildlifeToken,
            "token not correct place on tile!")
        assertTrue(game.players[game.currentPlayer].chosenWildlifeToken.isEmpty(),
            "ChosenWildlifeToken should be empty!")
        assertTrue(game.players[game.currentPlayer].natureToken==1,
            "NatureToken should more than 0!")
        assertTrue(game.players[game.currentPlayer].grid[coordinate]!!.isKeystone,
            "isKeystone should be true! ")
    }

    /** testing the function play wild life during the last turn   */
    @Test
    fun playWildlifeTokenLastTurnTest(){
        val game = rootService.game!!

        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.ELK,Animal.HAWK), true
        )
        val t1 = WildlifeToken(Animal.ELK)
        val coordinate = Coordinate(0,-1)

        game.selectPairs.add(Pair(h1,t1))
        game.players[game.currentPlayer].chosenHabitatTile.add(h1)
        game.players[game.currentPlayer].chosenWildlifeToken.add(t1)
        game.players[game.currentPlayer].grid[coordinate] = h1
        rootService.playerActionService.playWildlifeToken(coordinate)


        assertTrue(game.players[game.currentPlayer].chosenWildlifeToken.isEmpty(),
            "ChosenWildlifeToken should be cleared")

    }

    /** testing the function play wild life token placing the token where the current animal is not allowed */
    @Test
    fun playWildlifeTokenFalseAnimalTest(){
        val game = rootService.game!!

        val h1 = HabitatTile(
            1, arrayOf(
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN,
                HabitatType.WETLAND,
                HabitatType.WETLAND,
                HabitatType.WETLAND
            ), mutableListOf(Animal.HAWK), false
        )
        val t1 = WildlifeToken(Animal.ELK)
        val coordinate = Coordinate(0,-1)

        game.players[game.currentPlayer].chosenWildlifeToken.add(t1)
        game.players[game.currentPlayer].grid[coordinate] = h1
        assertThrows<IllegalStateException> {rootService.playerActionService.playWildlifeToken(coordinate)}
    }

    /** testing if the function wild life token free select is working properly  */
    @Test
    fun useNatureTokenFreeSelectTest(){
        val game = rootService.game!!

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

        val t1 = WildlifeToken(Animal.ELK)
        val t2 = WildlifeToken(Animal.HAWK)
/*
        repeat(2){
            game.selectPairs.add(game.habitatTiles.first() to game.wildlifeTokens.first())
            game.habitatTiles.removeFirst()
            game.wildlifeTokens.removeFirst()
        }*/

        game.selectPairs.add(Pair(h1,t1))
        game.selectPairs.add(Pair(h2,t2))
        game.players[game.currentPlayer].natureToken =2
        assertTrue(game.habitatTiles.isNotEmpty(),"drawStack of habitatTiles should not empty!")

        rootService.playerActionService.useNatureTokenFreeSelect(h1 to t1,h2 to t2)

        assertTrue(game.players[game.currentPlayer].natureToken == 1,
            "NatureToken should after useNatureTokenFreeSelectTest reduce 1 !")
        assertTrue(game.players[game.currentPlayer].chosenHabitatTile.size==1,
            "ChosenHabitatTile should 1 tile exist")
        assertTrue(game.players[game.currentPlayer].chosenWildlifeToken.size==1,
            "ChosenWildlifeToken should 1 token exist")

        assertEquals(42,game.habitatTiles.size,"habitatTiles should still 42 !")
        assertEquals(99,game.wildlifeTokens.size,"wildlifeTokens should still 99 !")
    }

    /** testing the function play wild life token placing the token where already exists a wild life token  */
    @Test
    fun useNatureTokenFreeSelectLastTurnTest(){
        val game = rootService.game!!

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

        val t1 = WildlifeToken(Animal.ELK)
        val t2 = WildlifeToken(Animal.HAWK)

        repeat(2){
            game.selectPairs.add(game.habitatTiles.removeLast() to game.wildlifeTokens.removeLast())
            //game.habitatTiles.removeLast()
            //game.wildlifeTokens.removeFirst()
        }

        game.players[game.currentPlayer].natureToken =2
        assertTrue(game.habitatTiles.isNotEmpty(),"drawStack of habitatTiles should not empty!")
        game.habitatTiles.clear()
        rootService.playerActionService.useNatureTokenFreeSelect(h1 to t1,h2 to t2)
    }

    /** testing the function play wild life token placing the token where already exists a wild life token  */
    @Test
    fun useNatureTokenReplace(){
        val game = rootService.game!!

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

        val t1 = WildlifeToken(Animal.ELK)
        val t2 = WildlifeToken(Animal.HAWK)

        game.players[game.currentPlayer].natureToken=2
        val replaceList = mutableListOf(Pair(h1,t1),Pair(h2,t2))
        game.selectPairs.addAll(replaceList)
        rootService.playerActionService.useNatureTokenReplace(replaceList)
        assertTrue(game.players[game.currentPlayer].natureToken == 1,
            "NatureToken should after useNatureTokenReplace reduce 1 !")
        assertNotEquals(96,game.wildlifeTokens.size,"token should give back to bag ! ")
    }

    /** testing the discard wild life token function */
    @Test
    fun discardWildlifeToken(){
        val game = rootService.game!!

        val t1 = WildlifeToken(Animal.ELK)
        rootService.playerActionService.discardWildlifeToken(t1)
        assertEquals(101,game.wildlifeTokens.size,"wildlifeToken should 100+4+1 !")
    }

    /**testing if habitat tile rotation is working properly  */
    @Test
    fun rotateHabitatTileTest(){
        val game = rootService.game!!

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)
        val h1AfterRotated = HabitatTile(1, arrayOf(
            HabitatType.WETLAND,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)

        val coordinate= Coordinate(0,-1)
        game.players[game.currentPlayer].chosenHabitatTile.add(h1)
        rootService.playerActionService.playHabitatTile(coordinate)
        rootService.playerActionService.rotateHabitatTile(coordinate)
        assertEquals(h1AfterRotated.animals,game.players[game.currentPlayer].grid[coordinate]!!.animals,
            "not successful rotating ")
    }

    /**testing id the habitat tile is working properly but without any tile to rotate */
    @Test
    fun rotateHabitatTileNoTileTest(){
        val game = rootService.game!!

        val h1= HabitatTile(1, arrayOf(
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.MOUNTAIN,
            HabitatType.WETLAND,
            HabitatType.WETLAND,
            HabitatType.WETLAND), mutableListOf(Animal.ELK), false)

        val coordinate= Coordinate(0,-1)
        assertThrows<IllegalStateException> {rootService.playerActionService.rotateHabitatTile(coordinate)}
    }
    /** testing a situation where no game was found  */
    @Test
    fun noGameTest(){
        rootService.game=null

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

        val t1 = WildlifeToken(Animal.ELK)
        val t2 = WildlifeToken(Animal.HAWK)
        val replaceList = mutableListOf(Pair(h1,t1),Pair(h2,t2))

        val coordinate= Coordinate(0,-1)
        assertThrows<IllegalStateException> {rootService.playerActionService.pickOnePair(h1 to t1)}
        assertThrows<IllegalStateException> {rootService.playerActionService.playHabitatTile(coordinate)}
        assertThrows<IllegalStateException> {rootService.playerActionService.playWildlifeToken(coordinate)}
        assertThrows<IllegalStateException> {rootService.playerActionService.rotateHabitatTile(coordinate)}
        assertThrows<IllegalStateException> {rootService.playerActionService.discardWildlifeToken(t1)}
        assertThrows<IllegalStateException> {rootService.playerActionService.
        useNatureTokenFreeSelect(h1 to t1,h1 to t1)}
        assertThrows<IllegalStateException> {rootService.playerActionService.useNatureTokenReplace(replaceList)}
    }
}