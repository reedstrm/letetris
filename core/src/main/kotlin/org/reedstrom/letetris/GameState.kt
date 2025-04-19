package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Color

data class Position(var x: Float, var y: Float)

class GameState(private val initialBoardWidth: Float, private val initialBoardHeight: Float) {
    private val preferences: Preferences = Gdx.app.getPreferences("GamePreferences")

    // Board dimensions
    var boardWidth: Float = initialBoardWidth
        private set
    var boardHeight: Float = initialBoardHeight
        private set

    // Spacing and padding
    var xPadding: Float = 2f
        private set

    var yPadding: Float = 1f

    var internalSpacing: Float = preferences.getFloat("internalSpacing", 4f) // Load saved value or use default
        set(value) {
            field = value
            xPadding = ((constantSpacing - internalSpacing) / 2).coerceAtLeast(0f)
            updateDimensions()
            savePreferences() // Save the updated value
        }

    private val constantSpacing = 8f // Keep 2 * padding + internalSpacing constant

    // Virtual world dimensions
    var worldWidth: Float = 0f
        private set
    var worldHeight: Float = 0f
        private set

    // Board origins
    var boardOrigin: Position = Position(0f, 0f)
        private set
    var boardOffset: Float = 0f
        private set

    // Callback for when dimensions change
    var onDimensionsChanged: () -> Unit = {}

    init {
        updateDimensions()
    }

    private fun updateDimensions() {
        boardOrigin = Position(xPadding, yPadding)
        boardOffset = boardWidth + internalSpacing
        worldWidth = boardWidth * 2 + internalSpacing + xPadding * 2
        worldHeight = boardHeight + yPadding

        // Notify listeners of dimension changes
        onDimensionsChanged()
    }

    private fun savePreferences() {
        preferences.putFloat("internalSpacing", internalSpacing)
        preferences.flush() // Ensure the value is saved to disk
    }

    fun resizeBoard(newWidth: Float, newHeight: Float) {
        boardWidth = newWidth
        boardHeight = newHeight
        updateDimensions()
    }

    var score: Int = 0
    var gameOver: Boolean = false
    var waitingForStart: Boolean = true
    var fallingOnLeft: Boolean = true
    var activeScreen: String = "GameScreen" // Default to GameScreen

    var piecePosition = Position(boardWidth / 2, boardHeight - 1) // Grid-based position
    var rotationState = 0
    var currentPiece = Tetromino.random()

    private var fallTimer = 0f
    private val fallInterval = 0.5f

    data class FrozenBlock(var pos: Position, val color: Color)
    val frozenBlocks = mutableListOf<FrozenBlock>()

    fun startGame() {
        waitingForStart = false
    }

    fun restartGame() {
        frozenBlocks.clear()
        piecePosition = Position(boardWidth / 2, boardHeight - 1)
        rotationState = 0
        currentPiece = Tetromino.random()
        score = 0
        gameOver = false
        waitingForStart = true
        fallTimer = 0f
    }

    fun tick(delta: Float) {
        clearCompletedLines()
        fallTimer += delta
        if (fallTimer >= fallInterval) {
            moveDown()
            fallTimer = 0f
        }
    }

    fun moveLeft() {
        piecePosition.x -= 1f
        if (checkCollision(xOnly = true)) piecePosition.x += 1f
    }

    fun moveRight() {
        piecePosition.x += 1f
        if (checkCollision(xOnly = true)) piecePosition.x -= 1f
    }

    fun moveDown() {
        piecePosition.y -= 1f
        if (checkCollision(yOnly = true)) {
            piecePosition.y += 1f
            lockPiece()
            spawnNewPiece()
            if (checkCollision()) {
                gameOver = true
            }
        }
    }

    fun rotatePiece() {
        val oldRotation = rotationState
        rotationState = (rotationState + 1) % 4
        if (checkCollision()) rotationState = oldRotation
    }

    fun drop() {
        while (!checkCollision(yOnly = true)) {
            piecePosition.y -= 1f
        }
        piecePosition.y += 1f // Adjust back to the last valid position
        lockPiece()
        spawnNewPiece()
        if (checkCollision()) {
            gameOver = true
        }
    }

    private fun checkCollision(xOnly: Boolean = false, yOnly: Boolean = false): Boolean {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x
            val y = piecePosition.y + offset.y

            if (!xOnly && y < 0f) return true
            if (!yOnly && (x < 0f || x >= boardWidth)) return true
            if (frozenBlocks.any { it.pos.x == x && it.pos.y == y }) return true
        }
        return false
    }

    private fun lockPiece() {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x
            val y = piecePosition.y + offset.y
            frozenBlocks.add(FrozenBlock(Position(x, y), currentPiece.color))
        }
        score += 10
    }

    private fun spawnNewPiece() {
        piecePosition = Position(boardWidth / 2, boardHeight - 1)
        rotationState = 0
        currentPiece = Tetromino.random()
    }

    private fun clearCompletedLines() {
        // Group blocks by their row (y-coordinate)
        val rows = frozenBlocks.groupBy { it.pos.y }
        val fullRows = rows.filter { (_, blocks) -> blocks.size >= boardWidth.toInt() }.keys.sorted()

        if (fullRows.isEmpty()) return

        score += fullRows.size * 100

        // Remove all blocks in the full rows at once
        frozenBlocks.removeIf { it.pos.y in fullRows }

        // Move remaining blocks down by the number of cleared rows below them
        for (block in frozenBlocks) {
            val clearedBelow = fullRows.count { it < block.pos.y }
            block.pos.y -= clearedBelow
        }
    }
}
