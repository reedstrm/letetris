package org.reedstrom.letetris

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class ConfigScreen(private val game: GameMain, private val gameState: GameState) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val shapeRenderer = ShapeRenderer()

    init {
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLUE
        shapeRenderer.rect(100f, 100f, 200f, 200f) // Example content
        shapeRenderer.end()

        // Handle input to go back to the main screen
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(GameScreen(game)) // Switch back to the main game screen
        }
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }
}