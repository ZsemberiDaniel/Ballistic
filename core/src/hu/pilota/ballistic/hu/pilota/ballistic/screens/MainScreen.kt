package hu.pilota.ballistic.hu.pilota.ballistic.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import hu.pilota.ballistic.DigitFilter
import hu.pilota.ballistic.context
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

class MainScreen : KtxScreen, KtxInputAdapter {
    // Text inputs
    private val heightInput = TextField("200", context.inject<Skin>()).apply {
        textFieldFilter = DigitFilter()
    }
    private val distanceInput = TextField("200", context.inject<Skin>())
    private val startingSpeedInput = TextField("20", context.inject<Skin>())

    private val submitButton = Button(context.inject<Skin>())

    // Main text input table
    private val inputTable = Table(context.inject()).apply {
        add(Label("Height: ", skin), heightInput)
        row()
        add(Label("Distance: ", skin), distanceInput)
        row()
        add(Label("Starting speed: ", skin), startingSpeedInput)
        row()
        add(submitButton)

        setFillParent(true)
    }

    // Renderer
    private val stage = Stage().apply {
        val sliderSize = Gdx.graphics.width * 0.02f

        // addActor(inputTable)
        addActor(ExpandingGridSlider(ExpandingSlider.LayoutType.VERTICAL, context.inject(), 10).apply {
            setSize(sliderSize, Gdx.graphics.height.toFloat() - sliderSize)
            setPosition(0f, sliderSize)
            setSliderSettings(ExpandingSlider.ExpandingSliderSettings(maxValue = 10_000f, minValue = 250f))
        })

        addActor(ExpandingGridSlider(ExpandingSlider.LayoutType.HORIZONTAL, context.inject(), 10).apply {
            setSize(Gdx.graphics.width.toFloat() - sliderSize, sliderSize)
            setPosition(sliderSize, 0f)
            setSliderSettings(ExpandingSlider.ExpandingSliderSettings(maxValue = 10_000f, minValue = 250f))
        })
    }

    override fun show() {
        Gdx.input.inputProcessor = stage

        submitButton.onClick {
            val g = 9.80665
            val y = heightInput.text.toDouble()
            val x = distanceInput.text.toDouble()
            val v = startingSpeedInput.text.toDouble()

            val c1 = v * v / (g * x)
            val c2 = Math.sqrt(Math.pow(v, 4.0) - (g * (g * x * x + 2 * y * v * v))) / (g * x)

            val alpha1 = Math.toDegrees(Math.atan(c1 - c2))
            val alpha2 = Math.toDegrees(Math.atan(c1 + c2))

            println("$alpha1 $alpha2")
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(255f, 255f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}