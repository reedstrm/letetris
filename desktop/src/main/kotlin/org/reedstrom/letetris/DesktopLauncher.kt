// Desktop Launcher for letetris
// File: desktop/src/main/kotlin/org/reedstrom/letetris/DesktopLauncher.kt

package org.reedstrom.letetris

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setTitle("Letetris 3D")
            setWindowedMode(800, 480)
            setForegroundFPS(60)
        }
        Lwjgl3Application(GameMain(), config)
    }
}
