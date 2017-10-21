package hu.pilota.ballistic.hu.pilota.ballistic.screens

import aurelienribon.tweenengine.Timeline
import aurelienribon.tweenengine.Tween
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import hu.pilota.ballistic.*
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import java.sql.Time

class MainScreen(val myGame: MyGame) : KtxScreen, KtxInputAdapter {
    private val spriteBatch: SpriteBatch = SpriteBatch()
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    private val submitButton: TextButton = TextButton("Shoot", context.inject<Skin>()).apply {
        setPosition(Gdx.graphics.width - width, Gdx.graphics.height - height)
    }.apply {
        onClick {
            Timeline.createParallel()
                    .push(Tween.to(verticalSlider, ActorAccessor.ALPHA, 0.5f).target(0f))
                    .push(Tween.to(horizontalSlider, ActorAccessor.ALPHA, 0.5f).target(0f))
                    .push(Tween.to(settingsTable, ActorAccessor.ALPHA, 0.5f).target(0f))
                    .start(myGame.tweenManager)

            secondPhase.horizontalRadius = horizontalSlider.knobMinMaxDifference / 2f
            secondPhase.verticalRadius = verticalSlider.knobMinMaxDifference / 2f
            isSecondPhase = true
        }
    }

    private val shootAtTexture = myGame.xIcon

    private val sliderSize = Gdx.graphics.height * 0.05f
    private val verticalSlider = ExpandingGridSlider(ExpandingSlider.LayoutType.VERTICAL, context.inject(), 10)
            .apply {
                setSize(sliderSize, Gdx.graphics.height.toFloat() - sliderSize)
                setPosition(0f, sliderSize)
                setSliderSettings(ExpandingSlider.ExpandingSliderSettings(maxValue = 10_000f, minValue = 250f, knobMinMaxDiff = 500f))

                onValueChange { newValue ->
                    println(newValue)
                }
            }

    private val horizontalSlider = ExpandingGridSlider(ExpandingSlider.LayoutType.HORIZONTAL, context.inject(),
                Math.floor(10 * (Gdx.graphics.width.toFloat() / Gdx.graphics.height).toDouble()).toInt())
            .apply {
                setSize(Gdx.graphics.width.toFloat() - sliderSize, sliderSize)
                setPosition(sliderSize, 0f)
                setSliderSettings(ExpandingSlider.ExpandingSliderSettings(maxValue = 10_000f, minValue = 250f,
                        knobMinMaxDiff = 500f * (Gdx.graphics.width.toFloat() / Gdx.graphics.height)))
            }

    private val startingSpeedSlider = Slider(30f, 500f, 0.1f, false, context.inject<Skin>()).apply {
        value = 200f

        onChange {
            startingSpeedText.text = value.toString()
        }
    }
    private val startingSpeedText: TextField = TextField(startingSpeedSlider.value.toString(), context.inject<Skin>()).apply {
        onChange {
            val newVal = text.toDoubleOrNull()
            println(newVal)

            // couldn't parse the input
            if (newVal == null) {
                color = Color.RED
            } else {
                color = Color.WHITE

                startingSpeedSlider.value = newVal.toFloat()
            }
        }
    }

    private val settingsTable = Table().apply {
        defaults().apply {
            padTop(Gdx.graphics.height / 150f)
            padLeft(Gdx.graphics.width / 300f)
        }

        add(startingSpeedSlider).size(Gdx.graphics.width * 0.3f, submitButton.height)
        add(startingSpeedText).size(Gdx.graphics.width * 0.1f, submitButton.height)
        row()
        add(submitButton).colspan(2).align(Align.right)

        pack()
        setPosition(Gdx.graphics.width - width, Gdx.graphics.height - height)
    }

    private inline val v0
        get() = startingSpeedSlider.value.toDouble()

    private var g = 9.80665

    // Renderer
    private val stage = Stage().apply {
        addActor(verticalSlider)
        addActor(horizontalSlider)

        addActor(settingsTable)
    }

