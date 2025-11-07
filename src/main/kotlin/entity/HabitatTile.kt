package entity

/**
 * The HabitatTile class represents a single hexagonal tile in the Cascadia game.
 * It contains information about the types of habitats it supports, the animals it allows,
 * and its relationship to neighboring tiles.*
 * @param [types] Includes the type of Habitat tiles.
 * @param [animals] Indicates animals suitable for a habitat tile.
 * @param [isKeystone] Indicates wether the tile is a keystone tile.
 * @property [wildlifeToken] Indicates whether the tile is a keystone.
 * @property [coordinate] The tiles current coordinate as a pair.
 * @property [rotation] The tiles current rotation. 0 (no rotation) to 5 (max rotation).
 */

data class HabitatTile(
    val id: Int,
    val types: Array<HabitatType?>,
    val animals: MutableList<Animal?> = mutableListOf(),
    var isKeystone: Boolean,
) {
    var wildlifeToken: WildlifeToken? = null
    //val coordinate = Pair<Int?, Int?>(null, null)
    var rotation: Int = 0

    init {
        require(types.size ==6) { "HabitatTile must have exactly 6 types." }
        require(animals.size in 1..3) { "HabitatTile must have 1 to 3 animals." }
        require(rotation in 0..5) { "Rotation must be between 0 and 5." }
    }

}