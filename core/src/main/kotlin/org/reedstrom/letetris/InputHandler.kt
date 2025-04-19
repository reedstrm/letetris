package org.reedstrom.letetris

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Gdx

class InputHandler(private val gameState: GameState) : InputProcessor {
    override fun keyDown(keycode: Int): Boolean {
        // Gdx.app.log("InputHandler", "Key pressed: $keycode")
        when (keycode) {
            Input.Keys.LEFT -> gameState.moveLeft()
            Input.Keys.RIGHT -> gameState.moveRight()
            Input.Keys.DOWN -> gameState.drop() 
            Input.Keys.UP -> gameState.rotatePiece()
            Input.Keys.SPACE -> if (gameState.waitingForStart) gameState.startGame()
            Input.Keys.R -> if (gameState.gameOver) gameState.restartGame()
            Input.Keys.T -> if (gameState.waitingForStart) gameState.activeScreen = "TestScreen" // Switch to TestScreen
            Input.Keys.C -> if (gameState.waitingForStart) gameState.activeScreen = "ConfigScreen" // Switch to ConfigScreen
            Input.Keys.V -> if (gameState.waitingForStart) gameState.activeScreen = "CreditsScreen" // Switch to CreditsScreen
            Input.Keys.COMMA -> gameState.fallingOnLeft = true
            Input.Keys.PERIOD -> gameState.fallingOnLeft = false
            Input.Keys.LEFT_BRACKET -> gameState.internalSpacing += 0.1f
            Input.Keys.RIGHT_BRACKET -> gameState.internalSpacing -= 0.1f
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