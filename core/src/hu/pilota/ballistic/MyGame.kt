package hu.pilota.ballistic

import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import hu.pilota.ballistic.hu.pilota.ballistic.hu.pilota.ballistic.tween.ActorAccessor
import hu.pilota.ballistic.hu.pilota.ballistic.screens.MainScreen
import ktx.app.KtxGame
import ktx.async.assets.AssetStorage
import ktx.async.enableKtxCoroutines
import ktx.async.ktxAsync
import ktx.inject.Context

/**
 * Created by zsemberi.daniel on 2017. 10. 02..
 */
val context = Context()

class MyGame : KtxGame<Screen>() {

    companion object {
        lateinit var xIcon: Texture
        lateinit var whooshSound: Sound
        lateinit var windingSound: Sound
    }
    private lateinit var tweenManager: TweenManager

    override fun create() {
        context.register {
            bindSingleton(TweenManager())
        }

        tweenManager = context.inject()
        Tween.registerAccessor(Actor::class.java, ActorAccessor())

        enableKtxCoroutines(asynchronousExecutorConcurrencyLevel = 1)

        val assetStorage = AssetStorage()
        ktxAsync {
            // Load stuff
            assetStorage.apply {
                val defaultSkin = load<Skin>("flatearthui/flat-earth-ui.json")
                xIcon = load("x.png")
                whooshSound = load("whoosh.wav")
                windingSound = load("winding.wav")

                val generator = FreeTypeFontGenerator(Gdx.files.internal("Roboto-Regular.ttf"))
                val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                    size = Math.ceil(20 * (Gdx.graphics.width / 1280.0)).toInt()
                }

                val font = generator.generateFont(parameter)

                defaultSkin.get(TextField.TextFieldStyle::class.java).font = font
                defaultSkin.get(TextButton.TextButtonStyle::class.java).font = font

                generator.dispose()

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

    override fun dispose() {
        xIcon.dispose()
        whooshSound.dispose()
        windingSound.dispose()
    }
}