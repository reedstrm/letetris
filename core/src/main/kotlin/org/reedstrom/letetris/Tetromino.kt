// File: core/src/main/kotlin/org/reedstrom/letetris/Tetromino.kt
package org.reedstrom.letetris

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

enum class Tetromino(
    private val shapes: Array<Array<Vector2>>,
    val color: Color
) {
    I(arrayOf(
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(2f, 0f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(0f, 2f)),
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(2f, 0f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(0f, 2f))
    ), Color.CYAN),

    O(arrayOf(
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f), Vector2(1f, 1f))
    ), Color.YELLOW),

    T(arrayOf(
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, 1f)),
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(0f, -1f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(-1f, 0f), Vector2(0f, 1f))
    ), Color.MAGENTA),

    S(arrayOf(
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(-1f, 1f), Vector2(0f, 1f)),
        arrayOf(Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, -1f), Vector2(1f, 0f)),
        arrayOf(Vector2(0f, 0f), Vector2(1f, 0f), Vector2(-1f, 1f), Vector2(0f, 1f)),
        arrayOf(Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, -1f), Vector2(1f, 0f))
    ), Color.GREEN),

    Z(arrayOf(
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(1f, -1f), Vector2(1f, 0f), Vector2(0f, 0f), Vector2(0f, 1f)),
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(1f, -1f), Vector2(1f, 0f), Vector2(0f, 0f), Vector2(0f, 1f))
    ), Color.RED),

    J(arrayOf(
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(1f, 1f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, -1f)),
        arrayOf(Vector2(-1f, -1f), Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(-1f, 1f))
    ), Color.BLUE),

    L(arrayOf(
        arrayOf(Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f), Vector2(-1f, 1f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(1f, 1f)),
        arrayOf(Vector2(1f, -1f), Vector2(-1f, 0f), Vector2(0f, 0f), Vector2(1f, 0f)),
        arrayOf(Vector2(0f, -1f), Vector2(0f, 0f), Vector2(0f, 1f), Vector2(-1f, -1f))
    ), Color.ORANGE);

    fun getRotatedOffsets(rotation: Int): Array<Vector2> {
        return shapes[rotation % shapes.size]
    }

    companion object {
        fun random(): Tetromino {
            return values().random()
        }
    }
}
