package gui

import entity.*
import service.ConnectionState
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.DEFAULT_HEXAGON_SIZE
import tools.aqua.bgw.core.DEFAULT_TOKEN_HEIGHT
import tools.aqua.bgw.core.DEFAULT_TOKEN_WIDTH
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import java.awt.Color

/**
 *  A [BoardGameScene] that represents the main scene of the game where players can perform actions after
 *  the game has started. This class is responsible for displaying the current player's grid and the pair of
 *  tiles/tokens that can be selected. Furthermore, the current player will be able to perform his/her player actions
 *  on the grid.
 */

class GameScene(val rootService: RootService, val cascadiaApplication: CascadiaApplication) :
    BoardGameScene(1920, 1080), Refreshable {
    private var selectedTile: HexagonView? = null
        set(value) {
            if (field != null && field?.parent != null) field!!.removeFromParent()
            field = value
        }
    private var selectedToken: TokenView? = null
        set(value) {
            if (field != null && field?.parent != null) field!!.removeFromParent()
            field = value
        }
    private var natureToken: Boolean = false

    private var selectedSpot: HexagonView? = null
    private var selectedCoordinate: Coordinate? = null

    private var occupied: MutableMap<Coordinate, Boolean> = mutableMapOf()
    private var isPlayingHabitatTile = false
    private var isPlayingWildLifeToken = false

    private var replaceTokens: MutableList<Pair<HabitatTile, WildlifeToken>> = mutableListOf()
    private var isReplaceSelectionMode = false

    private val hexagonGrid: HexagonGrid<HexagonView> = HexagonGrid<HexagonView>(
        coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL, posX = 889, posY = 330
    ).apply { scale = 0.6 }

    private val habitatTileSelection: LinearLayout<HexagonView> by lazy {
        LinearLayout<HexagonView>(
            posX = 650,
            posY = 790,
            width = DEFAULT_HEXAGON_SIZE * 6,
            height = DEFAULT_HEXAGON_SIZE,
            spacing = DEFAULT_HEXAGON_SIZE,
            visual = ColorVisual(Color(255, 255, 255, 0))
        ).apply {
            onAdd = {
                this.onMouseEntered = {
                    if (selectedTile != this) this.scale = 0.7
                }
                this.onMouseExited = {
                    if (selectedTile != this) this.scale = 0.5
                }
                this.onMouseClicked = {
                    tokenSelection.components.forEach { token ->
                        token.isVisible = true
                        token.scale = 1.5
                    }
                    selectedTile?.apply {
                        scale = 0.5
                    }
                    selectedTile = this
                    this.scale = 0.7
                    if (!natureToken) {
                        val tokenPartner = tokenSelection.components[habitatTileSelection.components.indexOf(this)]
                        for (token in tokenSelection.components) {
                            if (token != tokenPartner) token.isVisible = false
                        }
                        selectedToken = tokenPartner
                        tokenPartner.scale = 2.0
                    }
                }
            }

            onRemove = {
                this.scale = 0.6
                tokenSelection.components.forEach { component -> component.isVisible = true }
            }
        }
    }


    private val tokenSelection: LinearLayout<TokenView> by lazy {
        LinearLayout<TokenView>(
            posX = 725,
            posY = 965,
            width = DEFAULT_HEXAGON_SIZE * 6,
            height = DEFAULT_HEXAGON_SIZE,
            spacing = DEFAULT_HEXAGON_SIZE - 17,
            visual = ColorVisual(Color(255, 255, 255, 0))
        ).apply {
            onAdd = {
                scale = 1.5
                this.onMouseClicked = {
                    val index = tokenSelection.components.indexOf(this)
                    val game = rootService.game!!

                    if (isReplaceSelectionMode) {
                        val pair = game.selectPairs[index]

                        replaceTokens.add(pair)

                        this.scale = 2.0
                    } else {
                        selectedToken?.apply {
                            scale = 1.5
                        }
                        selectedToken = this
                        this.scale = 2.0
                    }
                }
            }
        }
    }

    private val playerLabel = Label(
        text = "Current: X",
        font = Font(50, Color(0x0000000), "Itim"),
        width = 500,
        height = 100,
        posX = 1400,
        posY = 0
    )

    private val useNatureTokenButton = Button(
        text = "Free Select (Nature Token)",
        width = 270,
        height = 60,
        posX = 770 + 2.75 * DEFAULT_HEXAGON_SIZE,
        posY = 740,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        onMouseClicked = {
            if (!natureToken) {
                tokenSelection.components.forEach { component -> component.isVisible = true }
                natureToken = true
            } else {
                val tokenPartner = tokenSelection.components[habitatTileSelection.components.indexOf(selectedTile)]
                for (token in tokenSelection.components) {
                    if (token != tokenPartner) token.isVisible = false
                }
                natureToken = false
            }
        }
    }

    private val useNatureTokenReplaceButton = Button(
        text = "Replace Tokens (Nature Token)",
        width = 300,
        height = 60,
        posX = 555,
        posY = 740,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        onMouseClicked = {
            if (!isReplaceSelectionMode) {
                text = "Replace selection"
                isReplaceSelectionMode = true
                replaceTokens.clear()
            } else {
                text = "Replace Tokens (Nature Token)"
                isReplaceSelectionMode = false
                rootService.playerActionService.useNatureTokenReplace(replaceTokens)
            }
        }
    }

    private val pickButton = Button(
        text = "Pick pair",
        width = 180,
        height = 60,
        posX = 725 + 1.35 * DEFAULT_HEXAGON_SIZE,
        posY = 740,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        onMouseClicked = {
            if (!natureToken) {
                selectedTile?.let { tile ->
                    val pair = rootService.game!!.selectPairs.find { it.first.id == tile.name.toInt() }
                    if (pair != null) rootService.playerActionService.pickOnePair(pair)
                }
            } else {
                selectedTile?.let { tile ->
                    val selectedTokenName = selectedToken?.name?.uppercase()
                    if (selectedTokenName != null) {
                        val pair = rootService.game?.selectPairs?.find { it.first.id == tile.name.toInt() }
                        val pair2 = rootService.game?.selectPairs?.find {
                            it.second.animal == Animal.valueOf(selectedTokenName)
                        }
                        if (pair != null && pair2 != null) {
                            rootService.playerActionService.useNatureTokenFreeSelect(pair, pair2)
                        }
                    }
                }
            }
        }
    }

    private val removeOverpopulationButton = Button(
        text = "Remove Overpopulation",
        width = 250,
        height = 60,
        posX = 1500,
        posY = 750,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        onMouseClicked = {
            rootService.gameService.removeOverPopulationThree()
        }
    }

    override fun refreshAfterRemoveOverpopulation() {
        updateSelection()
        removeOverpopulationButton.isVisible = false
    }

    private val endTurnButton = Button(
        text = "End Turn",
        width = 180,
        height = 60,
        posX = useNatureTokenButton.posX + 300,
        posY = useNatureTokenButton.posY + 100,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        isVisible = false
        onMouseClicked = {
            rootService.gameService.endTurn()
        }
    }

    private val discardWildlifeTokenButton = Button(
        text = "Discard Wildlife Token",
        width = 220,
        height = 60,
        posX = 1500,
        posY = 550,
        font = Font(24, Color(0x0000000), "Jaini"),
        visual = ColorVisual(Color(0xA0A0A0))
    ).apply {
        isVisible = false
        onMouseClicked = {
            rootService.playerActionService.discardWildlifeToken(
                rootService.game!!.getCurrentPlayer().chosenWildlifeToken.removeFirst()
            )
        }
    }

    private val leftTilesLabel = Label(
        posX = 20, posY = 900, width = DEFAULT_HEXAGON_SIZE, height = DEFAULT_HEXAGON_SIZE, visual = CompoundVisual(
            ImageVisual("images/tiles/empty.png"),
            TextVisual("82", font = Font(35, Color(0x0000000), "Jaini")),
        )
    )

    private val leftTokens = Label(
        posX = 20 + DEFAULT_HEXAGON_SIZE / 4,
        posY = 900 - DEFAULT_HEXAGON_SIZE * 0.65,
        width = DEFAULT_TOKEN_WIDTH,
        height = DEFAULT_TOKEN_HEIGHT,
        visual = CompoundVisual(
            ImageVisual("images/tokens/empty.png"),
            TextVisual("100", font = Font(35, Color(0x0000000), "Jaini")),
        )
    )

    private val scoringCardBear = Label(
        posX = 20,
        posY = 20,
        width = 250,
        height = 250,
        visual = ImageVisual("images/scoring-cards/bear-A.jpg")
    )
    private val scoringCardElk = Label(
        posX = 20,
        posY = 280,
        width = 250,
        height = 250,
        visual = ImageVisual("images/scoring-cards/elk-A.jpg")
    )

    private val scoringCardFox = Label(
        posX = 20,
        posY = 540,
        width = 250,
        height = 250,
        visual = ImageVisual("images/scoring-cards/fox-A.jpg")
    )

    private val scoringCardHawk = Label(
        posX = 280,
        posY = 145,
        width = 250,
        height = 250,
        visual = ImageVisual("images/scoring-cards/hawk-B.jpg")
    )

    private val scoringCardSalmon = Label(
        posX = 280,
        posY = 405,
        width = 250,
        height = 250,
        visual = ImageVisual("images/scoring-cards/salmon-B.jpg")
    )

    private val availableNatureTokens = Label(
        text = "4",
        font = Font(35, Color(0x0000000), "Jaini"),
        posX = 20 + DEFAULT_HEXAGON_SIZE,
        posY = 850
    )

    private val natureTokenLabel = Label(
        visual = ImageVisual("images/tokens/nature-token.png"),
        height = 60,
        width = 60,
        posX = 52 + DEFAULT_HEXAGON_SIZE,
        posY = 890
    )
    private val viewToTile = BidirectionalMap<HexagonView, HabitatTile>()

    init {
        background = ColorVisual(Color(0x67A7B4))

        //it is not possible to include the hexagonGrid in the contentPane :,(
        addComponents(
            hexagonGrid,
            playerLabel,
            habitatTileSelection,
            tokenSelection,
            useNatureTokenButton,
            leftTilesLabel,
            leftTokens,
            scoringCardBear,
            scoringCardElk,
            scoringCardFox,
            scoringCardHawk,
            scoringCardSalmon,
            availableNatureTokens,
            natureTokenLabel,
            pickButton,
            discardWildlifeTokenButton,
            useNatureTokenReplaceButton,
            endTurnButton,
            removeOverpopulationButton
        )

        for (q in -3..3) {
            for (r in -3..3) {
                if (q + r >= -3 && q + r <= 3) {
                    val hexagon = HexagonView(visual = ImageVisual("images/tiles/empty.png")).apply {
                        onMouseClicked = {
                            selectedSpot = this
                            selectedCoordinate = Coordinate(q, r)
                            if (isPlayingHabitatTile) rootService.playerActionService.playHabitatTile(Coordinate(q, r))
                            if (isPlayingWildLifeToken) rootService.playerActionService.playWildlifeToken(
                                Coordinate(
                                    q, r
                                )
                            )
                        }
                    }
                    hexagonGrid.add(hexagon)
                    hexagonGrid[q, r] = hexagon
                }
            }
        }
    }


    override fun refreshAfterStartNewGame() {
        val game = rootService.game
        checkNotNull(game) { "No started game found" }
        setupCards(game)
        updateSelection()
        updatePlayer()
    }

    override fun refreshAfterStartTurn() {
        val game = rootService.game
        checkNotNull(game) { "No started game found" }
        updatePlayer()
        updateSelection()
    }

    override fun refreshAfterPickOnePair() {
        if (selectedTile == null || selectedToken == null) {
            selectedTile =
                viewToTile.backward(rootService.game!!.getCurrentPlayer().chosenHabitatTile.first())
            selectedToken =
                viewToToken.backward(rootService.game!!.getCurrentPlayer().chosenWildlifeToken.first())
        }
        selectedTile?.let {
            if (it.parent != null) {
                it.removeFromParent()
            }
        }
        selectedToken?.let {
            if (it.parent != null) {
                it.removeFromParent()
            }
        }

        addComponents(selectedTile!!)
        addComponents(selectedToken!!)

        selectedTile!!.posX = 1500.0
        selectedTile!!.posY = 300.0
        selectedToken!!.posX = 1575.0
        selectedToken!!.posY = 470.0
        habitatTileSelection.isDisabled = true
        tokenSelection.isDisabled = true
        selectedTile!!.isDisabled = true
        isPlayingHabitatTile = true
        selectedToken!!.scale = 1.5
        pickButton.isVisible = false
        useNatureTokenButton.isVisible = false
        useNatureTokenReplaceButton.isVisible = false
        removeOverpopulationButton.isVisible = false
    }

    override fun refreshAfterPlayHabitatTile() {
        val currentPlayer = rootService.game!!.players[rootService.game!!.currentPlayer]
        if (selectedSpot == null) selectedSpot = hexagonGrid[0, 0]
        playAnimation(
            MovementAnimation.toComponentView(
                componentView = selectedTile!!, toComponentViewPosition = selectedSpot!!, scene = this, duration = 150
            ).apply {
                onFinished = {
                    if(selectedTile?.parent!=null ) {
                        selectedTile!!.removeFromParent()
                    }
                    updateGrid()
                    discardWildlifeTokenButton.isVisible = !pickButton.isVisible
                }
            })
        isPlayingHabitatTile = false
        val turnedSpot = selectedSpot!!
        selectedSpot!!.apply {
            onMouseClicked = {
                rootService.playerActionService.rotateHabitatTile(
                    currentPlayer.grid.entries.find { it.value.id == currentPlayer.chosenHabitatTile.first().id }!!.key
                )
            }
        }
        selectedToken!!.apply {
            onMouseClicked = {
                scale = 2.0
                isPlayingWildLifeToken = true
                turnedSpot.apply {
                    onMouseClicked = {
                        selectedSpot = this
                        selectedCoordinate = getCoordinates(this)
                        if (isPlayingHabitatTile)
                            rootService.playerActionService.playHabitatTile(selectedCoordinate!!)
                        if (isPlayingWildLifeToken)
                            rootService.playerActionService.playWildlifeToken(selectedCoordinate!!)
                    }
                }
            }
        }
    }

    override fun refreshAfterPlayWildlifeToken() {
        selectedToken!!.removeFromParent()
        isPlayingWildLifeToken = false
        updateGrid()
        endTurnButton.isVisible = !rootService.isNetworkGame()
        discardWildlifeTokenButton.isVisible = false
    }

    override fun refreshAfterRotateHabitatTile() {
        updateGrid()
    }

    override fun refreshAfterDiscardWildlifeToken() {
        discardWildlifeTokenButton.isVisible = false
        isPlayingWildLifeToken = false
        selectedToken!!.removeFromParent()
        endTurnButton.isVisible = !rootService.isNetworkGame()
    }

    override fun refreshAfterUseNatureTokenFreeSelect() {
        updatePlayer()
        natureToken = false
        refreshAfterPickOnePair()
    }

    override fun refreshAfterUseNatureTokenReplace() {
        updatePlayer()
        replaceTokens.clear()
        updateSelection()
    }

    override fun refreshAfterEndTurn() {
        updatePlayer()
        updateSelection()
        updateGrid()
        endTurnButton.isVisible = false
        pickButton.isVisible = true
        tokenSelection.isDisabled = false
        habitatTileSelection.isDisabled = false
        discardWildlifeTokenButton.isVisible = false
    }

    override fun refreshAfterGameEnd() {
        println("in overrife game end")
        cascadiaApplication.resultScene.updateRanks()
        cascadiaApplication.scoreboardScene.updateTable()
    }

    private fun updatePlayer() {
        val game = rootService.game!!
        val currentPlayer = game.players[game.currentPlayer]
        playerLabel.text = "Current: ${currentPlayer.name}"
        leftTilesLabel.visual = CompoundVisual(
            ImageVisual("images/tiles/empty.png"),
            TextVisual("${game.habitatTiles.size}", font = Font(35, Color(0x0000000), "Jaini"))
        )
        leftTokens.visual = CompoundVisual(
            ImageVisual("images/tokens/empty.png"),
            TextVisual("${game.wildlifeTokens.size}", font = Font(35, Color(0x0000000), "Jaini"))
        )
        availableNatureTokens.text = "${currentPlayer.natureToken}"
        for (tile in currentPlayer.grid.keys) occupied[tile] = true
        useNatureTokenButton.isVisible = currentPlayer.natureToken > 0
        useNatureTokenReplaceButton.isVisible = currentPlayer.natureToken > 0
        removeOverpopulationButton.isVisible = rootService.gameService.hasOverPopulationThree()
        updateGrid()
    }

    private fun updateGrid() {
        val game = rootService.game
        checkNotNull(game) { "No started game found" }
        for (q in -3..3) {
            for (r in -3..3) {
                if (q + r in -3..3) {
                    val tile = game.players[game.currentPlayer].grid[Coordinate(q, r)]
                    val tileAndToken = mutableListOf<SingleLayerVisual>()
                    if (tile != null) {
                        val isStarter = (q to r) == (0 to 0) || (q to r) == (0 to 1) || (q to r) == (-1 to 1)

                        if (isStarter) {
                            tileAndToken.add(ImageVisual("images/tiles/starters/${tile.id}.png"))
                            tile.wildlifeToken?.animal?.name?.lowercase()?.let { animalName ->
                                tileAndToken.add(ImageVisual("images/tokens/$animalName.png"))
                            }
                            // for no reason at all coordinates have to be switched
                            hexagonGrid[q, r]?.visual = CompoundVisual(tileAndToken)
                        } else {
                            // Normal tile
                            tileAndToken.add(ImageVisual("images/tiles/${tile.id}.png"))
                            tile.wildlifeToken?.animal?.name?.lowercase()?.let { animalName ->
                                tileAndToken.add(ImageVisual("images/tokens/$animalName.png"))
                            }

                            hexagonGrid[q, r]?.visual = CompoundVisual(tileAndToken)
                            hexagonGrid[q, r]?.rotation = (tile.rotation * 60).toDouble()
                        }

                    } else {
                        tileAndToken.add(ImageVisual("images/tiles/empty.png"))
                        hexagonGrid[q, r]?.visual = CompoundVisual(tileAndToken)
                        hexagonGrid[q, r]?.rotation = 0.0
                    }
                }
            }
        }
    }

    private val viewToToken = BidirectionalMap<TokenView, WildlifeToken>()


    private fun updateSelection() {
        habitatTileSelection.clear()
        tokenSelection.clear()
        viewToTile.clear()
        viewToToken.clear()
        val game = rootService.game ?: return

        for ((tile, token) in game.selectPairs) {

            val tileView = HexagonView(
                visual = ImageVisual("images/tiles/${tile.id}.png")
            ).apply {
                scale = 0.5
                name = tile.id.toString()

                onMouseClicked = {
                    if (isReplaceSelectionMode) {
                        this.scale = 0.4
                    } else {
                        selectedTile?.scale = 0.5
                        selectedTile = this
                        this.scale = 0.7
                        val index = habitatTileSelection.components.indexOf(this)
                        if (!natureToken) {
                            tokenSelection.components.forEachIndexed { i, t ->
                                t.isVisible = (i == index)
                            }
                        }
                    }
                }
            }
            viewToTile.add(tileView to tile)
            val tokenView = TokenView(
                visual = ImageVisual("images/tokens/${token.animal.name.lowercase()}.png")
            ).apply {
                scale = 1.5
                name = token.animal.name.lowercase()

                onMouseClicked = {
                    if (isReplaceSelectionMode) {
                        replaceTokens.add(tile to token)
                        this.scale = 2.0
                    } else {
                        selectedToken?.scale = 1.5
                        selectedToken = this
                        this.scale = 2.0
                    }
                }
            }
            viewToToken.add(tokenView to token)
            habitatTileSelection.add(tileView)
            tokenSelection.add(tokenView)
        }
    }

    override fun refreshAfterBotDrawPair() {
        val game = rootService.game
        requireNotNull(game)
        val currentPlayer = game.players[game.currentPlayer]

        for (habitatSelect in habitatTileSelection) {
            if (habitatSelect.name.toInt() == currentPlayer.chosenHabitatTile.first().id) {
                selectedTile = habitatSelect
                selectedToken = tokenSelection.components[
                    habitatTileSelection.components.indexOf(habitatSelect)
                ]
            }
        }
    }

    override fun refreshAfterBotPlayHabitatTile() {
        val currentPlayer = rootService.game!!.players[rootService.game!!.currentPlayer]
        // last key added to the grid in service
        val coordinate = currentPlayer.grid.keys.last()
        for (spot in hexagonGrid.components) {
            if (getCoordinates(spot) == coordinate) {
                selectedSpot = spot
                selectedCoordinate = coordinate

            }
        }
        if (selectedTile != null && selectedTile!!.parent != null) selectedTile!!.removeFromParent()
        updateGrid()
    }

    override fun refreshAfterBotPlayWildlifeToken() {
        // last key added to the grid in service
        requireNotNull(rootService.game)
        val currentPlayer = rootService.game!!.players[rootService.game!!.currentPlayer]
        for (habitat in currentPlayer.grid) {
            // problematic
            if (habitat.value.wildlifeToken?.equals(currentPlayer.chosenWildlifeToken) == true) {
                for (spot in hexagonGrid.components) {
                    if (getCoordinates(spot) == habitat.key) {
                        selectedSpot = spot
                        selectedCoordinate = habitat.key
                    }
                }
            }
        }
        if (selectedToken != null && selectedToken!!.parent != null) selectedToken!!.removeFromParent()
        updateGrid()
    }
    override fun refreshAfterBotPlayer() {
        pickButton.isDisabled = true
        removeOverpopulationButton.isDisabled = true
        useNatureTokenReplaceButton.isDisabled = true
        useNatureTokenButton.isDisabled = true
        playAnimation(DelayAnimation(5000).apply {
            onFinished = {
                pickButton.isDisabled = false
                removeOverpopulationButton.isDisabled = false
                useNatureTokenReplaceButton.isDisabled = false
                useNatureTokenButton.isDisabled = false
                if(!rootService.isNetworkGame()) rootService.gameService.endTurn() // this check
            }
        })
    }

    private fun setupCards(game: CascadiaGame) {
        for (card in game.scoringCards) {
            when (card.animal) {
                Animal.BEAR -> scoringCardBear.visual =
                    if (card.isRuleA) ImageVisual("images/scoring-cards/bear-A.jpg")
                    else ImageVisual("images/scoring-cards/bear-B.jpg")

                Animal.ELK -> scoringCardElk.visual =
                    if (card.isRuleA) ImageVisual("images/scoring-cards/elk-A.jpg")
                    else ImageVisual("images/scoring-cards/elk-B.jpg")

                Animal.FOX -> scoringCardFox.visual =
                    if (card.isRuleA) ImageVisual("images/scoring-cards/fox-A.jpg")
                    else ImageVisual("images/scoring-cards/fox-B.jpg")

                Animal.HAWK -> scoringCardHawk.visual =
                    if (card.isRuleA) ImageVisual("images/scoring-cards/hawk-A.jpg")
                    else ImageVisual("images/scoring-cards/hawk-B.jpg")

                Animal.SALMON -> scoringCardSalmon.visual =
                    if (card.isRuleA) ImageVisual("images/scoring-cards/salmon-A.jpg")
                    else ImageVisual("images/scoring-cards/salmon-B.jpg")
            }
        }
    }

    private fun getCoordinates(hexagon: HexagonView): Coordinate {
        for (q in -3..3) {
            for (r in -3..3) {
                if (q + r in -3..3) { // Valid axial coordinate
                    if (hexagonGrid[q, r] == hexagon) {
                        return Coordinate(q, r) // Found the coordinates
                    }
                }
            }
        }
        return Coordinate(0, 0)
    }

    override fun refreshConnectionState(newState: ConnectionState) {
        when (newState) {
            ConnectionState.PLAYING_MY_TURN -> {
                pickButton.isDisabled = false
                useNatureTokenButton.isDisabled = false
                useNatureTokenReplaceButton.isDisabled = false
                discardWildlifeTokenButton.isVisible = false
                removeOverpopulationButton.isDisabled = false
            }

            ConnectionState.WAITING_FOR_OPPONENT -> {
                pickButton.isDisabled = true
                removeOverpopulationButton.isDisabled = true
                useNatureTokenButton.isDisabled = true
                useNatureTokenReplaceButton.isDisabled = true
                discardWildlifeTokenButton.isVisible = false
            }

            ConnectionState.SWAPPING_WILDLIFE_TOKENS -> TODO()
            else -> {}
        }
    }

}