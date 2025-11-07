package service

import entity.*
import java.io.FileNotFoundException


/**
 * Constants for csv data files of habitat tiles
 */
internal const val TILES_FILE = "/tiles.csv"
internal const val START_TILES_FILE = "/start_tiles.csv"

/**
 * This class has methods on game tiles and tokens.
 * Creating draw tokens, extracting and converting data for tiles from csv files.
 * Placement of start tiles and inspection of tiles neighbors.
 *
 * @param rootService The root service to which this service belongs
 */
class HabitatTileService(private val rootService: RootService) : AbstractRefreshingService() {

    //This discardStack is used to temporarily store tokens when they are swapped out,
    // but are eventually put back into the bag.
    val discardTokens: MutableList<WildlifeToken> = mutableListOf<WildlifeToken>()

    /** creates a Draw Stack for Wildlife Tokens
     *
     * @throws IllegalStateException if no game is currently active
     * */
    fun createDrawStackWildlifeToken() {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        // 1. Create 20 WildlifeTokens for each Animal
        val wildlifeTokenStack = Animal.values().flatMap { animal ->
            List(20) { WildlifeToken(animal) }
        }
        // 2. Shuffle the WildlifeTokens to randomize the order
        game.wildlifeTokens.addAll(wildlifeTokenStack.shuffled().toMutableList())

        onAllRefreshables {
            refreshAfterCreateDrawStackWildlifeToken()
        }
    }

    /** creates a Draw Stack for Habitat Tiles
     *
     * @throws IllegalStateException if no game is currently active
     * */
    fun createDrawStackHabitatTile() {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val allTiles = getAllHabitatTiles().shuffled()
        when (game.players.size) {
            2 -> {
                game.habitatTiles.addAll(allTiles.take(43))
            }

            3 -> {
                game.habitatTiles.addAll(allTiles.take(63))
            }

            4 -> {
                game.habitatTiles.addAll(allTiles.take(83))
            }
        }

        onAllRefreshables {
            refreshAfterCreateDrawStackHabitatTile()
        }
    }

    /**
     * 4 combinations are used for player selection later on.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun createPlayStack() {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        repeat(4) {
            game.selectPairs.add(game.habitatTiles.last() to game.wildlifeTokens.last())
            game.habitatTiles.removeLast()
            game.wildlifeTokens.removeLast()
        }

        onAllRefreshables {
            refreshAfterCreatePlayStack()
        }
    }

    /**
     * Place a tile to determine if it is surrounded by empty space.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun hasNoNeighbor(coordinate: Coordinate, grid: MutableMap<Coordinate, HabitatTile>): Boolean {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val neighbors = listOf(
            Coordinate(coordinate.first!! + 1, coordinate.second),     // right,below
            Coordinate(coordinate.first!! - 1, coordinate.second),     // lift, above
            Coordinate(coordinate.first, coordinate.second!! + 1),     // right
            Coordinate(coordinate.first, coordinate.second!! - 1),     // lift
            Coordinate(coordinate.first!! + 1, coordinate.second!! - 1), // lift,below
            Coordinate(coordinate.first!! - 1, coordinate.second!! + 1)  // right,above
        )

        // if no neighbor, just return true.
        return neighbors.none { neighbor -> grid.containsKey(neighbor) }
    }

    /**
     * After the initial 15 tiles are distributed to the players,
     * they are first placed in the initial tile stack (starterHabitatTiles).
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun distributeInitialHabitats() {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val startTiles = getStarterHabitatTiles() // 15 Tiles

        game.starterHabitatTiles = startTiles
    }
    /**
     * This method will place the initial habitat tiles
     */
    fun placeInitialHabitats(tileGroup: MutableList<List<HabitatTile>>, random: Boolean = true) {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        //init Coordination for 3 start Tiles in Axial Coordinate System
        val initialCoordinates = listOf(
            Coordinate(0, 0),
            Coordinate(0, 1),
            Coordinate(-1, 1)
        )

        //place each tile in initialCoordination
        game.players.forEachIndexed { idx, player ->
            val playerTiles = if (random) {
                tileGroup.random()
            } else {
                tileGroup.first()
            }
            tileGroup.remove(playerTiles)

            playerTiles.forEachIndexed { index, tile ->
                player.grid[initialCoordinates[index]] = tile
            }
        }

        onAllRefreshables {
            refreshAfterPlaceInitialHabitats()
        }
    }


    /**
     * 3 tiles from the starterHabitatTiles are distributed to each player.
     *
     * @throws IllegalStateException if no game is currently active.
     */
    fun placeInitialHabitats() {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }

        val tileGroup = game.starterHabitatTiles.chunked(3).toMutableList()

        placeInitialHabitats(tileGroup)
    }

    /**
     * Extracts data from a CsvData file and converts it to a list.
     */
    internal fun parseHabitatCsvData(fileName: String): List<ParsedHabitatData> {
        val habitats = mutableListOf<ParsedHabitatData>()

        val text = this::class.java.getResource(fileName)?.readText()

        checkNotNull(text) { "File not found: $fileName" }

        val lines = text.lines().filter { line ->
            line != "" && !line.startsWith("id") && !line.startsWith("--") && !line.startsWith(" ")
        }

        for (line in lines) {
            val lineData = line.split(";")
            habitats.add(
                ParsedHabitatData(
                    lineData[0].toInt(),
                    lineData[1],
                    lineData[2],
                    lineData[3] == "yes"
                )
            )
        }
        return habitats
    }

    /**
     * data class ParsedHabitatData
     */
    internal data class ParsedHabitatData(
        val id: Int,
        val habitats: String,
        val wildlife: String,
        val isKeystone: Boolean,
    )

    internal fun getAllHabitatTiles() = convertParsedDataToHabitatTiles(parseHabitatCsvData(TILES_FILE))
    internal fun getStarterHabitatTiles() = convertParsedDataToHabitatTiles(parseHabitatCsvData(START_TILES_FILE))

    /**
     * Convert the list output by ParsedData to a list of type HabitatTile.
     */
    private fun convertParsedDataToHabitatTiles(parsedData: List<ParsedHabitatData>): MutableList<HabitatTile> {
        val habitatTiles = mutableListOf<HabitatTile>()

        for (data in parsedData) {
            // Parse habitats string into HabitatType array
            val habitatTypes = data.habitats.map { char ->
                HabitatType.values().find { it.name.startsWith(char.toString()) }
                //?: throw IllegalArgumentException("Invalid habitat type: $char")
            }.toTypedArray()

            // Parse wildlife string into MutableList<Animal>
            val animals = data.wildlife.map { char ->
                Animal.values().find { it.name.startsWith(char.toString()) }
                //?: throw IllegalArgumentException("Invalid animal type: $char")
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
}