package hu.pilota.ballistic

import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
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
    lateinit var xIcon: Texture
    lateinit var tweenManager: TweenManager

    override fun create() {
        tweenManager = TweenManager()
        Tween.registerAccessor(Actor::class.java, ActorAccessor())

        enableKtxCoroutines(asynchronousExecutorConcurrencyLevel = 1)

        val assetStorage = AssetStorage()
        ktxAsync {
            // Load stuff
            assetStorage.apply {
                val defaultSkin = load<Skin>("flatearthui/flat-earth-ui.json")
                xIcon = load("x.png")

                context.register {
                    bindSingleton(defaultSkin)
                }
            }

            // Add screens
            addScreen(MainScreen(this@MyGame))

            // Set start screen
            setScreen<MainScreen>()
        }
    }

    override fun render() {
        super.render()

        tweenManager.update(Gdx.graphics.deltaTime)
    }
}