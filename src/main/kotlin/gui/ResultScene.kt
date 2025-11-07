package gui

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.TextVisual
import java.awt.Color
import kotlin.system.exitProcess

/**
 * [MenuScene] that is used to show the results after the game.
 */
class ResultScene(val rootService: RootService, val cascadiaApplication: CascadiaApplication) :
    MenuScene(1920, 1080), Refreshable {

    // This pane is used to hold all components of the scene and easily center them on the screen
    private val contentPane = Pane<UIComponent>(
        width = 1250,
        height = 1000,
        posX = 1920 / 2 - 1250 / 2,
        posY = 1080 / 2 - 1000 / 2,
        visual = ColorVisual(Color(0x67a7b4))
    )

    private val gameOverLabel = Label(
        text = "GAME OVER",
        width = 1250,
        height = 125,
        posX = 0,
        posY = 160,
        alignment = Alignment.CENTER,
        font = Font(70, Color(0x0000000), "Jacques Francois Shadow")
    )

    private val winnerLabel = Label(
        text = "Winner:",
        width = 1250, height = 100, posX = 0, posY = 435,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val winnerScoreLabel = Label(
        text = "score:",
        width = 1250, height = 100, posX = 0, posY = 480,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val secondLabel = Label(
        text = "Second:",
        width = 400, height = 100, posX = 125, posY = 550,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val secondScoreLabel = Label(
        text = "score:",
        width = 400, height = 100, posX = 125, posY = 595,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val thirdLabel = Label(
        text = "Third:",
        width = 400, height = 100, posX = 725, posY = 550,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val thirdScoreLabel = Label(
        text = "score:",
        width = 400, height = 100, posX = 725, posY = 595,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val fourthLabel = Label(
        text = "Fourth:",
        width = 1250, height = 100, posX = 0, posY = 665,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val fourthScoreLabel = Label(
        text = "score:",
        width = 1250, height = 100, posX = 0, posY = 710,
        font = Font(35, Color(0x0000000), "Itim")
    )

    private val trophyLabel = Label(
        posX = 585, posY = 375, width = 75, height = 75,
        visual = ImageVisual("images/trophy.png")
    )

    val detailsButton = Button(
        text = "Details",
        width = 200,
        height = 75,
        posX = 525,
        posY = 840,
        font = Font(35, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.scoreboardScene, 0)
        }
    }

    private val quitButton: Button = Button(
        text = "Quit",
        width = 300,
        height = 100,
        posX = 35,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            exitProcess(0)
        }
    }

    val playAgainButton = Button(
        text = "Play Again",
        width = 300,
        height = 100,
        posX = 920,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.newGameScene, 0)
        }
    }

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            gameOverLabel,
            winnerLabel,
            winnerScoreLabel,
            secondLabel,
            secondScoreLabel,
            thirdLabel,
            thirdScoreLabel,
            fourthLabel,
            fourthScoreLabel,
            detailsButton,
            quitButton,
            playAgainButton,
            trophyLabel
        )
        addComponents(contentPane)
    }

    /** function will update the ranking after the endgame */
    fun updateRanks() {
        val game = rootService.game!!
        val players = game.players

        val rankedPlayers = players.sortedByDescending { it.score }

        winnerLabel.text = rankedPlayers.getOrNull(0)?.let { "Winner: ${it.name}" } ?: ""
        winnerScoreLabel.text = rankedPlayers.getOrNull(0)?.let { "Score: ${it.score}" } ?: ""

        secondLabel.text = rankedPlayers.getOrNull(1)?.let { "Second: ${it.name}" } ?: ""
        secondScoreLabel.text = rankedPlayers.getOrNull(1)?.let { "Score: ${it.score}" } ?: ""

        thirdLabel.text = rankedPlayers.getOrNull(2)?.let { "Third: ${it.name}" } ?: ""
        thirdScoreLabel.text = rankedPlayers.getOrNull(2)?.let { "Score: ${it.score}" } ?: ""

        fourthLabel.text = rankedPlayers.getOrNull(3)?.let { "Fourth: ${it.name}" } ?: ""
        fourthScoreLabel.text = rankedPlayers.getOrNull(3)?.let { "Score: ${it.score}" } ?: ""
    }
}