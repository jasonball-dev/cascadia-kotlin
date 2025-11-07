package entity

/**
 * the class HabitatType defines every single habitat type available in the game so each Habitat can
 * contain different visuals and properties
 */

enum class HabitatType {
    MOUNTAIN,
    FOREST,
    PRAIRIE,
    WETLAND,
    RIVER,
    ;

    override fun toString() =
        when (this) {
            MOUNTAIN -> "M"
            FOREST -> "F"
            PRAIRIE -> "P"
            WETLAND -> "W"
            RIVER -> "R"
        }
}