package org.reedstrom.letetris

import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.Gdx

class ControllerHandler(private val gameState: GameState) : ControllerListener {
    private val deadZone = 0.5f // Larger dead zone to reduce sensitivity
    private var lastAxisMoveTime = 0f // Tracks the last time an axis move was triggered
    private val axisMoveCooldown = 0.2f // Minimum time (in seconds) between axis moves

    override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
        val mapping = controller?.mapping ?: return false
        when (buttonCode) {
            mapping.buttonA -> gameState.moveDown()
            mapping.buttonB ->  if (gameState.waitingForStart) gameState.fallingOnLeft = false else gameState.moveRight()
            mapping.buttonX ->  if (gameState.waitingForStart) gameState.fallingOnLeft = true else gameState.moveLeft()
            mapping.buttonY -> gameState.rotatePiece()
            mapping.buttonStart -> if (gameState.waitingForStart) gameState.startGame() else if (gameState.gameOver) gameState.restartGame()
        }
        return true
    }

    override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
        val deltaTime = Gdx.graphics.deltaTime // Time elapsed since the last frame
        lastAxisMoveTime += deltaTime // Accumulate time for cooldown tracking

        // Check if enough time has passed since the last move
        if (lastAxisMoveTime < axisMoveCooldown) {
            return false
        }

        if (axisCode == controller?.mapping?.axisLeftX) {
            if (value > deadZone) {
                if (gameState.waitingForStart) gameState.fallingOnLeft = false else gameState.moveRight()
                lastAxisMoveTime = 0f // Reset cooldown timer
            } else if (value < -deadZone) {
                if (gameState.waitingForStart) gameState.fallingOnLeft = true else gameState.moveLeft()
                lastAxisMoveTime = 0f // Reset cooldown timer
            }
        }

        if (axisCode == controller?.mapping?.axisLeftY) {
            if (value > deadZone) {
                gameState.moveDown()
                lastAxisMoveTime = 0f // Reset cooldown timer
            } else if (value < -deadZone) {
                gameState.rotatePiece()
                lastAxisMoveTime = 0f // Reset cooldown timer
            }
        }
        return true
    }

    override fun connected(controller: Controller?) {}
    override fun disconnected(controller: Controller?) {}
    override fun buttonUp(controller: Controller?, buttonCode: Int): Boolean = false
}