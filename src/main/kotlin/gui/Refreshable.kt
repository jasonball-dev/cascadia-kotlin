package gui

import service.ConnectionState

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 */

interface Refreshable {

    /** perform refreshes that are necessary after picking a pair */
    fun refreshAfterPickOnePair() {}

    /** perform refreshes that are necessary after rotate habitat tile  */
    fun refreshAfterRotateHabitatTile() {}

    /** perform refreshes that are necessary after discard a wild life token */
    fun refreshAfterDiscardWildlifeToken() {}

    /** perform refreshes that are necessary after using a nature token for replace*/
    fun refreshAfterUseNatureTokenReplace() {}

    /** perform refreshes that are necessary after using a nature token for a free selection */
    fun refreshAfterUseNatureTokenFreeSelect() {}

    /** perform refreshes that are necessary after playing wild life token */
    fun refreshAfterPlayWildlifeToken() {}

    /** perform refreshes that are necessary after playing habitat tile*/
    fun refreshAfterPlayHabitatTile() {}

    /** perform refreshes that are necessary after creating a draw stack for wild life token */
    fun refreshAfterCreateDrawStackWildlifeToken() {}

    /** perform refreshes that are necessary after creating a draw stack for Habitat tile  */
    fun refreshAfterCreateDrawStackHabitatTile() {}

    /** perform refreshes that are necessary after creating a play stack  */
    fun refreshAfterCreatePlayStack() {}

    /** perform refreshes that are necessary after placing the initial habitats */
    fun refreshAfterPlaceInitialHabitats() {}

    /** perform refreshes that are necessary after bot playing wild life token */
    fun refreshAfterBotPlayWildlifeToken(){}

    /** perform refreshes that are necessary after bot playing habitat tile */
    fun refreshAfterBotPlayHabitatTile(){}

    /** perform refreshes that are necessary after bot play */
    fun refreshAfterBotPlayer(){}

    /** perform refreshes that are necessary after bot drawing a pair */
    fun refreshAfterBotDrawPair(){}

    /** perform refreshes that are necessary after a new game started */
    fun refreshAfterStartNewGame() {}

    /** perform refreshes that are necessary after a player starts his/her turn */
    fun refreshAfterStartTurn() {}

    /** perform refreshes that are necessary after a player ends his/her turn */
    fun refreshAfterEndTurn() {}

    /** perform refreshes that are necessary after a player picks tile and token */
    fun refreshAfterPicking(){}

    /** Perform refresh after the connection state changes */
    fun refreshConnectionState(newState: ConnectionState) {}

    /** perform refresh after End Game*/
    fun refreshAfterGameEnd(){}

    /** Perform refresh after removing overpopulation*/
    fun refreshAfterRemoveOverpopulation(){}
}