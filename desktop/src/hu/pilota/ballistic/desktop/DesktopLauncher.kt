package hu.pilota.ballistic.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import hu.pilota.ballistic.MyGame

class DesktopLauncher {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val config = LwjglApplicationConfiguration()

            config.fullscreen = true
            config.width = 1920
            config.height = 1080

            LwjglApplication(MyGame(), config)
        }
    }
}
