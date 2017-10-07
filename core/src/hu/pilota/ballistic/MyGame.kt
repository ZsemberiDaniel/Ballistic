package hu.pilota.ballistic

import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import hu.pilota.ballistic.hu.pilota.ballistic.screens.MainScreen
import ktx.app.KtxGame
import ktx.async.assets.AssetStorage
import ktx.async.enableKtxCoroutines
import ktx.async.ktxAsync
import ktx.inject.Context

/**
 * Created by zsemberi.daniel on 2017. 10. 02..
 */
val context = Context().apply {
    register {
        bind { DigitFilter() }
    }
}

class MyGame : KtxGame<Screen>() {
    override fun create() {
        enableKtxCoroutines(asynchronousExecutorConcurrencyLevel = 1)

        val assetStorage = AssetStorage()
        ktxAsync {
            // Load stuff
            assetStorage.apply {
                val defaultSkin = load<Skin>("flatearthui/flat-earth-ui.json")

                context.register {
                    bindSingleton(defaultSkin)
                }
            }

            // Add screens
            addScreen(MainScreen())

            // Set start screen
            setScreen<MainScreen>()
        }
    }
}