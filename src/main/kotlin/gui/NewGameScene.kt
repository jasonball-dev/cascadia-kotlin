package gui

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import kotlin.system.exitProcess
import java.awt.Color

/**
 * [MenuScene] that is used for starting a new game. It is displayed directly at program start.
 * There is a [startGameButton] and a [exitButton].
 * When pressing the [startGameButton] the user is able to provide the names of both players and
 * start the game with the [startButton]. This screen is also displayed when "new game"
 * or the "return arrow" is clicked.
 */

class NewGameScene(
    val rootService: RootService,
    val cascadiaApplication: CascadiaApplication
) : MenuScene(1920, 1080), Refreshable {


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

    private val playerOneLabel = Label(
        text = "Player 1",
        width = 200, height = 100, posX = 425, posY = 225,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val playerTwoLabel = Label(
        text = "Player 2",
        width = 200, height = 100, posX = 425, posY = 360,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val playerThreeLabel = Label(
        text = "Player 3",
        width = 200, height = 100, posX = 425, posY = 495,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val playerFourLabel = Label(
        text = "Player 4",
        width = 200, height = 100, posX = 425, posY = 630,
        font = Font(42, Color(0x0000000), "Jaini")
    )

    private val randomizeLabel = Label(
        text = "Randomize Playing Order",
        width = 300, height = 100, posX = 495, posY = 820,
        font = Font(30, Color(0x0000000), "Jaini")
    )

    private val playerOneInput = TextField(
        width = 300,
        height = 60,
        posX = 470,
        posY = 315,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
    )

    private val playerTwoInput = TextField(
        width = 300,
        height = 60,
        posX = 470,
        posY = 450,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
    )

    private val playerThreeInput = TextField(
        width = 300,
        height = 60,
        posX = 470,
        posY = 585,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
    )

    private val playerFourInput = TextField(
        width = 300,
        height = 60,
        posX = 470,
        posY = 720,
        font = Font(30, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9)),
    )

    private val hotSeatModeButton: Button = Button(
        text = "HotSeat Mode",
        width = 400,
        height = 100,
        posX = 425,
        posY = 350,
        font = Font(45, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            isVisible = false
            hostGameButton.isVisible = false
            joinGameButton.isVisible = false
            playerOneLabel.isVisible = true
            playerTwoLabel.isVisible = true
            playerThreeLabel.isVisible = true
            playerFourLabel.isVisible = true
            playerOneInput.isVisible = true
            playerTwoInput.isVisible = true
            playerThreeInput.isVisible = true
            playerFourInput.isVisible = true
            quitButton.isVisible = false
            nextButton.isVisible = true
            backButton.isVisible = true
            randomizeLabel.isVisible = true
            playerOneComboBox.isVisible = true
            playerTwoComboBox.isVisible = true
            playerThreeComboBox.isVisible = true
            playerFourComboBox.isVisible = true
            randomizeCheckBox.isVisible = true
        }
    }

    private val joinGameButton = Button(
        text = "Join Game",
        width = 400,
        height = 100,
        posX = 425,
        posY = 485,
        font = Font(45, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.joinHostGameScene.selectHosting(false)
            cascadiaApplication.showMenuScene(cascadiaApplication.joinHostGameScene, 0)
        }
    }

    private val hostGameButton = Button(
        text = "Host Game",
        width = 400,
        height = 100,
        posX = 425,
        posY = 620,
        font = Font(45, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.joinHostGameScene.selectHosting(true)
            cascadiaApplication.showMenuScene(cascadiaApplication.joinHostGameScene, 0)
        }
    }

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
            isVisible = false
            nextButton.isVisible = false
            playerOneLabel.isVisible = false
            playerTwoLabel.isVisible = false
            playerThreeLabel.isVisible = false
            playerFourLabel.isVisible = false
            playerOneInput.isVisible = false
            playerTwoInput.isVisible = false
            playerThreeInput.isVisible = false
            playerFourInput.isVisible = false
            joinGameButton.isVisible = true
            hotSeatModeButton.isVisible = true
            quitButton.isVisible = true
            hostGameButton.isVisible = true
            headlineLabel.isVisible = true
            randomizeLabel.isVisible = false
            playerOneComboBox.isVisible = false
            playerTwoComboBox.isVisible = false
            playerThreeComboBox.isVisible = false
            playerFourComboBox.isVisible = false
            randomizeCheckBox.isVisible = false
        }
    }

    private val nextButton = Button(
        text = "Next",
        width = 250,
        height = 100,
        posX = 900,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        onMouseClicked = {
            val names = listOf(
                playerOneInput.text,
                playerTwoInput.text,
                playerThreeInput.text,
                playerFourInput.text
            )
            val types = listOf(
                stringToEnumMap.getOrDefault(playerOneComboBox.selectedItem, PlayerType.LOCAL),
                stringToEnumMap.getOrDefault(playerTwoComboBox.selectedItem, PlayerType.LOCAL),
                stringToEnumMap.getOrDefault(playerThreeComboBox.selectedItem, PlayerType.LOCAL),
                stringToEnumMap.getOrDefault(playerFourComboBox.selectedItem, PlayerType.LOCAL)
            )
            var playerData = names.zip(types)
            playerData = playerData.filter { it.first.isNotEmpty() }
            if (randomizeCheckBox.isChecked)
                playerData = playerData.shuffled()
            val (newNames, newTypes) = playerData.unzip()
            cascadiaApplication.scoringCardSelectionScene.playerTypes = newTypes
            cascadiaApplication.scoringCardSelectionScene.playerNames = newNames
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.scoringCardSelectionScene, 0)
        }
    }
    private val stringToEnumMap: Map<String, PlayerType> = mapOf(
        "Player" to PlayerType.LOCAL,
        "Bot (Easy)" to PlayerType.BOTEZ,
        "Bot (Hard)" to PlayerType.BOTHARD
    )


    private val quitButton = Button(
        text = "Quit",
        width = 250,
        height = 100,
        posX = 500,
        posY = 870,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            exitProcess(0)
        }
    }

    val playerOneComboBox = ComboBox<String>(
        posX = 830,
        posY = 315,
        width = 220,
        height = 65,
        prompt = "Type"
    ).apply {
        items = mutableListOf("Player", "Bot (Easy)", "Bot (Hard)")
        font = Font(28, Color(0x0000000), "Jaini")
        visual = ColorVisual(Color(0xD9D9D9))
    }

    val playerTwoComboBox = ComboBox<String>(
        posX = 830,
        posY = 450,
        width = 220,
        height = 65,
        prompt = "Type"
    ).apply {
        items = mutableListOf("Player", "Bot (Easy)", "Bot (Hard)")
        font = Font(28, Color(0x0000000), "Jaini")
        visual = ColorVisual(Color(0xD9D9D9))
    }

    val playerThreeComboBox = ComboBox<String>(
        posX = 830,
        posY = 585,
        width = 220,
        height = 65,
        prompt = "Type"
    ).apply {
        items = mutableListOf("Player", "Bot (Easy)", "Bot (Hard)")
        font = Font(28, Color(0x0000000), "Jaini")
        visual = ColorVisual(Color(0xD9D9D9))
    }

    val playerFourComboBox = ComboBox<String>(
        posX = 830,
        posY = 720,
        width = 220,
        height = 65,
        prompt = "Type"
    ).apply {
        items = mutableListOf("Player", "Bot (Easy)", "Bot (Hard)")
        font = Font(28, Color(0x0000000), "Jaini")
        visual = ColorVisual(Color(0xD9D9D9))
    }

    val randomizeCheckBox =
        CheckBox(
            posX = 469,
            posY = 853,
            alignment = Alignment.CENTER_LEFT
        )

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            headlineLabel,
            hotSeatModeButton,
            joinGameButton,
            hostGameButton,
            quitButton,
            nextButton,
            backButton,
            playerOneLabel,
            playerTwoLabel,
            playerThreeLabel,
            playerFourLabel,
            playerOneInput,
            playerTwoInput,
            playerThreeInput,
            playerFourInput,
            randomizeLabel,
            playerOneComboBox,
            playerTwoComboBox,
            playerThreeComboBox,
            playerFourComboBox,
            randomizeCheckBox
        )
        addComponents(contentPane)
        nextButton.isVisible = false
        backButton.isVisible = false
        playerOneLabel.isVisible = false
        playerTwoLabel.isVisible = false
        playerThreeLabel.isVisible = false
        playerFourLabel.isVisible = false
        playerOneInput.isVisible = false
        playerTwoInput.isVisible = false
        playerThreeInput.isVisible = false
        playerFourInput.isVisible = false
        randomizeLabel.isVisible = false
        playerOneComboBox.isVisible = false
        playerTwoComboBox.isVisible = false
        playerThreeComboBox.isVisible = false
        playerFourComboBox.isVisible = false
        randomizeCheckBox.isVisible = false
    }
}