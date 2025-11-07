package gui

import entity.Animal
import entity.HabitatType
import entity.Player
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * [MenuScene] that is used to show the scoreboard of the game.
 */

class ScoreboardScene(val rootService: RootService, val cascadiaApplication: CascadiaApplication) :
    MenuScene(1920, 1080), Refreshable {

    // This pane is used to hold all components of the scene and easily center them on the screen
    private val contentPane = Pane<UIComponent>(
        width = 1250,
        height = 1000,
        posX = 1920 / 2 - 1250 / 2,
        posY = 1080 / 2 - 1000 / 2,
        visual = ColorVisual(Color(0x67a7b4))
    )

    // Start Table
    private var columns = mutableListOf(
        TableColumn<Player>(
            title = "Name",
            width = 190,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.name },
        TableColumn<Player>(
            title = "Bear",
            width = 45,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.animalScores[Animal.BEAR].toString() },
        TableColumn<Player>(
            title = "Elk",
            width = 45,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.animalScores[Animal.ELK].toString() },
        TableColumn<Player>(
            title = "Fox",
            width = 45,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.animalScores[Animal.FOX].toString() },
        TableColumn<Player>(
            title = "Hawk",
            width = 45,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.animalScores[Animal.HAWK].toString() },
        TableColumn<Player>(
            title = "Salmon",
            width = 60,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.animalScores[Animal.SALMON].toString() },
        TableColumn<Player>(
            title = "Animal Total",
            width = 95,
            font = Font(35, Color(0xefffe9), "Itim")
        ) { it.animalScores.values.sum().toString() },
        TableColumn<Player>(
            title = "Mountains",
            width = 75,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorScores[HabitatType.MOUNTAIN].toString() },
        TableColumn<Player>(
            title = "Forests",
            width = 65,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorScores[HabitatType.FOREST].toString() },
        TableColumn<Player>(
            title = "Prairies",
            width = 65,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorScores[HabitatType.PRAIRIE].toString() },
        TableColumn<Player>(
            title = "Wetlands",
            width = 70,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorScores[HabitatType.WETLAND].toString() },
        TableColumn<Player>(
            title = "Rivers",
            width = 65,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorScores[HabitatType.RIVER].toString() },
        TableColumn<Player>(
            title = "Bonus",
            width = 65,
            font = Font(35, Color(0x0000000), "Itim")
        ) { it.habitatCorridorBonuses.values.sum().toString() },
        TableColumn<Player>(
            title = "Habitat Total",
            width = 95,
            font = Font(35, Color(0xefffe9), "Itim")
        ) { (it.habitatCorridorScores.values.sum() + it.habitatCorridorBonuses.values.sum()).toString() },
        TableColumn<Player>(
            title = "Nature Tokens",
            width = 105,
            font = Font(35, Color(0xefffe9), "Itim")
        ) { it.natureToken.toString() },
        TableColumn<Player>(
            title = "Total",
            width = 64,
            font = Font(35, Color(0xDFFF00), "Itim")
        ) { it.score.toString() },
    )

    /* TEST TABLE
    var items = mutableListOf<Player>()

    var testTable = TableView<Player>(
        posX = 25,
        posY = 350,
        width = 1200,
        height = 270,
        columns = columns,
        items = items,
        visual = ColorVisual(Color(0x4d7d87))
    )*/

    // End Table

    private val gameOverLabel = Label(
        text = "GAME OVER",
        width = 1250,
        height = 125,
        posX = 0,
        posY = 50,
        alignment = Alignment.CENTER,
        font = Font(70, Color(0x0000000), "Jacques Francois Shadow")
    )

    private val bear = Label(
        posX = 202, posY = 280, width = 75, height = 75,
        visual = ImageVisual("images/tokens/bear.png")
    )

    private val elk = Label(
        posX = 247, posY = 280, width = 75, height = 75,
        visual = ImageVisual("images/tokens/elk.png")
    )

    private val fox = Label(
        posX = 292, posY = 280, width = 75, height = 75,
        visual = ImageVisual("images/tokens/fox.png")
    )

    private val hawk = Label(
        posX = 337, posY = 280, width = 75, height = 75,
        visual = ImageVisual("images/tokens/hawk.png")
    )

    private val salmon = Label(
        posX = 392, posY = 280, width = 75, height = 75,
        visual = ImageVisual("images/tokens/salmon.png")
    )

    private val mountains = Label(
        posX = 558, posY = 270, width = 65, height = 75,
        visual = ImageVisual("images/tiles/starters/10.png")
    )

    private val forests = Label(
        posX = 630, posY = 270, width = 65, height = 75,
        visual = ImageVisual("images/tiles/starters/50.png")
    )

    private val prairies = Label(
        posX = 695, posY = 270, width = 65, height = 75,
        visual = ImageVisual("images/tiles/starters/30.png")
    )

    private val wetlands = Label(
        posX = 763, posY = 270, width = 65, height = 75,
        visual = ImageVisual("images/tiles/starters/20.png")
    )

    private val rivers = Label(
        posX = 830, posY = 270, width = 65, height = 75,
        visual = ImageVisual("images/tiles/starters/40.png")
    )

    private val natureToken = Label(
        posX = 1080, posY = 290, width = 50, height = 50,
        visual = ImageVisual("images/tokens/nature-token.png")
    )

    private val returnButton: Button = Button(
        text = "Return",
        width = 300,
        height = 100,
        posX = 35,
        posY = 860,
        font = Font(45, Color(0x0000000), "Itim"),
        visual = ColorVisual(Color(0xD9D9D9))
    ).apply {
        onMouseClicked = {
            cascadiaApplication.hideMenuScene()
            cascadiaApplication.showMenuScene(cascadiaApplication.resultScene, 0)
        }
    }

    init {
        background = ColorVisual(Color(0x67A7B4))
        contentPane.addAll(
            gameOverLabel,
            returnButton,
            // testTable,
            bear,
            elk,
            fox,
            hawk,
            salmon,
            mountains,
            forests,
            prairies,
            wetlands,
            rivers,
            natureToken
        )
        addComponents(contentPane)
    }
    /** this function will update the table */
    fun updateTable() {
        val game = rootService.game!!
        val players = game.players

        val rankedPlayers = players.sortedByDescending { it.score }

        val table = TableView<Player>(
            posX = 25,
            posY = 350,
            width = 1200,
            height = 270,
            columns = columns,
            items = rankedPlayers,
            visual = ColorVisual(Color(0x4d7d87))
        )

        contentPane.addAll(table)
    }
}