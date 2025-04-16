package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import org.reedstrom.letetris.InputHandler

class GameScreen(private val game: GameMain) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply {
        data.setScale(2f)
    }

    private val isAndroid = Gdx.app.type.name == "Android"

    // Dynamically calculate viewport size
    private var viewportWidth: Float = 0f
    private var viewportHeight: Float = 0f

    private var blockSize: Float = 0f
    private var borderWidth: Float = 0f
    private var boardOrigin: Vector2 = Vector2()
    private var boardOffset: Float = 0f

    private val boardWidth = 10
    private val boardHeight = 16
    private val borderWidthFactor = 1.5f // Adjusted factor for border width

    private val gameState = GameState(boardWidth, boardHeight)

    init {
        viewportWidth = Gdx.graphics.width.toFloat()
        viewportHeight = Gdx.graphics.height.toFloat()

        // Calculate dependent properties
        blockSize = viewportWidth / (boardWidth * 2 + 4 * borderWidthFactor)
        borderWidth = blockSize * borderWidthFactor
        boardOrigin = Vector2(borderWidth, borderWidth)
        boardOffset = boardWidth * blockSize + 2 * borderWidth

        // Log calculated dimensions
        Gdx.app.log("GameScreen", "Viewport dimensions: width=$viewportWidth, height=$viewportHeight")
        Gdx.app.log("GameScreen", "blockSize: $blockSize\nTotal Width: ${blockSize * boardWidth * 2 + 4 * borderWidth}")
    }

    override fun show() {
        camera.setToOrtho(false, viewportWidth, viewportHeight)

        // Register input handlers
        Gdx.input.inputProcessor = InputHandler(gameState)
        Controllers.addListener(ControllerHandler(gameState))
    }

    override fun render(delta: Float) {
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
        // Clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        // Draw the filled board areas
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(boardOrigin.x, boardOrigin.y, boardWidth * blockSize, boardHeight * blockSize)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(boardOrigin.x + boardOffset, boardOrigin.y, boardWidth * blockSize, boardHeight * blockSize)

        // Draw frozen blocks to right field
        for (block in gameState.frozenBlocks) {
            shapeRenderer.color = block.color
            shapeRenderer.rect(toScreenX(block.pos.x) + boardOffset, toScreenY(block.pos.y), blockSize, blockSize)
        }

        // Draw the current piece
        shapeRenderer.color = gameState.currentPiece.color
        for (offset in gameState.currentPiece.getRotatedOffsets(gameState.rotationState)) {
            val x = gameState.piecePosition.x + offset.x.toInt()
            val y = gameState.piecePosition.y + offset.y.toInt()
            shapeRenderer.rect(toScreenX(x), toScreenY(y), blockSize, blockSize)
        }
        shapeRenderer.end()

        // Draw the grid lines on top of everything
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.GRAY
        for (i in 0..boardWidth) {
            val x = boardOrigin.x + i * blockSize
            shapeRenderer.line(x, boardOrigin.y, x, boardOrigin.y + boardHeight * blockSize)
            shapeRenderer.line(x + boardOffset, boardOrigin.y, x + boardOffset, boardOrigin.y + boardHeight * blockSize)
        }
        for (j in 0..boardHeight) {
            val y = boardOrigin.y + j * blockSize
            shapeRenderer.line(boardOrigin.x, y, boardOrigin.x + boardWidth * blockSize, y)
            shapeRenderer.line(boardOrigin.x + boardOffset, y, boardOrigin.x + boardWidth * blockSize + boardOffset, y)
        }
        shapeRenderer.end()

        // Draw the score
        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE
        font.draw(game.batch, "Score: ${gameState.score}", boardOrigin.x, viewportHeight - 20f)
        game.batch.end()
    }

    private fun drawGetReadyOverlay() {
        val overlayWidth = boardWidth * blockSize * 0.8f // 80% of the board width
        val overlayHeight = boardHeight * blockSize * 0.2f // 20% of the board height
        val overlayY = boardOrigin.y + boardHeight * blockSize * 0.5f - overlayHeight / 2 // Centered vertically

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Left screen (centered on the left playing field)
        val leftCenterX = boardOrigin.x + boardWidth * blockSize / 2
        shapeRenderer.rect(leftCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        // Right screen (centered on the right playing field)
        val rightCenterX = boardOrigin.x + boardOffset + boardWidth * blockSize / 2
        shapeRenderer.rect(rightCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        shapeRenderer.end()

        // Draw the thin white line directly between the two fields
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        val centerX = boardOrigin.x + boardWidth * blockSize + borderWidth // Midpoint between the two fields
        shapeRenderer.line(centerX, 0f, centerX, viewportHeight)
        shapeRenderer.end()

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE

        val startText = if (isAndroid) "Press Start" else "Press Space to Start"

        // Draw text for the left screen
        font.draw(game.batch, "Get Ready!", leftCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.6f)
        font.draw(game.batch, startText, leftCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.3f)

        // Draw text for the right screen
        font.draw(game.batch, "Get Ready!", rightCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.6f)
        font.draw(game.batch, startText, rightCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.3f)

        game.batch.end()
    }

    private fun drawGameOverOverlay() {
        val overlayWidth = boardWidth * blockSize * 0.8f // 80% of the board width
        val overlayHeight = boardHeight * blockSize * 0.3f // 30% of the board height
        val overlayY = boardOrigin.y + boardHeight * blockSize * 0.5f - overlayHeight / 2 // Centered vertically

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Left screen (centered on the left playing field)
        val leftCenterX = boardOrigin.x + boardWidth * blockSize / 2
        shapeRenderer.rect(leftCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        // Right screen (centered on the right playing field)
        val rightCenterX = boardOrigin.x + boardOffset + boardWidth * blockSize / 2
        shapeRenderer.rect(rightCenterX - overlayWidth / 2, overlayY, overlayWidth, overlayHeight)

        shapeRenderer.end()

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE

        val restartText = if (isAndroid) "Press Start to Restart" else "Press R to Restart"

        // Draw text for the left screen
        font.draw(game.batch, "Game Over", leftCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.7f)
        font.draw(game.batch, restartText, leftCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.4f)
        font.draw(game.batch, "Score: ${gameState.score}", leftCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.1f)

        // Draw text for the right screen
        font.draw(game.batch, "Game Over", rightCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.7f)
        font.draw(game.batch, restartText, rightCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.4f)
        font.draw(game.batch, "Score: ${gameState.score}", rightCenterX - overlayWidth / 4, overlayY + overlayHeight * 0.1f)

        game.batch.end()
    }

    // Helper functions to convert grid coordinates to screen coordinates
    private fun toScreenX(gridX: Int): Float {
        return boardOrigin.x + gridX * blockSize
    }

    private fun toScreenY(gridY: Int): Float {
        return boardOrigin.y + gridY * blockSize
    }
}