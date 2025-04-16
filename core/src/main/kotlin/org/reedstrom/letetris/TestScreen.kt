package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class TestScreen : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()

    private val blockSize = 20f // Size of each block
    private val padding = 10f  // Padding between Tetrominos
    private val tetrominoSpacing = 100f // Space between Tetromino groups

    init {
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    }

    override fun render(delta: Float) {
        // Clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw all Tetrominos and their rotations
        var xOffset = padding
        var yOffset = padding
        for (tetromino in Tetromino.values()) {
            for (rotation in 0..3) {
                drawTetromino(tetromino, rotation, xOffset, yOffset)
                yOffset += blockSize * 5 // Move to the next column for the next rotation
            }
            yOffset = padding // Reset xOffset for the next Tetromino
            xOffset += tetrominoSpacing // Move down for the next Tetromino
        }

        shapeRenderer.end()
    }

    private fun drawTetromino(tetromino: Tetromino, rotation: Int, xOffset: Float, yOffset: Float) {
        shapeRenderer.color = tetromino.color
        val offsets = tetromino.getRotatedOffsets(rotation)

        for (offset in offsets) {
            val x = xOffset + offset.x * blockSize
            val y = yOffset + offset.y * blockSize
            shapeRenderer.rect(x, y, blockSize, blockSize)
        }
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }
}