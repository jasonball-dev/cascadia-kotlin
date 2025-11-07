package service

import entity.*

/**
 * The ScoringCardService class is responsible for calculating the scores of players
 * based on the scoring rules defined in the game and the configurations of the habitat tiles and wildlife tokens.
 *
 * @property rootService The RootService that provides access to the game state and other services.
 */
class ScoringCardService(private val rootService: RootService) : AbstractRefreshingService() {

    private var bearScore = 0
    private var elkScore = 0
    private var salmonScore = 0
    private var hawkScore = 0
    private var foxScore = 0

    val habitatScores = mutableMapOf<HabitatType, Int>()

    private var forestScore = 0
    private var mountainScore = 0
    private var riverScore = 0
    private var prairieScore = 0
    private var wetlandScore = 0

    /** this function will calculate every single score for each player */
    fun calculateScoreForAllPlayers() {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate scores." }

        game.players.forEach { player ->
            calculateScoreForPlayer(player)
           // println(" player name " + player.name + " score is "+ player.score)
        }
        calculationOfBonuses()
        game.players.forEach {player ->
           player.score += getAllBonuses(player)
          // println(" player name " + player.name + " score is "+ player.score)
        }
    }

    /**
     * Calculates scores for all players in the current game. Each player's score is recalculated based on
     * the scoring rules defined by the scoring cards and their grid configuration.
     *
     * @throws IllegalStateException if there is no active game.
     */
    fun calculateScoreForPlayer(player: Player) {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate scores." }
        player.score=0

        game.scoringCards.forEach { scoringCard ->
            when (scoringCard.animal) {
                Animal.BEAR -> calculateBearScore(player, scoringCard)

                Animal.ELK -> if (scoringCard.isRuleA) {
                    calculateElkScoreTypA(player)
                } else {
                    calculateElkScoreB(player)
                }

                Animal.SALMON -> calculateSalmonScore(player, scoringCard)

                Animal.HAWK -> if (scoringCard.isRuleA) {
                    calculateHawkScoreTpyA(player, scoringCard)
                } else {
                    calculateHawkScoreTpyB(player, scoringCard)
                }

                Animal.FOX -> if (scoringCard.isRuleA) {
                    calculateFoxScoreTypeA(player)
                } else {
                    calculateFoxScoreTypeB(player)
                }
            }

        }
        player.animalScores[Animal.BEAR] = bearScore
        player.animalScores[Animal.FOX] = foxScore
        player.animalScores[Animal.HAWK] = hawkScore
        player.animalScores[Animal.ELK] = elkScore
        player.animalScores[Animal.SALMON] = salmonScore

        calculateHabitatCorridors(player)

        player.animalScores.forEach{
            player.score  += it.value
        }

        player.habitatCorridorScores.forEach{
             println(" ${it.key}  ${it.value}  player is ${player.name}")
             player.score += it.value
        }
        player.score  += player.natureToken

        bearScore = 0
        foxScore = 0
        hawkScore = 0
        elkScore = 0
        salmonScore = 0
    }

    /**
     * Calculates the score for Bear clusters on the player's grid.
     */
    private fun calculateBearScore(
        player: Player,
        scoringCard: ScoringCard,
    ) {
        val grid = player.grid
        val bearClusters = mutableSetOf<Set<Coordinate>>()
        val visited = mutableSetOf<Coordinate>()

        grid.forEach { (coordinate, tile) ->
            if (tile.wildlifeToken?.animal == Animal.BEAR && coordinate !in visited) {
                val cluster = sameWildlifeTokenNeighbors(coordinate, grid, visited)
                bearClusters.add(cluster)
            }
        }
        when (scoringCard.isRuleA) {
            true -> bearClusters.forEach { cluster ->
                if (cluster.size == 2) bearScore += 4
            }

            false -> bearClusters.forEach { cluster ->
                if (cluster.size == 3) bearScore += 10
            }
        }
    }

