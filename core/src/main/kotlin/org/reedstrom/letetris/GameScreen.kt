package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport

class GameScreen(private val game: GameMain) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val viewport: FitViewport = FitViewport(1f, 1f, camera) // Initialize with placeholder dimensions

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply {
        data.setScale(1.5f)
    }

    private val isAndroid = Gdx.app.type.name == "Android"

    private val gameState = GameState(10f, 20f).apply {
        onDimensionsChanged = { updateViewport() }
    }

    private val textCamera = OrthographicCamera().apply {
        setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    }

    init {
        // Initialize viewport with actual dimensions after GameState is fully initialized
        updateViewport()
    }

    private fun updateViewport() {
        viewport.worldWidth = gameState.worldWidth
        viewport.worldHeight = gameState.worldHeight - 5f
        camera.setToOrtho(false, gameState.worldWidth, gameState.worldHeight -5f)
    }

    override fun show() {
        viewport.apply()
        Gdx.input.inputProcessor = InputHandler(gameState)
        Controllers.addListener(ControllerHandler(gameState))
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        textCamera.setToOrtho(false, width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        viewport.apply()
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        game.batch.projectionMatrix = camera.combined

        // Clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Check if the active screen has changed
        if (gameState.activeScreen == "ConfigScreen") {
            game.setScreen(ConfigScreen(game, gameState))
            return
        } else if (gameState.activeScreen == "CreditsScreen") {
            game.setScreen(CreditsScreen(game))
            return
        }

        drawGameBoard()

        if (gameState.waitingForStart) {
            drawGetReadyOverlay()
            return
        }

        if (gameState.gameOver) {
            drawGameOverOverlay()
            return
        }

        gameState.tick(delta)
    }

    private fun drawGameBoard() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw the left board
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(gameState.boardOrigin.x, gameState.boardOrigin.y, gameState.boardWidth, gameState.boardHeight)

        // Draw the right board
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(
            gameState.boardOrigin.x + gameState.boardOffset, gameState.boardOrigin.y, gameState.boardWidth, gameState.boardHeight)

        // Draw frozen blocks
        for (block in gameState.frozenBlocks) {
            shapeRenderer.color = block.color
            val xOffset = if (gameState.fallingOnLeft) gameState.boardOrigin.x + gameState.boardOffset else gameState.boardOrigin.x
            shapeRenderer.rect(
                xOffset + block.pos.x,
                gameState.boardOrigin.y + block.pos.y,
                1f,
                1f
            )
        }

        // Draw the current piece
        shapeRenderer.color = gameState.currentPiece.color
        val pieceXOffset = if (gameState.fallingOnLeft) gameState.boardOrigin.x else gameState.boardOrigin.x + gameState.boardOffset
        for (offset in gameState.currentPiece.getRotatedOffsets(gameState.rotationState)) {
            val x = gameState.piecePosition.x + offset.x + pieceXOffset
            val y = gameState.piecePosition.y + offset.y + gameState.boardOrigin.y
            if (y < (gameState.boardOrigin.y + gameState.boardHeight)) shapeRenderer.rect(x, y, 1f, 1f)
        }

        shapeRenderer.end()

        // Draw gridlines on top of everything
        drawGridLines()
    }

    private fun drawGridLines() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.LIGHT_GRAY

        val origin = gameState.boardOrigin
        val offset = gameState.boardOffset

        // Draw outlines for both boards
        shapeRenderer.rect(origin.x, origin.y, gameState.boardWidth, gameState.boardHeight)
        shapeRenderer.rect(
            origin.x + offset, origin.y, gameState.boardWidth, gameState.boardHeight)

        // Draw half of squares on left board, half on right - will form grid when merged properly
        for (x in 0..gameState.boardWidth.toInt() - 1) {
            for (y in 0..gameState.boardHeight.toInt()) {
                if ((x + y) % 2 == 0) {
                    if (x % 2 == 0) {
                        shapeRenderer.rect(x.toFloat() + origin.x, y.toFloat() + origin.y, 1f, 1f)
                    } else {
                        shapeRenderer.rect(x.toFloat() + origin.x + offset, y.toFloat() + origin.y, 1f, 1f)
                    }
                }
            }
        }
        shapeRenderer.end()
    }

    private fun drawGetReadyOverlay() {
        val overlayWidth = gameState.boardWidth * 0.8f // 80% of the board width
        val overlayHeight = gameState.boardHeight * 0.2f // 20% of the board height
        val overlayY = gameState.boardOrigin.y + gameState.boardHeight * 0.5f - overlayHeight / 2f // Centered vertically

        // Draw a vertical line from the top to the bottom of the screen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        val centerX = gameState.boardOrigin.x + gameState.boardWidth + gameState.internalSpacing / 2f
        shapeRenderer.line(centerX, 0f, centerX, gameState.worldHeight)
        shapeRenderer.end()
        
        // Draw the overlay background
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Left screen (centered on the left playing field)
        val leftCenterX = gameState.boardOrigin.x + gameState.boardWidth / 2f
        shapeRenderer.rect(leftCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        // Right screen (centered on the right playing field)
        val rightCenterX = gameState.boardOrigin.x + gameState.boardOffset + gameState.boardWidth / 2f
        shapeRenderer.rect(rightCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        shapeRenderer.end()

        // Draw the overlay text
        game.batch.projectionMatrix = textCamera.combined
        game.batch.begin()
        font.color = Color.WHITE
        
        val startText = if (isAndroid) "Press Start" else "Press Space to Start"

        val textLine1L = convertToTextCameraCoordinates(leftCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.75f)
        val textLine1R = convertToTextCameraCoordinates(rightCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.75f)
        font.draw(game.batch, "Get Ready!", textLine1L.x, textLine1L.y)
        font.draw(game.batch, "Get Ready!", textLine1R.x, textLine1R.y)

        val textLine2L = convertToTextCameraCoordinates(leftCenterX - overlayWidth / 4f - 1.5f, overlayY + overlayHeight * 0.55f)
        val textLine2R = convertToTextCameraCoordinates(rightCenterX - overlayWidth / 4f - 1.5f, overlayY + overlayHeight * 0.55f)
        font.draw(game.batch, startText, textLine2L.x, textLine2L.y)
        font.draw(game.batch, startText, textLine2R.x, textLine2R.y)

        val textLine3L = convertToTextCameraCoordinates(leftCenterX - 0.5f, overlayY + overlayHeight * 0.35f)
        val textLine3R = convertToTextCameraCoordinates(rightCenterX -0.5f, overlayY + overlayHeight * 0.35f)
        if (gameState.fallingOnLeft) font.draw(game.batch, "<-", textLine3L.x, textLine3L.y)
        else font.draw(game.batch, "->", textLine3R.x, textLine3R.y)
        
        game.batch.end()
    }

    // Convert game coordinates to textCamera coordinates
    private fun convertToTextCameraCoordinates(gameX: Float, gameY: Float): com.badlogic.gdx.math.Vector3 {
        // Step 1: Project game coordinates to screen space using the main camera
        val screenCoordinates = camera.project(com.badlogic.gdx.math.Vector3(gameX, gameY, 0f))

        // Step 2: Flip the y coordinate for screen space
        screenCoordinates.y = Gdx.graphics.height - screenCoordinates.y

        // Step 3: Unproject screen coordinates to textCamera world space
        return textCamera.unproject(screenCoordinates)
    }

    private fun drawGameOverOverlay() {
        val overlayWidth = gameState.boardWidth * 0.8f // 80% of the board width
        val overlayHeight = gameState.boardHeight * 0.3f // 30% of the board height
        val overlayY = gameState.boardOrigin.y + gameState.boardHeight * 0.5f - overlayHeight / 2 // Centered vertically

        // Draw the overlay background
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Left screen (centered on the left playing field)
        val leftCenterX = gameState.boardOrigin.x + gameState.boardWidth / 2
        shapeRenderer.rect(leftCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        // Right screen (centered on the right playing field)
        val rightCenterX = gameState.boardOrigin.x + gameState.boardOffset + gameState.boardWidth / 2
        shapeRenderer.rect(rightCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        shapeRenderer.end()

        // Draw the overlay text
        game.batch.projectionMatrix = textCamera.combined
        game.batch.begin()
        font.color = Color.WHITE

        val restartText = if (isAndroid) "Press Start to Restart" else "Press R to Restart"

        val textLine1L = convertToTextCameraCoordinates(leftCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.75f)
        val textLine1R = convertToTextCameraCoordinates(rightCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.75f)
        font.draw(game.batch, "Game Over", textLine1L.x, textLine1L.y)
        font.draw(game.batch, "Game Over", textLine1R.x, textLine1R.y)

        val textLine2L = convertToTextCameraCoordinates(leftCenterX - overlayWidth / 4f - 1.5f, overlayY + overlayHeight * 0.55f)
        val textLine2R = convertToTextCameraCoordinates(rightCenterX - overlayWidth / 4f - 1.5f, overlayY + overlayHeight * 0.55f)
        font.draw(game.batch, restartText, textLine2L.x, textLine2L.y)
        font.draw(game.batch, restartText, textLine2R.x, textLine2R.y)

        val textLine3L = convertToTextCameraCoordinates(leftCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.35f)
        val textLine3R = convertToTextCameraCoordinates(rightCenterX - overlayWidth / 4f, overlayY + overlayHeight * 0.35f)
        font.draw(game.batch, "Score: ${gameState.score}", textLine3L.x, textLine3L.y)
        font.draw(game.batch, "Score: ${gameState.score}", textLine3R.x, textLine3R.y)
        
        game.batch.end()
    }
}