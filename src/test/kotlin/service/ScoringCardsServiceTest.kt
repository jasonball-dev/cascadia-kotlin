import kotlin.test.*
import entity.*
import service.*

/** class for testing if the score has been working as expected  */
class ScoringCardsServiceTest {

    private lateinit var rootService: RootService
    private lateinit var scoringCardService: ScoringCardService
    private lateinit var player: Player
    private lateinit var grid: MutableMap<Coordinate, HabitatTile>

    /** setting up the environment so the tests can be done  */
    @BeforeTest

    fun setUp() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        player = game.players[game.currentPlayer]
        rootService.game = game


        grid = player.grid

    }

    /** testing if the bear (Type A) score is working properly*/
    @Test
    fun testCalculateBearScoreTypeA() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.BEAR))
        // Add habitat tiles with bears
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 1,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.BEAR), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 2,
            types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE,HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.BEAR), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }

        scoringCardService.calculateScoreForAllPlayers()

        assertEquals(
            4, player.animalScores[Animal.BEAR],
            "Score should be 4 for a pair of bears (Rule A)."
        )
    }


    /** testing if the bear (Type B)score is working properly*/
    @Test
    fun testCalculateBearScoreTypeB() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.BEAR))
        // Add habitat tiles with bears
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 1,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST,HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.BEAR), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 2,
            types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE,HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.BEAR), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }
        grid[Coordinate(2, 0)] = HabitatTile(
            id = 3,
            types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE,HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.BEAR), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }

        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(10, player.animalScores[Animal.BEAR],
            "Score should be 10 for a pair of bears (Rule A).")
    }

    /** testing if the Elk (Type A) score is working properly*/
    @Test
    fun testCalculateElkScoreTypA() {
        // Add habitat tiles with elks in a line
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.ELK))

        grid[Coordinate(0, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }

        grid[Coordinate(1, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }

        grid[Coordinate(2, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }

        grid[Coordinate(3, 0)] = HabitatTile(
            id = 8,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(13, player.animalScores[Animal.ELK],
            "Score should be 13 for a line of two elks (Rule A).")
    }

    /** testing if the ELK (Type B) score is working properly*/
    @Test
    fun testCalculateElkScoreTypB() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.ELK))

        // Add habitat tiles with elks in a line
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 3,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(-1, 1)] = HabitatTile(
            id = 4,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 4,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(13, player.animalScores[Animal.ELK],
            "Score should be 13 for a group of 4 elks (Rule B).")
    }


    /** testing if the Salmon (Type A) score is working properly*/
    @Test
    fun testCalculateSalmonScoreTypA() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.SALMON))
        // Add habitat tiles with salmon in a run
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 6,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        grid[Coordinate(-1, 1)] = HabitatTile(
            id = 6,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }

        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(
            8, player.animalScores[Animal.SALMON],
            "Score should be 8 for a run of two salmons (Rule A)."
        )
    }

    /** testing if the salmon (Type B) score is working properly*/
    @Test
    fun testCalculateSalmonScoreTypB() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.SALMON))
        // Add habitat tiles with salmon in a run
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 6,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        grid[Coordinate(2, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        grid[Coordinate(3, 0)] = HabitatTile(
            id = 6,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.SALMON),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.SALMON)
        }
        scoringCardService.calculateScoreForAllPlayers()

        assertEquals(11, player.animalScores[Animal.SALMON],
            "Score should be 11 for a run of two salmons (Rule B).")
    }


    /** testing if the Hawk (Type A) score is working properly*/
    @Test
    fun testCalculateHawkScoreTypeA() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.HAWK))
        // Add habitat tiles with hawks
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 7,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 10,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.BEAR),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 11,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(3, 0)] = HabitatTile(
            id = 7,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(
            5,
            player.animalScores[Animal.HAWK],
            "Score should be 5 for two hawks with a line of sight (Rule A)."
        )
    }

    /** testing if the Hawk (Type B) score is working properly*/
    @Test
    fun testCalculateHawkScoreTypeB() {
        // Add habitat tiles with hawks
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.HAWK))
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 7,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }

        grid[Coordinate(2, 0)] = HabitatTile(
            id = 8,
            types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        scoringCardService.calculateScoreForPlayer(player)
        assertEquals(
            5,
            player.animalScores[Animal.HAWK],
            "Score should be 5 for two hawks with a line of sight (Rule b)."
        )
    }

    /** testing if the fox (Type A) score is working properly*/
    @Test
    fun testCalculateFoxScoreTypeA() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.FOX))
        // Add habitat tiles with foxes and neighbors
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 9,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.FOX),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 10,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.BEAR),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.BEAR)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 11,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(
            2,
            player.animalScores[Animal.FOX],
            "Score should be 2 for a fox adjacent to two unique animals (Rule A)."
        )
    }

    /** testing if the fox (Type B) score is working properly*/
    @Test
    fun testCalculateFoxScoreTypeB() {
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.FOX))
        // Add habitat tiles with foxes and neighbors
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 12,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.FOX),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 14,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(-1, 0)] = HabitatTile(
            id = 15,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.FOX),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(-1, 1)] = HabitatTile(
            id = 16,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.FOX),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(1, -1)] = HabitatTile(
            id = 17,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK),
            isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(
            5,player.animalScores[Animal.FOX],
            "Score should be 5 for a fox adjacent to two unique animals (Rule B)."
        )
    }

    /** testing if the habitatCorridors  with connected tiles score calculation is working properly (test 1)*/
    @Test
    fun testCalculateHabitatCorridorsWithConnectedTiles() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        player = game.players[game.currentPlayer]
        rootService.game = game
        grid = player.grid

        grid[Coordinate(0, 0)] = HabitatTile(
            id = 12, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken =
                WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 13, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 14, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(3, player.habitatCorridorScores[HabitatType.FOREST])
    }

    /** testing if the habitatCorridors score calculation is working properly */
    @Test
    fun testCalculateHabitatCorridors() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        player = game.players[1]
        rootService.game = game
        grid = player.grid
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 12, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.RIVER, HabitatType.RIVER,
                HabitatType.RIVER, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 13, types = arrayOf(
                HabitatType.FOREST, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 14, types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(-1, 0)] = HabitatTile(
            id = 15, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.WETLAND,
                HabitatType.WETLAND, HabitatType.WETLAND
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(4, player.habitatCorridorScores[HabitatType.FOREST])
    }

    /** testing if Elk Score for a large elk group is working properly */
    @Test
    fun testCalculateElkScoreTypBGrid() {
        // Add habitat tiles with elks in a line
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = false, animal = Animal.ELK))

        grid = player.grid
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }

        grid[Coordinate(-1, 0)] = HabitatTile(
            id = 8,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(0, -2)] = HabitatTile(
            id = 9,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(1, -1)] = HabitatTile(
            id = 8,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(1, -2)] = HabitatTile(
            id = 10,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 6,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForPlayer(player)

        assertEquals(20, player.animalScores[Animal.ELK],
            "Score should be 27 for a group of 4 elks (Rule B).")
    }

    /** testing if Elk score in a single Elk line is working properly  */
    @Test
    fun testCalculateElkAScore_singleElkLine() {
        // Arrange
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        rootService.game = game
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.BEAR))
        player = game.players[game.currentPlayer]

        val grid = player.grid
        // Add habitat tiles with elks in a line
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.ELK))
        grid[Coordinate(0, 0)] = HabitatTile(
            id = 3,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 4,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(2, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        grid[Coordinate(3, 0)] = HabitatTile(
            id = 5,
            types = arrayOf(null, null, null, null, null, null),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        // Assert
        assert(player.animalScores[Animal.ELK] == 13)
        { "Expected score: 13, but was ${player.animalScores[Animal.ELK]}" }
    }


    /**This method tests Bonus points of players.*/
    @Test
    fun testCalculateHabitatCorridorsBonusPoints() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        var game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )

        rootService.game = game
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.BEAR))
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.ELK))
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.FOX))
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.HAWK))
        rootService.game!!.scoringCards.add(ScoringCard(isRuleA = true, animal = Animal.SALMON))
        game = rootService.game!!

        game.players[0].grid[Coordinate(0, 0)] = HabitatTile(
            id = 0, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.MOUNTAIN, HabitatType.WETLAND,
                HabitatType.RIVER, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[0].grid[Coordinate(1, 0)] = HabitatTile(
            id = 1, types = arrayOf(
                HabitatType.FOREST, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        game.players[0].grid[Coordinate(0, 1)] = HabitatTile(
            id = 2, types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[0].grid[Coordinate(-1, 0)] = HabitatTile(
            id = 3, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.WETLAND,
                HabitatType.WETLAND, HabitatType.WETLAND
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[0].grid[Coordinate(-1, 1)] = HabitatTile(
            id = 4, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.WETLAND,
                HabitatType.WETLAND, HabitatType.WETLAND
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[0].grid[Coordinate(1, -1)] = HabitatTile(
            id = 5, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.MOUNTAIN, HabitatType.WETLAND,
                HabitatType.WETLAND, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[0].grid[Coordinate(0, -1)] = HabitatTile(
            id = 6, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        game.players[0].grid[Coordinate(-2, 1)] = HabitatTile(
            id = 7, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.WETLAND, HabitatType.WETLAND,
                HabitatType.WETLAND, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[0].grid[Coordinate(-2, 2)] = HabitatTile(
            id = 8, types = arrayOf(
                HabitatType.FOREST, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }


        game.players[1].grid[Coordinate(0, 0)] = HabitatTile(
            id = 9, types = arrayOf(
                HabitatType.RIVER, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.RIVER, HabitatType.RIVER
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[1].grid[Coordinate(1, 0)] = HabitatTile(
            id = 10, types = arrayOf(
                HabitatType.RIVER, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.RIVER
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        game.players[1].grid[Coordinate(0, 1)] = HabitatTile(
            id = 11, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[1].grid[Coordinate(-1, 0)] = HabitatTile(
            id = 12, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[1].grid[Coordinate(-1, 1)] = HabitatTile(
            id = 13, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        game.players[1].grid[Coordinate(1, -1)] = HabitatTile(
            id = 14, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[1].grid[Coordinate(0, -1)] = HabitatTile(
            id = 15, types = arrayOf(
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        game.players[1].grid[Coordinate(-2, 1)] = HabitatTile(
            id = 16, types = arrayOf(
                HabitatType.FOREST, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.WETLAND, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        game.players[1].grid[Coordinate(-2, 2)] = HabitatTile(
            id = 17, types = arrayOf(
                HabitatType.FOREST, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        scoringCardService.calculateScoreForAllPlayers()

        assertEquals(10, scoringCardService.getAllBonuses(game.players[0]))
        assertEquals(10, scoringCardService.getAllBonuses(game.players[1]))
    }

    /** testing if Habitat score in small grid (2 tile corridor) is working properly */
    @Test
    fun testOfSimpleCorridorWithLengthTwo() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        rootService.game = game
        grid = game.players[0].grid

        grid[Coordinate(0, 0)] = HabitatTile(
            id = 12, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 13, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 14, types = arrayOf(
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(2, game.players[0].habitatCorridorScores[HabitatType.FOREST])
    }

    /** testing if Habitat score in small grid (3 tile corridor) is working properly */
    @Test
    fun testOfSimpleCorridorWithLengthThree() {
        rootService = RootService()
        scoringCardService = ScoringCardService(rootService)
        val game = CascadiaGame(
            arrayOf(
                Player("tuqa", PlayerType.LOCAL),
                Player("mete", PlayerType.LOCAL),
                Player("tom", PlayerType.LOCAL),
                Player("zefei", PlayerType.LOCAL)
            )
        )
        rootService.game = game

        grid = game.players[0].grid

        grid[Coordinate(0, 0)] = HabitatTile(
            id = 12, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.MOUNTAIN,
                HabitatType.MOUNTAIN, HabitatType.MOUNTAIN
            ),
            animals = mutableListOf(Animal.FOX), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.FOX)
        }
        grid[Coordinate(1, 0)] = HabitatTile(
            id = 13, types = arrayOf(
                HabitatType.FOREST, HabitatType.PRAIRIE,
                HabitatType.PRAIRIE, HabitatType.PRAIRIE,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.HAWK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.HAWK)
        }
        grid[Coordinate(0, 1)] = HabitatTile(
            id = 14, types = arrayOf(
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST,
                HabitatType.FOREST, HabitatType.FOREST
            ),
            animals = mutableListOf(Animal.ELK), isKeystone = false
        ).apply {
            wildlifeToken = WildlifeToken(animal = Animal.ELK)
        }
        scoringCardService.calculateScoreForAllPlayers()
        assertEquals(3, game.players[0].habitatCorridorScores[HabitatType.FOREST])
    }
}