    /** this method will calculate the amount of points for ELK and with scoring cards type A */
    fun calculateElkScoreTypA(player: Player) {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate scores." }

        elkScore += calculateElkScoreForShapes(
            shapeGroupsWithScores = getShapeGroupsWithScoresTypeA(),
            player = player
        )
    }

    /** this method will calculate the amount of points for ELK and with scoring cards type B */
    fun calculateElkScoreB(player: Player) {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate scores." }

        elkScore += calculateElkScoreForShapes(
            shapeGroupsWithScores = getShapeGroupsWithScoresTypeB(),
            player = player
        )
    }

    /**
     * Returns shape groups and associated scores for "Type A".
     */
    private fun getShapeGroupsWithScoresTypeA(): List<Pair<List<List<Pair<Int, Int>>>, Int>> {
        // 4-tile shapes for Type A
        val shapes4Tile = listOf(
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0)),
            listOf(Pair(0, 0), Pair(1, -1), Pair(2, -2), Pair(3, -3)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(0, -2), Pair(0, -3)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(-2, 0), Pair(-3, 0)),
            listOf(Pair(0, 0), Pair(-1, 1), Pair(-2, 2), Pair(-3, 3))
        )

        // 3-tile shapes for Type A
        val shapes3Tile = listOf(
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 0), Pair(1, -1), Pair(2, -2)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(0, -2)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(-2, 0)),
            listOf(Pair(0, 0), Pair(-1, 1), Pair(-2, 2))
        )

        // 2-tile shapes for Type A
        val shapes2Tile = listOf(
            listOf(Pair(0, 0), Pair(0, 1)),
            listOf(Pair(0, 0), Pair(1, 0)),
            listOf(Pair(0, 0), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(0, -1)),
            listOf(Pair(0, 0), Pair(-1, 0)),
            listOf(Pair(0, 0), Pair(-1, 1))
        )

        // 1-tile shape
        val shapes1Tile = listOf(
            listOf(Pair(0, 0))
        )

        // Return them grouped with their respective scores
        return listOf(
            shapes4Tile to 13,
            shapes3Tile to 9,
            shapes2Tile to 5,
            shapes1Tile to 2
        )
    }

    /**
     * Returns shape groups and associated scores for "Type B".
     */
    private fun getShapeGroupsWithScoresTypeB(): List<Pair<List<List<Pair<Int, Int>>>, Int>> {
        // 4-tile shapes for Type B
        val shapes4Tile = listOf(
            listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(0, -1), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(0, -1), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(0, -1), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(0, 1), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(-2, 1), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1)),
            listOf(Pair(0, 0), Pair(0, 1), Pair(-1, 2), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, -1), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(1, -2), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, -1))
        )

        // 3-tile shapes for Type B
        val shapes3Tile = listOf(
            listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(1, -1)),
            listOf(Pair(0, 0), Pair(0, -1), Pair(-1, 0)),
            listOf(Pair(0, 0), Pair(-1, 0), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(0, 1), Pair(-1, 1))
        )

        // 2-tile shapes for Type B
        val shapes2Tile = listOf(
            listOf(Pair(0, 0), Pair(1, 0)),
            listOf(Pair(0, 0), Pair(0, 1)),
            listOf(Pair(0, 0), Pair(-1, 1)),
            listOf(Pair(0, 0), Pair(-1, 0)),
            listOf(Pair(0, 0), Pair(0, -1)),
            listOf(Pair(0, 0), Pair(1, -1))
        )

        // 1-tile shape
        val shapes1Tile = listOf(
            listOf(Pair(0, 0))
        )

        // Return them grouped with their respective scores
        return listOf(
            shapes4Tile to 13,
            shapes3Tile to 9,
            shapes2Tile to 5,
            shapes1Tile to 2
        )
    }

    /**
     * Reusable function that scans the player's grid for Elk shapes,
     * marks matched tiles as visited, and sums up the score. Returns the total
     * points found for these shapes in the current call.
     */
    private fun calculateElkScoreForShapes(
        shapeGroupsWithScores: List<Pair<List<List<Pair<Int, Int>>>, Int>>,
        player: Player
    ): Int {
        val visited = mutableSetOf<Coordinate>()
        var localScore = 0

        // Process shapes group by group
        for ((shapes, score) in shapeGroupsWithScores) {
            for ((coordinate, tile) in player.grid) {
                val wildlifeToken = tile.wildlifeToken

                // Skip visited tiles or tiles without Elk
                if (coordinate in visited || wildlifeToken?.animal != Animal.ELK) continue

                // Stop checking this shape list once we find a match (like a "break")
                shapes.firstOrNull { shape ->
                    val matchingGroup = shape.map { (dx, dy) ->
                        Pair(coordinate.first?.plus(dx), coordinate.second?.plus(dy))
                    }

                    // Check if the shape matches
                    val shapeMatches = matchingGroup.all {
                        it in player.grid &&
                                it !in visited &&
                                player.grid[it]?.wildlifeToken?.animal == Animal.ELK
                    }

                    if (shapeMatches) {
                        // Add matched coordinates to visited
                        visited.addAll(matchingGroup)
                        localScore += score
                    }

                    // Returning 'true' here tells firstOrNull() to stop searching shapes
                    shapeMatches
                }
            }
        }
        return localScore
    }


    /**
     * Calculates the score for Salmon chains on the player's grid.
     */
    private fun calculateSalmonScore(player: Player, scoringCard: ScoringCard) {
        val visited = mutableSetOf<Coordinate>()
        val salmonChains = mutableListOf<List<Coordinate>>()

        player.grid.forEach { (coordinate, tile) ->
            val wildlifeToken = tile.wildlifeToken
            if (wildlifeToken?.animal == Animal.SALMON
                && coordinate !in visited
                && getSalmonNeighbors(coordinate, player.grid).filter { it !in visited }.size == 1
            ) {
                val chain = findSalmonChain(coordinate, player.grid, visited)
                visited.addAll(chain)
                salmonChains.add(chain)
            }
        }

        salmonChains.forEach { chain ->
            val chainScore = scoreChain(chain.size, scoringCard.isRuleA)
            salmonScore += chainScore
        }
    }

    /**
     * Finds a chain of Salmon starting from a coordinate.
     */
    private fun findSalmonChain(
        start: Coordinate,
        grid: MutableMap<Coordinate, HabitatTile>,
        visited: MutableSet<Coordinate>
    ): List<Coordinate> {
        val chain = mutableListOf<Coordinate>()
        var current = start

        // when current salmon has one neighbor again, stop loop
        while (true) {
            if (getSalmonNeighbors(current, grid).filter { it !in visited }.size == 1) {
                visited.add(current)
                chain.add(current)

                current = getSalmonNeighbors(current, grid).first { it !in visited }
            }
            if (getSalmonNeighbors(current, grid).filter { it !in visited }.isEmpty()) {
                visited.add(current)
                chain.add(current)
                return chain
            }
            if (getSalmonNeighbors(current, grid).filter { it !in visited }.size > 1) {
                visited.addAll(getSalmonNeighbors(current, grid).filter { it !in visited })
                return chain

            }
        }
    }

    /**
     * Retrieves the neighbors of a coordinate that have Salmon.
     */
    private fun getSalmonNeighbors(
        coordinate: Coordinate,
        grid: MutableMap<Coordinate, HabitatTile>
    ): List<Coordinate> {
        val directions = listOf(
            Pair(0, 1), Pair(1, 0), Pair(1, -1),
            Pair(0, -1), Pair(-1, 0), Pair(-1, 1)
        )
        val neighbors = mutableListOf<Coordinate>()
        for (direction in directions) {
            val neighbor = Pair(
                coordinate.first?.plus(direction.first),
                coordinate.second?.plus(direction.second)
            )
            if (grid.containsKey(neighbor)) {
                val neighborTile = grid[neighbor]
                if (neighborTile?.wildlifeToken?.animal == Animal.SALMON) {
                    neighbors.add(neighbor)
                }
            }
        }
        return neighbors
    }

    /**
     * Scores a chain of Salmon based on its length and the scoring card.
     */
    private fun scoreChain(chainLength: Int, isRuleA: Boolean): Int {
        return when {
            chainLength <= 0 -> 0
            isRuleA -> when (chainLength) {
                1 -> 2
                2 -> 5
                3 -> 8
                4 -> 12
                5 -> 16
                6 -> 20
                else -> 25
            }

            else -> when (chainLength) {
                1 -> 2
                2 -> 4
                3 -> 9
                4 -> 11
                else -> 17
            }
        }
    }

    /**
     * Calculates the score for Hawks using scoring card rule A.
     */
    private fun calculateHawkScoreTpyA(
        player: Player,
        scoringCard: ScoringCard,
    ) {
        val grid = player.grid
        val hawks = mutableListOf<Coordinate>()
        var seperatedHaws = 0
        grid.forEach { (coordinate, tile) ->
            if (tile.wildlifeToken?.animal == Animal.HAWK) hawks.add(coordinate)
        }

        hawks.forEach { hawk ->
            if ((numberOfSameWildlifeTokenNeighbor(hawk, grid)) == 0) {
                seperatedHaws += 1
            }

        }

        if (scoringCard.isRuleA) {
            when (seperatedHaws) {
                1 -> seperatedHaws = 2
                2 -> seperatedHaws = 5
                3 -> seperatedHaws = 8
                4 -> seperatedHaws = 11
                5 -> seperatedHaws = 14
                6 -> seperatedHaws = 18
                7 -> seperatedHaws = 22
                8 -> seperatedHaws = 26
            }
            hawkScore += seperatedHaws
        }
    }

    /**This function scores points of Hawks from scoringCards typ B */
    private fun calculateHawkScoreTpyB(
        player: Player,
        scoringCard: ScoringCard
    ) {
        val grid = player.grid
        val hawks = mutableListOf<Coordinate>()

        //this is an Integer. But It will change to score points after when(seperatedHawsCouple)
        var seperatedHawsCouple = 1
        grid.forEach { (coordinate, tile) ->
            if (tile.wildlifeToken?.animal == Animal.HAWK) hawks.add(coordinate)
        }

        val visited = mutableSetOf<Coordinate>()
        hawks.forEach { hawk ->
            if (findIsolatedCoupleHawks(grid, visited).size == 0) {
                seperatedHawsCouple += 1
            }

        }

        if (!scoringCard.isRuleA) {
            when (seperatedHawsCouple) {
                1 -> seperatedHawsCouple = 0
                2 -> seperatedHawsCouple = 5
                3 -> seperatedHawsCouple = 9
                4 -> seperatedHawsCouple = 12
                5 -> seperatedHawsCouple = 16
                6 -> seperatedHawsCouple = 20
                7 -> seperatedHawsCouple = 24
                8 -> seperatedHawsCouple = 28
            }
            hawkScore += seperatedHawsCouple
        }
    }

    /**This function scores points of Fox from scoringCards typ A */
    private fun calculateFoxScoreTypeA(
        player: Player
    ) {
        val grid = player.grid
        grid.forEach { (coordinate, tile) ->
            if (tile.wildlifeToken?.animal == Animal.FOX) {
                val neighbors = listOf(
                    Coordinate(coordinate.first!! - 1, coordinate.second!! + 1),
                    Coordinate(coordinate.first, coordinate.second!! + 1),
                    Coordinate(coordinate.first!! + 1, coordinate.second),
                    Coordinate(coordinate.first!! + 1, coordinate.second!! - 1),
                    Coordinate(coordinate.first, coordinate.second!! - 1),
                    Coordinate(coordinate.first!! - 1, coordinate.second)
                )

                val uniqueAnimals = neighbors.mapNotNull { neighbor ->
                    grid[neighbor]?.wildlifeToken?.animal
                }.distinct()

                foxScore += uniqueAnimals.size
            }
        }
    }
    /**This function scores points of Fox from scoringCards typ B */
    private fun calculateFoxScoreTypeB(
        player: Player
    ) {
        val grid = player.grid
        // Iterate over all habitat tiles in the grid
        grid.forEach { (coordinate, tile) ->
            var point = 0
            // Check if the tile has a Fox wildlife token
            if (tile.wildlifeToken?.animal == Animal.FOX) {
                // Get all neighboring coordinates
                val neighbors = listOf(
                    Coordinate(coordinate.first!! - 1, coordinate.second!! + 1), // Top right
                    Coordinate(coordinate.first, coordinate.second!! + 1),      // Right
                    Coordinate(coordinate.first!! + 1, coordinate.second),      // Bottom right
                    Coordinate(coordinate.first!! + 1, coordinate.second!! - 1), // Bottom left
                    Coordinate(coordinate.first, coordinate.second!! - 1),      // Left
                    Coordinate(coordinate.first!! - 1, coordinate.second)       // Top left
                )

                // Map neighbors to their wildlife tokens
                val adjacentAnimals = neighbors.mapNotNull { neighbor ->
                    grid[neighbor]?.wildlifeToken?.animal
                }.filter { it != Animal.FOX }

                // Create a mutable list to track unique pairs
                val uniquePairs = mutableListOf<Pair<Animal, Animal>>()

                // Count occurrences of each animal
                val animalCounts = adjacentAnimals.groupingBy { it }.eachCount()

                // Add pairs to the uniquePairs list for each animal with a count of 2 or more
                animalCounts.forEach { (animal, count) ->
                    repeat(count / 2) {
                        uniquePairs.add(Pair(animal, animal))
                    }
                }

                when (uniquePairs.size) {
                    1 -> point = 3
                    2 -> point = 5
                    3 -> point = 7
                }
                foxScore += point
            }

        }
    }

    /**
     * Finds a cluster of wildlife tokens of the same type around a given coordinate.
     */
    private fun sameWildlifeTokenNeighbors(
        coordinate: Coordinate,
        grid: MutableMap<Coordinate, HabitatTile>,
        visited: MutableSet<Coordinate>
    ): MutableSet<Coordinate> {
        // get the tile from the map
        val habitatTile = grid[coordinate] ?: return mutableSetOf()

        // If we already visited this coordinate, stop
        if (!visited.add(coordinate)) return mutableSetOf()

        // find neighbors around (coordinate)
        val neighbors = listOf(
            Coordinate(coordinate.first!! - 1, coordinate.second!! + 1),  // right, above
            Coordinate(coordinate.first, coordinate.second!! + 1),       // right
            Coordinate(coordinate.first!! + 1, coordinate.second),       // right, below
            Coordinate(coordinate.first!! + 1, coordinate.second!! - 1), // left, below
            Coordinate(coordinate.first, coordinate.second!! - 1),       // left
            Coordinate(coordinate.first!! - 1, coordinate.second)       // left, above
        )

        val results = mutableSetOf<Coordinate>()

        for (neighbor in neighbors) {
            val neighborTile = grid[neighbor] ?: continue
            // same wildlife token?
            if (habitatTile.wildlifeToken?.animal == neighborTile.wildlifeToken?.animal) {
                results += neighbor
                // recursion
                results += sameWildlifeTokenNeighbors(neighbor, grid, visited)
            }
        }
        return results
    }

    /**
     * Calculates how many wildlife tokens of neighbors are the same.
     */
    private fun numberOfSameWildlifeTokenNeighbor(
        coordinate: Coordinate,
        grid: MutableMap<Coordinate, HabitatTile>
    ): Int {
        // Get current game and check if it is running
        val game = rootService.game
        // Check if the game is running
        checkNotNull(game) { "No game is currently active" }


        val neighbors = listOfNotNull(
            coordinate.first?.let { Coordinate(it - 1, coordinate.second ?: 0 + 1) },
            coordinate.second?.let { Coordinate(coordinate.first ?: 0, it + 1) },
            coordinate.first?.let { Coordinate(it + 1, coordinate.second ?: 0) },
            coordinate.second?.let { Coordinate(coordinate.first ?: 0 + 1, it - 1) },
            coordinate.second?.let { Coordinate(coordinate.first ?: 0, it - 1) },
            coordinate.first?.let { Coordinate(it - 1, coordinate.second ?: 0) }
        )


        if (rootService.habitatTileService.hasNoNeighbor(coordinate, grid)) {
            return 0
        }


        var number = 0
        for (neighbor in neighbors) {
            val currentTile = grid[coordinate]
            val neighborTile = grid[neighbor]


            val currentWildlifeToken = currentTile?.wildlifeToken
            val neighborWildlifeToken = neighborTile?.wildlifeToken

            if (currentWildlifeToken != null &&
                neighborWildlifeToken != null &&
                currentWildlifeToken.animal == neighborWildlifeToken.animal
            ) {
                number++
            }
        }

        return number
    }

    // Function to find all neighboring coordinates for Hawk rule B
    private fun getIsolatedNeighbors(coordinate: Coordinate): List<Coordinate> {
        return listOf(
            Coordinate(coordinate.first!! - 2, coordinate.second!! + 2),
            Coordinate(coordinate.first, coordinate.second!! + 2),
            Coordinate(coordinate.first!! + 2, coordinate.second),
            Coordinate(coordinate.first!! + 2, coordinate.second!! - 2),
            Coordinate(coordinate.first, coordinate.second!! - 2),
            Coordinate(coordinate.first!! - 2, coordinate.second)
        )
    }

    private fun findIsolatedCoupleHawks(
        grid: MutableMap<Coordinate, HabitatTile>,
        visited: MutableSet<Coordinate>
    ): MutableSet<Coordinate> {
        val isolatedHawks = mutableSetOf<Coordinate>()


        // Traverse the grid to identify hawks with no hawk neighbors
        grid.forEach { (coordinate, tile) ->
            val token = tile.wildlifeToken
            if (token?.animal == Animal.HAWK && coordinate !in visited) {
                val neighbors = getIsolatedNeighbors(coordinate)
                val hasHawkNeighbor = neighbors.any() { neighbor ->
                    grid[neighbor]?.wildlifeToken?.animal == Animal.HAWK
                }

                if (hasHawkNeighbor) {
                    isolatedHawks.add(coordinate)
                }

                // Mark the coordinate as visited
                visited.add(coordinate)
            }
        }

        return isolatedHawks
    }
    /** this function will calculate the points for each Habitat corridor */
    fun calculateHabitatCorridors(player: Player) {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate corridors." }

        val visited = mutableSetOf<Coordinate>()
        val grid = player.grid.entries.associate { it.toPair() }

        val playerForestCorridors = mutableListOf<Int>()
        val playerMountainCorridors = mutableListOf<Int>()
        val playerRiverCorridors = mutableListOf<Int>()
        val playerWetlandCorridors = mutableListOf<Int>()
        val playerPrairieCorridors = mutableListOf<Int>()


        grid.keys.forEach { start ->
            if (start !in visited) {
                val corridorTypes = mutableSetOf<HabitatType>()
                val queue = ArrayDeque<Coordinate>()

                queue.add(start)
                visited.add(start)

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    val currentTile = grid[current]
                    if (currentTile == null)
                        continue

                    currentTile.types.filterNotNull().forEach { corridorTypes.add(it) }

                    getNeighborsWithAdjacentEdge(current).forEach { (neighbor, matchingEdge) ->

                        val neighborTile = grid[neighbor]
                        if (neighborTile != null && neighbor !in visited) {
                            if (neighborTile.types[matchingEdge] == currentTile.types[(matchingEdge + 3) % 6]) {
                                queue.add(neighbor)
                                visited.add(neighbor)

                                if (neighborTile.types[matchingEdge] == HabitatType.FOREST) {
                                    if (forestScore == 0) {
                                        forestScore += 2

                                    } else {
                                        forestScore++
                                    }
                                } else if (neighborTile.types[matchingEdge] == HabitatType.MOUNTAIN) {
                                    if (mountainScore == 0) {
                                        mountainScore += 2
                                    } else {
                                        mountainScore++
                                    }
                                } else if (neighborTile.types[matchingEdge] == HabitatType.RIVER) {
                                    if (riverScore == 0) {
                                        riverScore += 2
                                    } else {
                                        riverScore++
                                    }
                                } else if (neighborTile.types[matchingEdge] == HabitatType.PRAIRIE) {
                                    if (prairieScore == 0) {
                                        prairieScore += 2
                                    } else {
                                        prairieScore++
                                    }
                                } else if (neighborTile.types[matchingEdge] == HabitatType.WETLAND) {
                                    if (wetlandScore == 0) {
                                        wetlandScore += 2
                                    } else {
                                        wetlandScore++
                                    }
                                }

                            }
                        }
                    }

                    if (queue.size == 1) {
                        if (forestScore != 0)
                            forestScore =0
                        if (mountainScore != 0)
                            mountainScore = 0
                        if (riverScore != 0)
                            riverScore = 0
                        if (prairieScore != 0)
                            prairieScore = 0
                        if (wetlandScore != 0)
                            wetlandScore = 0
                    }

                    playerForestCorridors.add(forestScore)
                    playerMountainCorridors.add(mountainScore)
                    playerRiverCorridors.add(riverScore)
                    playerPrairieCorridors.add(prairieScore)
                    playerWetlandCorridors.add(wetlandScore)

                }
                playerForestCorridors.add(2)
                playerMountainCorridors.add(2)
                playerRiverCorridors.add(2)
                playerPrairieCorridors.add(2)
                playerWetlandCorridors.add(2)

                corridorTypes.forEach { type ->
                    when (type) {
                        HabitatType.FOREST -> if (habitatScores[type] != playerForestCorridors.max() ) {
                            habitatScores[type] =  playerForestCorridors.max()
                        } else if (habitatScores[type]!! < playerForestCorridors.max()) {
                            habitatScores[type] =  playerForestCorridors.max()
                        }

                        HabitatType.MOUNTAIN -> if (habitatScores[type] != playerMountainCorridors.max()) {
                            habitatScores[type] =  playerMountainCorridors.max()
                        } else if (habitatScores[type]!! < playerMountainCorridors.max()) {
                            habitatScores[type] = playerMountainCorridors.max()
                        }

                        HabitatType.RIVER -> if (habitatScores[type] != playerRiverCorridors.max()) {
                            habitatScores[type] =  playerRiverCorridors.max()
                        } else if (habitatScores[type]!! < playerRiverCorridors.max()) {
                            habitatScores[type] =  playerRiverCorridors.max()
                        }

                        HabitatType.PRAIRIE -> if (habitatScores[type] != playerPrairieCorridors.max()) {
                            habitatScores[type] = playerPrairieCorridors.max()
                        } else if (habitatScores[type]!! < playerPrairieCorridors.max()) {
                            habitatScores[type] = playerPrairieCorridors.max()
                        }

                        HabitatType.WETLAND -> if (habitatScores[type] != playerWetlandCorridors.max()) {
                            habitatScores[type] = playerWetlandCorridors.max()
                        } else if (habitatScores[type]!! < playerWetlandCorridors.max()) {
                            habitatScores[type] = playerWetlandCorridors.max()
                        }
                    }
                }
                forestScore = 0
                mountainScore = 0
                riverScore = 0
                prairieScore = 0
                wetlandScore = 0
                player.habitatCorridorScores.putAll(habitatScores)

            }
        }
    }


    private fun getNeighborsWithAdjacentEdge(coordinate: Coordinate): List<Pair<Coordinate, Int>> {
        return listOf(
            Pair(Coordinate(coordinate.first!! - 1, coordinate.second!! + 1), 3), // Edge 0
            Pair(Coordinate(coordinate.first, coordinate.second!! + 1), 4),      // Edge 1
            Pair(Coordinate(coordinate.first!! + 1, coordinate.second), 5),      // Edge 2
            Pair(Coordinate(coordinate.first!! + 1, coordinate.second!! - 1), 0), // Edge 3
            Pair(Coordinate(coordinate.first, coordinate.second!! - 1), 1),      // Edge 4
            Pair(Coordinate(coordinate.first!! - 1, coordinate.second), 2)       // Edge 5
        )
    }

    private fun calculationOfBonuses() {
        val game = rootService.game
        checkNotNull(game) { "No active game to calculate scores." }

        val players = game.players
        val habitatTypes = HabitatType.values()

        habitatTypes.forEach { habitatType ->
            val corridorScores = players.map { it.habitatCorridorScores[habitatType] ?: 0 }
            val sortedPlayers = players.zip(corridorScores).sortedByDescending { pair -> pair.second }

            when (players.size) {
                1 -> { // Solo game
                    if (corridorScores[0] >= 7) {
                        players[0].habitatCorridorBonuses[habitatType] = 2
                    }
                }
                2 -> { // 2-player game
                    val largestScore = sortedPlayers[0].second
                    val tiesForLargest = sortedPlayers.filter { pair -> pair.second == largestScore }

                    if (tiesForLargest.size > 1) { // Tie for largest
                        tiesForLargest.forEach { pair ->
                            pair.first.habitatCorridorBonuses[habitatType] = 1
                        }
                    } else { // One largest
                        sortedPlayers[0].first.habitatCorridorBonuses[habitatType] = 2
                    }
                }
                3, 4 -> { // 3/4-player game
                    val largestScore = sortedPlayers[0].second
                    val secondLargestScore = sortedPlayers.drop(1).firstOrNull { pair -> pair.second < largestScore }?.second
                    val tiesForLargest = sortedPlayers.filter { pair -> pair.second == largestScore }

                    if (tiesForLargest.size >= 3) { // 3 or more tie for largest
                        tiesForLargest.forEach { pair ->
                            pair.first.habitatCorridorBonuses[habitatType] = 1
                        }
                    } else if (tiesForLargest.size == 2) { // 2 tie for largest
                        tiesForLargest.forEach { pair ->
                            pair.first.habitatCorridorBonuses[habitatType] = 2
                        }
                    } else { // One largest
                        sortedPlayers[0].first.habitatCorridorBonuses[habitatType] = 3
                        if (secondLargestScore != null) {
                            val tiesForSecond = sortedPlayers.filter { pair -> pair.second == secondLargestScore }
                            if (tiesForSecond.size > 1) { // Tie for second place
                                tiesForSecond.forEach { pair ->
                                    pair.first.habitatCorridorBonuses[habitatType] = 0
                                }
                            } else { // One second largest
                                sortedPlayers
                                    .find { pair -> pair.second == secondLargestScore }
                                    ?.first
                                    ?.habitatCorridorBonuses
                                    ?.set(habitatType, 1)
                            }
                        }
                    }
                }
            }
        }
    }
    /** this method will calculate the bonus points  */
    fun getAllBonuses(player: Player) : Int{
        var result = 0
        player.habitatCorridorBonuses.forEach{ it ->
            result += it.value
        }
        return result
    }
}