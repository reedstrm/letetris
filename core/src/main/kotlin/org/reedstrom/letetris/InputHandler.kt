package org.reedstrom.letetris

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor

class InputHandler(private val gameState: GameState) : InputProcessor {
    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.LEFT -> if (gameState.waitingForStart) gameState.fallingOnLeft = true else gameState.moveLeft()
            Input.Keys.RIGHT -> if (gameState.waitingForStart) gameState.fallingOnLeft = false else gameState.moveRight()
            Input.Keys.DOWN -> gameState.moveDown()
            Input.Keys.UP -> gameState.rotatePiece()
            Input.Keys.SPACE -> if (gameState.waitingForStart) gameState.startGame()
            Input.Keys.R -> if (gameState.gameOver) gameState.restartGame()
            Input.Keys.T -> if (gameState.waitingForStart) gameState.activeScreen = "TestScreen" // Switch to TestScreen
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean = false
    override fun keyTyped(character: Char): Boolean = false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
    override fun scrolled(amountX: Float, amountY: Float): Boolean = false
    override fun touchCancelled(p0: Int, p1: Int, p2: Int, p3: Int): Boolean = false
}