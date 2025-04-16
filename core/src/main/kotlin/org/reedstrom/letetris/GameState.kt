package org.reedstrom.letetris

import com.badlogic.gdx.graphics.Color

data class Position(var x: Int, var y: Int) 

class GameState(private val boardWidth: Int, private val boardHeight: Int) {
    var score: Int = 0
    var gameOver: Boolean = false
    var waitingForStart: Boolean = true

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
        piecePosition.x -= 1
        if (checkCollision(xOnly = true)) piecePosition.x += 1
    }

    fun moveRight() {
        piecePosition.x += 1
        if (checkCollision(xOnly = true)) piecePosition.x -= 1
    }

    fun moveDown() {
        piecePosition.y -= 1
        if (checkCollision(yOnly = true)) {
            piecePosition.y += 1
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

    private fun checkCollision(xOnly: Boolean = false, yOnly: Boolean = false): Boolean {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x.toInt()
            val y = piecePosition.y + offset.y.toInt()

            if (!xOnly && y < 0) return true
            if (!yOnly && (x < 0 || x >= boardWidth)) return true
            if (frozenBlocks.any { it.pos.x == x && it.pos.y == y }) return true
        }
        return false
    }

    private fun lockPiece() {
        for (offset in currentPiece.getRotatedOffsets(rotationState)) {
            val x = piecePosition.x + offset.x.toInt()
            val y = piecePosition.y + offset.y.toInt()
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
        val fullRows = rows.filter { (_, blocks) -> blocks.size >= boardWidth }.keys.sorted()

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
