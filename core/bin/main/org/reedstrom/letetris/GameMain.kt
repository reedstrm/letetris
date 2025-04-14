package org.reedstrom.letetris

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameMain : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        this.setScreen(GameScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        screen.dispose()
    }
}
