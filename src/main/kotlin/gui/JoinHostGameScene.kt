package gui

import entity.Player
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/**
 * [MenuScene] that is used for joining and hosting a game.
 */

class JoinHostGameScene(val rootService: RootService, private val cascadiaApplication: CascadiaApplication) :
    MenuScene(1920, 1080), Refreshable {

    /** To know which elements have to be shown*/
    fun selectHosting(isHosting: Boolean) {
        createLobbyButton.isVisible = isHosting
        joinLobbyButton.isVisible = !isHosting
        sessionIDLabel.isVisible = !isHosting
        sessionIDInput.isVisible = !isHosting
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

    private val yourNameLabel = Label(
        text = "Your Name",
        width = 200, height = 100, posX = 443, posY = 280,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val sessionIDLabel = Label(
        text = "SessionID",
        width = 200, height = 100, posX = 443, posY = 440,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val yourNameInput = TextField(
        width = 300,
        height = 60,
        posX = 475,
        posY = 365,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
    )

    val sessionIDInput = TextField(
        width = 300,
        height = 60,
        posX = 475,
        posY = 525,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
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
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.newGameScene, 0)
        }
    }

    private val joinLobbyButton = Button(
        text = "Join Lobby",
        width = 400,
        height = 100,
        posX = 425,
        posY = 785,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            rootService.networkService.joinGame(
                Player(
                    yourNameInput.text,
                    stringToEnumMap.getOrDefault(playerTypeComboBox.selectedItem, PlayerType.LOCAL)
                ),
                sessionIDInput.text
            )
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.lobbyScene.selectHosting(false)
            cascadiaApplication.lobbyScene.playerName = yourNameInput.text
            cascadiaApplication.showMenuScene(cascadiaApplication.lobbyScene, 0)
        }
    }

    private val stringToEnumMap: Map<String, PlayerType> = mapOf(
        "Player" to PlayerType.LOCAL,
        "Bot (Easy)" to PlayerType.BOTEZ,
        "Bot (Hard)" to PlayerType.BOTHARD
    )

    private val createLobbyButton = Button(
        text = "Create Lobby",
        width = 400,
        height = 100,
        posX = 425,
        posY = 785,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.lobbyScene.selectHosting(true)
            cascadiaApplication.lobbyScene.playerName = yourNameInput.text
            cascadiaApplication.showMenuScene(cascadiaApplication.lobbyScene, 0)
            rootService.networkService.hostGame(
                Player(
                    yourNameInput.text,
                    stringToEnumMap.getOrDefault(playerTypeComboBox.selectedItem, PlayerType.LOCAL)
                )
            )
        }
    }

    private val playerTypeComboBox = ComboBox<String>(
        posX = 516,
        posY = 640,
        width = 220,
        height = 65,
        prompt = "Type"
    ).apply {
        items = mutableListOf("Player", "Bot (Easy)", "Bot (Hard)")
        font = Font(28, Color(0x0000000), "Jaini")
        visual = ColorVisual(Color(0xD9D9D9))
    }

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            headlineLabel,
            yourNameLabel,
            sessionIDLabel,
            backButton,
            yourNameInput,
            sessionIDInput,
            joinLobbyButton,
            createLobbyButton,
            playerTypeComboBox
        )
        addComponents(contentPane)
    }
}