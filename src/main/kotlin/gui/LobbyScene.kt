package gui

import service.ConnectionState
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/**
 * [MenuScene] that is used as the lobby for joining a game.
 */

class LobbyScene(val rootService: RootService, val cascadiaApplication: CascadiaApplication) :
    MenuScene(1920, 1080),
    Refreshable {
    var playerName = ""

    /** To know which elements have to be shown*/
    fun selectHosting(isHosting: Boolean) {
        startGameButton.isVisible = isHosting
        waitingLabel.isVisible = !isHosting
        backButton.isVisible = isHosting
        quitLobbyButton.isVisible = !isHosting
    }

    // This pane is used to hold all components of the scene and easily center them on the screen
    private val contentPane = Pane<UIComponent>(
        width = 1250,
        height = 1000,
        posX = 1920 / 2 - 1250 / 2,
        posY = 1080 / 2 - 1000 / 2,
        visual = ColorVisual(Color(0x67a7b4))
    )

    private val headlineLabel = Label(
        text = "Cascadia",
        width = 1250,
        height = 125,
        posX = 0,
        posY = 100,
        alignment = Alignment.CENTER,
        font = Font(96, Color(0x0000000), "Jacques Francois Shadow")
    )

    private val hostLabel = Label(
        text = "Host:",
        width = 1250, height = 100, posX = 0, posY = 265,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val sessionIDLabel = Label(
        text = "SessionID:",
        width = 1250, height = 100, posX = 0, posY = 355,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val waitingLabel = Label(
        text = "Waiting for host to start...",
        width = 600, height = 100, posX = 325, posY = 830,
        font = Font(45, Color(0x0000000), "Itim")
    )

    val playerOneLabel = Label(
        text = "P1:",
        width = 400, height = 100, posX = 360, posY = 460,
        alignment = Alignment.CENTER_LEFT,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val oneLabel = Label(
        text = "1.",
        width = 400, height = 100, posX = 300, posY = 460,
        alignment = Alignment.CENTER_LEFT,
        font = Font(30, Color(0x0000000), "Jaini")
    )

    val playerTwoLabel = Label(
        text = "P2:",
        width = 400, height = 100, posX = 360, posY = 625,
        alignment = Alignment.CENTER_LEFT,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val twoLabel = Label(
        text = "2.",
        width = 400, height = 100, posX = 300, posY = 625,
        alignment = Alignment.CENTER_LEFT,
        font = Font(30, Color(0x0000000), "Jaini")
    )

    val playerThreeLabel = Label(
        text = "P3:",
        width = 400, height = 100, posX = 760, posY = 460,
        alignment = Alignment.CENTER_LEFT,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val threeLabel = Label(
        text = "3.",
        width = 400, height = 100, posX = 700, posY = 460,
        alignment = Alignment.CENTER_LEFT,
        font = Font(30, Color(0x0000000), "Jaini")
    )

    val playerFourLabel = Label(
        text = "P4:",
        width = 400, height = 100, posX = 760, posY = 625,
        alignment = Alignment.CENTER_LEFT,
        font = Font(45, Color(0x0000000), "Jaini")
    )

    val fourLabel = Label(
        text = "4.",
        width = 400, height = 100, posX = 700, posY = 625,
        alignment = Alignment.CENTER_LEFT,
        font = Font(30, Color(0x0000000), "Jaini")
    )

    private val backButton: Button = Button(
        text = "Back",
        width = 250,
        height = 100,
        posX = 35,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            playerName = ""
            playerOneLabel.text = "P1:"
            playerTwoLabel.text = "P2:"
            playerThreeLabel.text = "P3:"
            playerFourLabel.text = "P4:"
            hostLabel.text = "Host:"
            sessionIDLabel.text = "SessionID:"
            rootService.networkService.disconnect()
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.joinHostGameScene.selectHosting(true)
            cascadiaApplication.showMenuScene(cascadiaApplication.joinHostGameScene, 0)
        }
    }

    private val quitLobbyButton: Button = Button(
        text = "Quit Lobby",
        width = 290,
        height = 100,
        posX = 35,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            rootService.networkService.disconnect()
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.joinHostGameScene.selectHosting(false)
            cascadiaApplication.showMenuScene(cascadiaApplication.joinHostGameScene, 0)
        }
    }



    override fun refreshConnectionState(newState: ConnectionState) {
        when (newState) {
            ConnectionState.CONNECTED -> {
                playerOneLabel.text += playerName
                if (startGameButton.isVisible)
                    hostLabel.text += playerName
            }

            ConnectionState.DISCONNECTED -> println(newState)
            ConnectionState.WAITING_FOR_HOST_CONFIRMATION -> println(newState)
            ConnectionState.WAITING_FOR_GUEST -> updateLobby()
            ConnectionState.WAITING_FOR_JOIN_CONFIRMATION -> println(newState)
            ConnectionState.WAITING_FOR_INIT -> {
                hostLabel.text = "Host:" + rootService.networkService.client!!.players[0].name
                updateLobby()
            }

            ConnectionState.PLAYING_MY_TURN -> println(newState)
            ConnectionState.WAITING_FOR_OPPONENT -> println(newState)
            ConnectionState.SWAPPING_WILDLIFE_TOKENS -> println(newState)
        }
    }

    private fun updateLobby(){
        sessionIDLabel.text = "SessionID:" + rootService.networkService.client!!.sessionID
        val players = rootService.networkService.client!!.players
        val playerLabels = listOf(playerOneLabel, playerTwoLabel, playerThreeLabel, playerFourLabel)
        playerLabels.forEachIndexed { index, label ->
            if (index < players.size) {
                label.text = "P${index + 1}: ${players[index].name}"
            } else {
                label.text = "P${index + 1}:"
            }
        }
    }

    val startGameButton = Button(
        text = "Start Game",
        width = 300,
        height = 100,
        posX = 475,
        posY = 820,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            rootService.networkService.startNewHostedGame()
        }
    }

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            headlineLabel,
            sessionIDLabel,
            quitLobbyButton,
            waitingLabel,
            hostLabel,
            playerOneLabel,
            playerTwoLabel,
            playerThreeLabel,
            playerFourLabel,
            oneLabel,
            twoLabel,
            threeLabel,
            fourLabel,
            startGameButton,
            backButton
        )
        addComponents(contentPane)
    }
}