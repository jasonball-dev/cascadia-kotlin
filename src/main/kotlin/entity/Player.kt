package entity

/**
 * The Player class represents a player of a Cascadia game.*
 *
 * @param [name] The players name.
 * @param [playerType] The players type (LOCAL, NETWORK, BOTEZ, BOTHARD).
 * @property [score] The players current score.
 * @property [natureToken] The number of the players nature tokens.
 * @property [grid] The players habitat tiles grid as map.
 * @property [chosenHabitatTile] The players current chosen [HabitatTile].
 * @property [chosenWildlifeToken] The players current chosen [WildlifeToken].
 */

data class Player(
    val name: String,
    val playerType: PlayerType,
) {
    var score: Int = 0
    var natureToken: Int = 0
    val grid: MutableMap<Coordinate, HabitatTile> = mutableMapOf()
    var chosenHabitatTile: MutableList<HabitatTile> = mutableListOf()
    var chosenWildlifeToken: MutableList<WildlifeToken> = mutableListOf()
    var animalScores = mutableMapOf<Animal,Int>()
    var habitatCorridorScores = mutableMapOf<HabitatType, Int>()
    var habitatCorridorBonuses = mutableMapOf<HabitatType, Int>()
    var usedNatureToken = false

    init {
        require( chosenHabitatTile.size in 0..1){ "chosenHabitatTile must 0 or 1." }
        require( chosenWildlifeToken.size in 0..1){ "chosenWildlifeToken must 0 or 1." }
    }

}
