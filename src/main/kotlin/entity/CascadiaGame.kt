package entity

/**
 * The CascadiaGame class represents the core structure of a Cascadia game.*
 *
 * @param [players] hold players in array
 * @param [currentPlayer] is determined by number
 * @property selectPairs those are the  4 Available Pairs of Animal Tokens
 * and Habitats to be chosen in players turn
 * @property [starterHabitatTiles] Holds the initial habitat tiles assigned to players.
 * @property [habitatTiles] Contains the habitat tiles used during gameplay.
 * Tiles are drawn and added to the player's environment.
 * @property [wildlifeTokens] Stores the wildlife tokens available in the game.
 * Players draft these tokens alongside habitat tiles.
 * @property [scoringCards] Represents the scoring objectives for the game, one for each wildlife type.
 * These are determined randomly during setup.**
 */

class CascadiaGame(
    var players: Array<Player>,
    var currentPlayer: Int = 0,
) {
    var starterHabitatTiles: MutableList<HabitatTile> = mutableListOf()
    //habitatTiles is drawStack for Tiles, wildlifeTokens is a bag for Tokens
    var habitatTiles: MutableList<HabitatTile> = mutableListOf()
    var wildlifeTokens: MutableList<WildlifeToken> = mutableListOf()

    var selectPairs: MutableList<Pair<HabitatTile, WildlifeToken>> = mutableListOf()

    var scoringCards: MutableList<ScoringCard> = mutableListOf()

    init {
        require(players.size in 2..4) { "player must contain between 2 and 4 players." }
    }

    /**
     * Helper function to get the current player in the game.
     */
    fun getCurrentPlayer() = players[currentPlayer]
}
