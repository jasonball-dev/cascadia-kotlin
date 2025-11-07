package gui

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import kotlin.random.Random

/**
 * [MenuScene] that is used to select the scoring cards before starting a game.
 */

class ScoringCardSelectionScene(val rootService: RootService, val cascadiaApplication: CascadiaApplication) :
    MenuScene(1920, 1080), Refreshable {
    // This pane is used to hold all components of the scene and easily center them on the screen

    // player information forwarded from the last Scene
    var playerNames: List<String> = emptyList()
    var playerTypes: List<PlayerType> = emptyList()

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

    // BEARS Start
    val bearsLabel = Label(
        text = "BEAR",
        width = 400, height = 100, posX = 110, posY = 240,
        alignment = Alignment.CENTER_LEFT,
        font = Font(50, Color(0x0000000), "Itim")
    )

    private val scoringCardBearA = Label(
        posX = 110, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/bear-A.jpg")
    )

    private val scoringCardBearB = Label(
        posX = 275, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/bear-B.jpg")
    )

    val boxBearA: CheckBox =
        CheckBox(
            posX = 110,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxBearB.isChecked = true else boxBearB.isChecked = false }}

    val boxBearB: CheckBox =
        CheckBox(
            posX = 275,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxBearA.isChecked = true else boxBearA.isChecked = false } }
    // BEAR End

    // ELK Start
    val elkLabel = Label(
        text = "ELK",
        width = 400, height = 100, posX = 470, posY = 240,
        alignment = Alignment.CENTER_LEFT,
        font = Font(50, Color(0x0000000), "Itim")
    )

    private val scoringCardElkA = Label(
        posX = 470, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/elk-A.jpg")
    )

    private val scoringCardElkB = Label(
        posX = 635, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/elk-B.jpg")
    )

    val boxElkA: CheckBox =
        CheckBox(
            posX = 470,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxElkB.isChecked = true else boxElkB.isChecked = false } }

    val boxElkB: CheckBox =
        CheckBox(
            posX = 635,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxElkA.isChecked = true else boxElkA.isChecked = false } }
    // ELK End

    // SALMON Start
    val salmonLabel = Label(
        text = "SALMON",
        width = 400, height = 100, posX = 830, posY = 240,
        alignment = Alignment.CENTER_LEFT,
        font = Font(50, Color(0x0000000), "Itim")
    )

    private val scoringCardSalmonA = Label(
        posX = 830, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/salmon-A.jpg")
    )

    private val scoringCardSalmonB = Label(
        posX = 995, posY = 320, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/salmon-B.jpg")
    )

    val boxSalmonA: CheckBox =
        CheckBox(
            posX = 830,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxSalmonB.isChecked = true else boxSalmonB.isChecked = false } }

    val boxSalmonB: CheckBox =
        CheckBox(
            posX = 995,
            posY = 470,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxSalmonA.isChecked = true else boxSalmonA.isChecked = false } }
    // SALMON End

    // HAWK Start
    val hawkLabel = Label(
        text = "HAWK",
        width = 1250, height = 100, posX = 255, posY = 505,
        alignment = Alignment.CENTER_LEFT,
        font = Font(50, Color(0x0000000), "Itim")
    )

    private val scoringCardHawkA = Label(
        posX = 255, posY = 585, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/hawk-A.jpg")
    ) //-100 x

    private val scoringCardHawkB = Label(
        posX = 420, posY = 585, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/hawk-B.jpg")
    )

    val boxHawkA: CheckBox =
        CheckBox(
            posX = 255,
            posY = 735,
            alignment = Alignment.CENTER_LEFT
        ).apply {
            onMouseClicked = { if (!isChecked) boxHawkB.isChecked = true else boxHawkB.isChecked = false }
        }

    val boxHawkB: CheckBox =
        CheckBox(
            posX = 420,
            posY = 735,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxHawkA.isChecked = true else boxHawkA.isChecked = false } }
    // HAWK End

    // FOX Start
    val foxLabel = Label(
        text = "FOX",
        width = 1250, height = 100, posX = 700, posY = 505,
        alignment = Alignment.CENTER_LEFT,
        font = Font(50, Color(0x0000000), "Itim")
    )

    private val scoringCardFoxA = Label(
        posX = 700, posY = 585, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/fox-A.jpg")
    )

    private val scoringCardFoxB = Label(
        posX = 865, posY = 585, width = 150, height = 150,
        visual = ImageVisual("images/scoring-cards/fox-B.jpg")
    )

    val boxFoxA: CheckBox =
        CheckBox(
            posX = 700,
            posY = 735,
            alignment = Alignment.CENTER_LEFT
        ).apply {
            onMouseClicked = { if (!isChecked) boxFoxB.isChecked = true else boxFoxB.isChecked = false }
        }

    val boxFoxB: CheckBox =
        CheckBox(
            posX = 865,
            posY = 735,
            alignment = Alignment.CENTER_LEFT
        ).apply { onMouseClicked = { if (!isChecked) boxFoxA.isChecked = true else boxFoxA.isChecked = false } }
    // FOX End

    val randomLabel = Label(
        text = "Random",
        width = 1250, height = 100, posX = 0, posY = 780,
        font = Font(60, Color(0x0000000), "Itim")
    )

    val animalBoxes =
        listOf(boxBearA, boxBearB, boxElkA, boxElkB, boxFoxA, boxFoxB, boxHawkA, boxHawkB, boxSalmonA, boxSalmonB)

    val boxRandom =
        CheckBox(
            posX = 480,
            posY = 820,
            alignment = Alignment.CENTER_LEFT
        ).apply {
            onMouseClicked = {
                if (isChecked)
                    animalBoxes.forEach { animalBox -> animalBox.isChecked = false }
                else
                    animalBoxes.forEachIndexed { index, animalBox ->
                        if (index % 2 == 0)
                            animalBox.isChecked = true
                    }
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
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.newGameScene, 0)
        }
    }

    val startButton = Button(
        text = "Start",
        width = 250,
        height = 100,
        posX = 960,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            val selectedCards = emptyList<Boolean>().toMutableList()
            if (boxRandom.isChecked) {
                repeat(5) {
                    selectedCards.add(Random.nextBoolean())
                }
            } else {
                selectedCards.add(boxBearA.isChecked)
                selectedCards.add(boxElkA.isChecked)
                selectedCards.add(boxFoxA.isChecked)
                selectedCards.add(boxHawkA.isChecked)
                selectedCards.add(boxSalmonA.isChecked)
            }
            rootService.gameService.startGame(playerNames, playerTypes, selectedCards.toList())
        }
    }

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            headlineLabel,
            bearsLabel,
            elkLabel,
            salmonLabel,
            hawkLabel,
            foxLabel,
            randomLabel,
            startButton,
            scoringCardBearA,
            scoringCardBearB,
            scoringCardElkA,
            scoringCardElkB,
            scoringCardHawkA,
            scoringCardHawkB,
            scoringCardSalmonA,
            scoringCardSalmonB,
            scoringCardFoxA,
            scoringCardFoxB,
            boxBearA,
            boxBearB,
            boxElkA,
            boxElkB,
            boxSalmonA,
            boxSalmonB,
            boxHawkA,
            boxHawkB,
            boxFoxA,
            boxFoxB,
            boxRandom,
            backButton
        )
        addComponents(contentPane)
        boxBearA.isChecked = true
        boxElkA.isChecked = true
        boxSalmonA.isChecked = true
        boxHawkA.isChecked = true
        boxFoxA.isChecked = true
    }
}