    private var isSecondPhase = false
    private val secondPhase = AnimationClass()


    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(255f, 255f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Drawing the arcs
        // Calculating the angle
        val y = verticalSlider.realValue
        val x = horizontalSlider.realValue

        val c1 = v0 * v0 / (g * x)
        val c2 = Math.sqrt(Math.pow(v0, 4.0) - (g * (g * x * x + 2 * y * v0 * v0))) / (g * x)

        val alpha1 = Math.atan(c1 - c2)
        val alpha2 = Math.atan(c1 + c2)

        if (isSecondPhase) {
            if (secondPhase.horizontalRadius < horizontalSlider.realValue)
                secondPhase.horizontalRadius += delta * 100
            if (secondPhase.verticalRadius < verticalSlider.realValue)
                secondPhase.verticalRadius += delta * 100

            println("${secondPhase.horizontalRadius} ${secondPhase.verticalRadius} ${horizontalSlider.knobMidValue}")

            drawArcSecondPhase(alpha1, v0, g, 6f, Color.ROYAL)
            drawArcSecondPhase(alpha2, v0, g, 6f, Color.FIREBRICK)
        } else {
            drawArc(alpha1, v0, g, 6f, Color.ROYAL)
            drawArc(alpha2, v0, g, 6f, Color.FIREBRICK)
        }

        // draw stage
        stage.act(delta)
        stage.draw()

        spriteBatch.apply {
            begin()

            /* val shootAtSize = Gdx.graphics.width * 0.03f
            draw(shootAtTexture,
                    horizontalSlider.getKnobPosition().x - shootAtSize / 2f,
                    verticalSlider.getKnobPosition().y - shootAtSize / 2f,
                    shootAtSize, shootAtSize)*/

            end()
        }
    }

    /**
     * Draws an arc with the MainScreen's ShapeRenderer
     * @param angle With which to draw the arc. Radians
     * @param v0 Starting speed of object
     * @param g Gravity
     * @param lineWidth With of the arc's line
     * @param color Color of the arc
     */
    private fun drawArc(angle: Double, v0: Double, g: Double, lineWidth: Float, color: Color) {
        shapeRenderer.apply {
            begin(ShapeRenderer.ShapeType.Line)
                Gdx.gl.glLineWidth(lineWidth)
                this.color = color

                // Drawing the line based on the horizontal slider's values
                var i = horizontalSlider.knobMinValue // where we at with the drawing currently (what meter)
                var lastPoint = Vector2( // which point was drawn last
                        horizontalSlider.getPointAtFreely(0f).x,
                        verticalSlider.getPointAtFreely(
                                (heightAtDistance(i.toDouble(), angle, v0, g) - verticalSlider.knobMinValue) / verticalSlider.knobMinMaxDifference).y
                )
                val newPoint = Vector2() // which point will be drawn now

                while (i <= horizontalSlider.knobMaxValue) {
                    newPoint.x = horizontalSlider.getPointAtFreely(
                            (i - horizontalSlider.knobMinValue) / horizontalSlider.knobMinMaxDifference).x
                    newPoint.y = verticalSlider.getPointAtFreely(
                            (heightAtDistance(i.toDouble(), angle, v0, g) - verticalSlider.knobMinValue) / verticalSlider.knobMinMaxDifference).y

                    if (!lastPoint.x.approximatly(newPoint.x) && !lastPoint.y.approximatly(newPoint.y))
                        line(lastPoint, newPoint)

                    i += 0.5f
                    lastPoint = Vector2(newPoint)
                }

            end()
        }
    }

    /**
     * Draws an arc with the MainScreen's ShapeRenderer
     * @param angle With which to draw the arc. Radians
     * @param v0 Starting speed of object
     * @param g Gravity
     * @param lineWidth With of the arc's line
     * @param color Color of the arc
     */
    private fun drawArcSecondPhase(angle: Double, v0: Double, g: Double, lineWidth: Float, color: Color) {
        shapeRenderer.apply {
            begin(ShapeRenderer.ShapeType.Line)
            Gdx.gl.glLineWidth(lineWidth)
            this.color = color

            // Drawing the line based on the horizontal slider's values
            var i = 0f // where we at with the drawing currently (what meter)
            var lastPoint = Vector2( // which point was drawn last
                    0f,
                    (heightAtDistance(i.toDouble(), angle, v0, g) - verticalSlider.knobMidValue + secondPhase.verticalRadius)
                            / (2 * secondPhase.verticalRadius) * Gdx.graphics.height
            )
            val newPoint = Vector2() // which point will be drawn now

            while (i <= secondPhase.horizontalRadius * 2f) {
                newPoint.x = i / (secondPhase.horizontalRadius * 2f) * Gdx.graphics.width
                newPoint.y = (heightAtDistance(i.toDouble(), angle, v0, g) - verticalSlider.knobMidValue + secondPhase.verticalRadius) /
                        (2 * secondPhase.verticalRadius) * Gdx.graphics.height

                line(lastPoint, newPoint)

                i += 0.5f
                lastPoint = Vector2(newPoint)
            }

            end()
        }
    }

    override fun dispose() {
        stage.dispose()

        spriteBatch.dispose()
        shootAtTexture.dispose()
    }

    private data class AnimationClass(var horizontalRadius: Float = 0f, var verticalRadius: Float = 0f)
}