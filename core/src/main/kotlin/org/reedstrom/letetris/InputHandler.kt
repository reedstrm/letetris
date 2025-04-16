package org.reedstrom.letetris

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor

class InputHandler(private val gameScreen: GameScreen) : InputProcessor {
    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.LEFT -> gameScreen.moveLeft()
            Input.Keys.RIGHT -> gameScreen.moveRight()
            Input.Keys.DOWN -> gameScreen.moveDown()
            Input.Keys.UP -> gameScreen.rotatePiece()
            Input.Keys.SPACE -> if (gameScreen.waitingForStart) gameScreen.startGame()
            Input.Keys.R -> if (gameScreen.gameOver) gameScreen.restartGame()
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