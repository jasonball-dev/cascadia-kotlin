package service

/**
 * Enum to distinguish the different states that occur in networked games, in particular
 * during connection and game setup. Used in [NetworkService].
 */
enum class ConnectionState {

    /**
     * no connection active. initial state at the start of the program and after
     * an active connection was closed.
     */
    DISCONNECTED,

    /**
     * connected to server, but no game started or joined yet
     */
    CONNECTED,

    /**
     *  hostGame request sent to server. waiting for confirmation (i.e. [CreateGameResponse])
     */
    WAITING_FOR_HOST_CONFIRMATION,

    /**
     * host game started. waiting for guest player to join
     */
    WAITING_FOR_GUEST,

    /**
     * joinGame request sent to server. waiting for confirmation (i.e. [JoinGameResponse])
     */
    WAITING_FOR_JOIN_CONFIRMATION,

    /**
     * joined game as a guest and waiting for host to send init message (i.e. [CascadiaGameInitMessage])
     */
    WAITING_FOR_INIT,

    /**
     * Game is running. It is my turn. (and potentially the opponent's as well,
     * as in Cascadia both players are active at once)
     */
    PLAYING_MY_TURN,

    /**
     * Game is running. I did my turn. Waiting for opponent to send their turn.
     */
    WAITING_FOR_OPPONENT,

    /**
     * Game is running. It is either my or some else's turn and wildlife tokens are getting swapped.
     * (By resolving overpopulation etc.)
     */
    SWAPPING_WILDLIFE_TOKENS,

    ;

    /** to string function */
    override fun toString() = when (this) {
        DISCONNECTED -> "Disconnected"
        CONNECTED -> "Connected"
        WAITING_FOR_HOST_CONFIRMATION -> "Waiting for Host Confirmation"
        WAITING_FOR_GUEST -> "Waiting for Guest"
        WAITING_FOR_JOIN_CONFIRMATION -> "Waiting for Join Confirmation"
        WAITING_FOR_INIT -> "Waiting for Initialisation"
        PLAYING_MY_TURN -> "Your turn"
        WAITING_FOR_OPPONENT -> "Waiting for Opponent turn"
        SWAPPING_WILDLIFE_TOKENS -> "Player is swapping wildlife tokens."
    }
}