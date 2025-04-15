package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class GameScreen(private val game: GameMain) : ScreenAdapter() {
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

    override fun show() {
        camera.setToOrtho(false, 800f, 480f)
    }

    override fun render(delta: Float) {
        handleRestart()
        if (gameOver) {
            drawGameOverOverlay()
            return
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        handleInput()

        fallTimer += delta
        if (fallTimer >= fallInterval) {
            piecePosition.y -= blockSize
            if (checkCollision(yOnly = true)) {
                piecePosition.y += blockSize
                lockPiece()
                clearCompletedLines()
                spawnNewPiece()
                if (checkCollision()) gameOver = true
            }
            fallTimer = 0f
        }

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Play zones
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(boardOrigin.x, boardOrigin.y, boardWidth * blockSize, boardHeight * blockSize)

        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(boardOrigin.x + boardOffset, boardOrigin.y, boardWidth * blockSize, boardHeight * blockSize)

        // Draw frozen blocks
        for (block in frozenBlocks) {
            shapeRenderer.color = block.color
            shapeRenderer.rect(block.pos.x, block.pos.y, blockSize, blockSize)
        }

        // Draw current piece
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

        // Draw debug grid
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
    }

    private fun drawGameOverOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(200f, 190f, 400f, 100f)
        shapeRenderer.end()

        game.batch.begin()
        game.batch.color = Color.WHITE
        game.batch.projectionMatrix = camera.combined
        font.color = Color.WHITE
        font.draw(game.batch, "Game Over", 330f, 250f)
        font.draw(game.batch, "Press R to Restart", 270f, 220f)
        game.batch.end()
    }

    private fun handleRestart() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            frozenBlocks.clear()
            piecePosition.set(blockSize * 5 + borderWidth, boardOrigin.y + boardHeight * blockSize)
            rotationState = 0
            currentPiece = Tetromino.random()
            fallTimer = 0f
            gameOver = false
        }
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            piecePosition.x -= blockSize
            if (checkCollision(xOnly = true)) piecePosition.x += blockSize
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            piecePosition.x += blockSize
            if (checkCollision(xOnly = true)) piecePosition.x -= blockSize
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            val oldRotation = rotationState
            rotationState = (rotationState + 1) % 4
            if (checkCollision()) rotationState = oldRotation
        }
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

        frozenBlocks.removeIf { it.pos.y in fullRows }

        for (row in fullRows) {
            for (block in frozenBlocks) {
                if (block.pos.y > row) {
                    block.pos = Vector2(block.pos.x, block.pos.y - blockSize)
                }
            }
        }
    }
}
