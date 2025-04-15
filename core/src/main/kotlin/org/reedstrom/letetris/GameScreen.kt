package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class GameScreen(private val game: GameMain) : ScreenAdapter(), ControllerListener {
    private val camera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply {
        data.setScale(2f)
    }

    private val blockSize = 30f
    private val boardWidth = 10
    private val boardHeight = 20
    private val borderWidth = 50f
    private val boardOrigin = Vector2(borderWidth, borderWidth)
    private val boardOffset = boardWidth * blockSize + 2 * borderWidth

    private var piecePosition = Vector2(blockSize * 5 + borderWidth, boardOrigin.y + boardHeight * blockSize)
    private var rotationState = 0
    private var currentPiece = Tetromino.random()

    private var fallTimer = 0f
    private val fallInterval = 0.5f

    data class FrozenBlock(var pos: Vector2, val color: Color)
    private val frozenBlocks = mutableListOf<FrozenBlock>()

    private var gameOver = false
    private var score = 0
    private val isAndroid = Gdx.app.type.name == "Android"
    private var waitingForStart = true

    override fun show() {
        camera.setToOrtho(false, 800f, 480f)
        Controllers.addListener(this)
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

        // Draw frozen blocks
        for (block in frozenBlocks) {
            shapeRenderer.color = block.color
            shapeRenderer.rect(block.pos.x, block.pos.y, blockSize, blockSize)
        }

        // Draw the current piece
        shapeRenderer.color = currentPiece.color
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            shapeRenderer.rect(
                piecePosition.x + offset.x * blockSize,
                piecePosition.y + offset.y * blockSize,
                blockSize,
                blockSize
            )
        }
        shapeRenderer.end()

        // Draw the grid lines
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
            shapeRenderer.line(boardOrigin.x + boardOffset, y, boardOrigin.x + boardOffset + boardWidth * blockSize, y)
        }
        shapeRenderer.end()
    }

    override fun render(delta: Float) {
        drawGameBoard()

        if (waitingForStart) {
            drawGetReadyOverlay()
            val controller = Controllers.getCurrent()
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controller?.getButton(controller?.mapping?.buttonStart ?: -1) == true) {
                waitingForStart = false
            }
            return
        }

        handleRestart()
        if (gameOver) {
            drawGameOverOverlay()
            return
        }

        handleInput()

        fallTimer += delta
        if (fallTimer >= fallInterval) {
            moveDown()
            fallTimer = 0f
        }

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE
        font.draw(game.batch, "Score: $score", boardOrigin.x + boardOffset, boardOrigin.y - 20f)
        game.batch.end()
    }

    private fun handleRestart() {
        val controller = Controllers.getCurrent()
        val restartPressed = Gdx.input.isKeyJustPressed(Input.Keys.R) || (controller?.getButton(controller.mapping.buttonStart) == true)

        if (restartPressed) {
            frozenBlocks.clear()
            piecePosition.set(blockSize * 5 + borderWidth, boardOrigin.y + boardHeight * blockSize)
            rotationState = 0
            currentPiece = Tetromino.random()
            fallTimer = 0f
            score = 0
            gameOver = false
            // Clear the screen
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) moveLeft()
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) moveRight()
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) moveDown()
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) rotatePiece()
    }

    private fun moveLeft() {
        piecePosition.x -= blockSize
        if (checkCollision(xOnly = true)) piecePosition.x += blockSize
    }

    private fun moveRight() {
        piecePosition.x += blockSize
        if (checkCollision(xOnly = true)) piecePosition.x -= blockSize
    }

    private fun moveDown() {
        piecePosition.y -= blockSize
        if (checkCollision(yOnly = true)) {
            piecePosition.y += blockSize
            lockPiece()
            clearCompletedLines()
            spawnNewPiece()
            if (checkCollision()) {
                gameOver = true
            }
        }
    }

    private fun rotatePiece() {
        val oldRotation = rotationState
        rotationState = (rotationState + 1) % 4
        if (checkCollision()) rotationState = oldRotation
    }

    private fun checkCollision(xOnly: Boolean = false, yOnly: Boolean = false): Boolean {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x * blockSize
            val y = piecePosition.y + offset.y * blockSize

            if (!xOnly && y < boardOrigin.y) return true
            if (!yOnly && (x < boardOrigin.x || x >= boardOrigin.x + boardWidth * blockSize)) return true
            if (frozenBlocks.any { it.pos.x - boardOffset == x && it.pos.y == y }) return true
        }
        return false
    }

    private fun lockPiece() {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x * blockSize
            val y = piecePosition.y + offset.y * blockSize
            frozenBlocks.add(FrozenBlock(Vector2(x + boardOffset, y), currentPiece.color))
        }
        score += 10
    }

    private fun spawnNewPiece() {
        piecePosition.set(blockSize * 5 + borderWidth, boardOrigin.y + boardHeight * blockSize)
        rotationState = 0
        currentPiece = Tetromino.random()
    }

    private fun clearCompletedLines() {
        val rows = frozenBlocks.groupBy { it.pos.y }
        val fullRows = rows.filter { (_, blocks) ->
            blocks.count { it.pos.x >= boardOrigin.x + boardOffset && it.pos.x < boardOrigin.x + boardOffset + boardWidth * blockSize } >= boardWidth
        }.keys.sorted()

        if (fullRows.isEmpty()) return

        score += fullRows.size * 100

        // Remove all blocks in the full rows
        frozenBlocks.removeIf { it.pos.y in fullRows }

        // Find the highest cleared row
        val maxClearedRow = fullRows.maxOrNull() ?: return

        // Move all blocks above the highest cleared row down
        for (block in frozenBlocks) {
            if (block.pos.y > maxClearedRow) {
                block.pos = Vector2(block.pos.x, block.pos.y - blockSize * fullRows.size)
            }
        }
    }

    private fun drawGetReadyOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Centered rectangles for both screens
        shapeRenderer.rect(boardOrigin.x + boardWidth * blockSize / 2 - 200f, 190f, 400f, 100f)
        shapeRenderer.rect(boardOrigin.x + boardOffset + boardWidth * blockSize / 2 - 200f, 190f, 400f, 100f)

        shapeRenderer.end()

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE

        val startText = if (isAndroid) "Press Start" else "Press Space to Start"

        // Left screen (centered on the left playing field)
        val leftCenterX = boardOrigin.x + boardWidth * blockSize / 2
        font.draw(game.batch, "Get Ready!", leftCenterX - 70f, 250f)
        font.draw(game.batch, startText, leftCenterX - 70f, 220f)

        // Right screen (centered on the right playing field)
        val rightCenterX = boardOrigin.x + boardOffset + boardWidth * blockSize / 2
        font.draw(game.batch, "Get Ready!", rightCenterX - 70f, 250f)
        font.draw(game.batch, startText, rightCenterX - 70f, 220f)

        game.batch.end()
    }

    private fun drawGameOverOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)

        // Centered rectangles for both screens
        shapeRenderer.rect(boardOrigin.x + boardWidth * blockSize / 2 - 200f, 150f, 400f, 140f)
        shapeRenderer.rect(boardOrigin.x + boardOffset + boardWidth * blockSize / 2 - 200f, 150f, 400f, 140f)

        shapeRenderer.end()

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE

        val restartText = if (isAndroid) "Press Start to Restart" else "Press R to Restart"

        // Left screen (centered on the left playing field)
        val leftCenterX = boardOrigin.x + boardWidth * blockSize / 2
        font.draw(game.batch, "Game Over", leftCenterX - 70f, 250f)
        font.draw(game.batch, restartText, leftCenterX - 130f, 220f)
        font.draw(game.batch, "Score: $score", leftCenterX - 70f, 190f)

        // Right screen (centered on the right playing field)
        val rightCenterX = boardOrigin.x + boardOffset + boardWidth * blockSize / 2
        font.draw(game.batch, "Game Over", rightCenterX - 70f, 250f)
        font.draw(game.batch, restartText, rightCenterX - 130f, 220f)
        font.draw(game.batch, "Score: $score", rightCenterX - 70f, 190f)

        game.batch.end()
    }

    override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
        val mapping = controller?.mapping ?: return false

        when (buttonCode) {
            mapping.buttonA -> moveDown()
            mapping.buttonB -> moveRight()
            mapping.buttonX -> moveLeft()
            mapping.buttonY -> rotatePiece()
        }

        return true
    }

    override fun connected(controller: Controller?) {}
    override fun disconnected(controller: Controller?) {}
    override fun buttonUp(controller: Controller?, buttonCode: Int): Boolean = false
    override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean = false
}
