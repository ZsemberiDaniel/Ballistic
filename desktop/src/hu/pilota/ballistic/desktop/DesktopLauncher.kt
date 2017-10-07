package hu.pilota.ballistic.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import hu.pilota.ballistic.MyGame

class DesktopLauncher {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val config = LwjglApplicationConfiguration()

            config.fullscreen = false
            config.width = 900
            config.height = 600

            LwjglApplication(MyGame(), config)
        }
    }
}
