package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [HabitatTileService]
 */
class HabitatTileServiceTest {
    private var rootService: RootService = RootService()

    /**
     * Tests [HabitatTileService.parseHabitatCsvData]
     * Tests if the CSV is correctly parsed
     */
    @Test
    fun testParseHabitatCsvData() {
        val parsedData = rootService.habitatTileService.parseHabitatCsvData(TILES_FILE)
        assert(parsedData[0].id == 0)
        assert(parsedData[0].wildlife == "EB")
        assert(parsedData[0].habitats == "PPPFFF")
        assert(!parsedData[0].isKeystone)

        // Starter Tiles
        val parsedStarterData = rootService.habitatTileService.parseHabitatCsvData(START_TILES_FILE)
        assert(parsedStarterData[0].id == 10)
        assert(parsedStarterData[0].wildlife == "B")
        assert(parsedStarterData[0].habitats == "MMMMMM")
        assert(parsedStarterData[0].isKeystone)
    }

    /** testing if the draw stack for habitat tiles has been created properly*/
    @Test
    fun testCreateDrawStackHabitatTile4() {

        // Get current game and check if it is running
        val players = arrayOf(
            Player("Bob", PlayerType.LOCAL),
            Player("Alice", PlayerType.LOCAL),
            Player("Tom", PlayerType.LOCAL),
            Player("Hanks", PlayerType.LOCAL)
        )

        val game = CascadiaGame(players, 0)
        rootService.game = game

        // Check if the game is running
        rootService.habitatTileService.createDrawStackHabitatTile()

        game.habitatTiles.forEach {
            println(it.toString())
        }
    }

    /** testing if the draw stack for habitat tiles has been created properly*/
    @Test
    fun testCreateDrawStackHabitatTile3() {

        // Get current game and check if it is running
        val players = arrayOf(
            Player("Bob", PlayerType.LOCAL),
            Player("Alice", PlayerType.LOCAL),
            Player("Tom", PlayerType.LOCAL)
        )

        val game = CascadiaGame(players, 0)
        rootService.game = game

        // Check if the game is running
        rootService.habitatTileService.createDrawStackHabitatTile()

        game.habitatTiles.forEach {
            println(it.toString())

        }
    }

    private fun convertParsedDataToHabitatTiles
                (parsedData: List<HabitatTileService.ParsedHabitatData>): MutableList<HabitatTile>{
        val habitatTiles = mutableListOf<HabitatTile>()

        for (data in parsedData) {
            // Parse habitats string into HabitatType array
            val habitatTypes = data.habitats.map { char ->
                HabitatType.values().find { it.name.startsWith(char.toString()) }
        //            ?: throw IllegalArgumentException("Invalid habitat type: $char")
            }.toTypedArray()

            // Parse wildlife string into MutableList<Animal>
            val animals = data.wildlife.map { char ->
                Animal.values().find { it.name.startsWith(char.toString())  }
        //            ?: throw IllegalArgumentException("Invalid animal type: $char")
            }.toMutableList()

            // Create HabitatTile object
            habitatTiles.add(
                HabitatTile(
                    id = data.id,
                    types = habitatTypes,
                    animals = animals,
                    isKeystone = data.isKeystone
                )
            )
        }
        return habitatTiles
    }

    /** testing if the  draw stacl for wild life token has been created properly  */
    @Test
    fun testCreateDrawStackWildlifeToken() {

        val game = CascadiaGame(
            arrayOf(
                Player("alice", PlayerType.LOCAL), Player("bob", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL), Player("hanks", PlayerType.LOCAL)
            )
        )
        checkNotNull(game)
        rootService.game = game

        rootService.habitatTileService.createDrawStackWildlifeToken()
        println(rootService.game!!.wildlifeTokens.size)

        for (wildlifeToken in rootService.game!!.wildlifeTokens) {
            println(wildlifeToken.toString())
        }
    }

    /** testing if the initial habitat tiles have been distributed properly  */
    @Test
    fun testDistributeInitialHabitats(){
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )

        rootService.game = game
        checkNotNull(rootService.game)

        rootService.habitatTileService.distributeInitialHabitats()

        assertNotNull(rootService.game!!.starterHabitatTiles.size)

    }


    /** testing if the initial  habitat tiles have been placed properly */
    @Test
    fun testPlaceInitialHabitats() {
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        rootService.game = game

        checkNotNull(rootService.game)

        rootService.habitatTileService.createDrawStackHabitatTile()
        rootService.habitatTileService.distributeInitialHabitats()
        // Call the method to be tested
        rootService.habitatTileService.placeInitialHabitats()

        val players = rootService.game!!.players

        players.forEachIndexed { index, player ->
            assertEquals(3, player.grid.values.size,
                "Player $index should have 3 starter habitat tiles")

            val p1Tiles = player.grid.values.toList()
            println("$player tiles: ${p1Tiles.map { it.id }.toList()}")

            // Check if initial tiles are not the same across players
            players.forEachIndexed { idx, p2 ->
                if (player != p2) {
                    val p2Tiles = p2.grid.values.toList()
                    assertTrue(
                        p1Tiles.intersect(p2Tiles.toSet()).isEmpty(),
                        "Two players have the same starter habitat tile: P1 $player \n $p1Tiles, P2 $p2 \n $p2Tiles"
                    )
                }
            }
        }
    }

    /** testing if the play stack has been created properly*/
    @Test
    fun createPlayStack(){
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        rootService.game = game

        checkNotNull(rootService.game)
        rootService.habitatTileService.createDrawStackHabitatTile()
        rootService.habitatTileService.createDrawStackWildlifeToken()
        rootService.habitatTileService.createPlayStack()
        assertEquals(4,game.selectPairs.size,"selectPairs.size should be 4 !")
    }
}