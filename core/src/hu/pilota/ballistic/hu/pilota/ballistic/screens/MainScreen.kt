package hu.pilota.ballistic.hu.pilota.ballistic.screens

import aurelienribon.tweenengine.Timeline
import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenAccessor
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Timer
import hu.pilota.ballistic.*
import ktx.actors.alpha
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

private val ANIM_SPEED: Float = 1.7f

class MainScreen(val myGame: MyGame) : KtxScreen, KtxInputAdapter {

    companion object {
        private val groundColor = Color(0.14902f, 0.19608f, 0.21961f, 1f)
        private val groundLineCount = 40
        private val groundLineWidth = 3f

        private val graphColor1 = Color(0.95686f, 0.26275f, 0.21176f, 1f)
        private val graphColor2 = Color(0.24706f, 0.31765f, 0.7098f, 1f)
        private val graphLineWidth = 4f
    }
        // Szia Zsemberi! :-) Feri a kódodban járt
    private val spriteBatch: SpriteBatch = SpriteBatch()
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    private val submitButton: TextButton = TextButton("Shoot", context.inject<Skin>()).apply {
        setSize(Gdx.graphics.width * 0.1f, Gdx.graphics.height * 0.07f)
        setPosition(Gdx.graphics.width - width, Gdx.graphics.height - height)
        onClick {
            // Set the values for second phase
            secondPhase.startHorizontalRadius = horizontalSlider.knobMinMaxDifference / 2f
            secondPhase.horizontalRadius = secondPhase.startHorizontalRadius
            secondPhase.startVerticalRadius = verticalSlider.knobMinMaxDifference / 2f
            secondPhase.verticalRadius = secondPhase.startVerticalRadius
            isSecondPhase = true

            // if at very bottom we want the radius to be at least half the screen
            val horizontalEndScale = Math.max(horizontalSlider.knobMinMaxDifference / 2f,
                    horizontalSlider.knobMidValue) / secondPhase.horizontalRadius
            val verticalEndScale = Math.max(verticalSlider.knobMinMaxDifference / 2f,
                    verticalSlider.knobMidValue) / secondPhase.verticalRadius
            val scale = Math.max(horizontalEndScale, verticalEndScale)


            Timeline.createSequence()
                    .push(Tween.call({_, _ ->
                        backButton.isVisible = true
                    }))
                    .beginParallel()
                        // fade all GUI
                        .push(Tween.to(verticalSlider, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(0f))
                        .push(Tween.to(horizontalSlider, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(0f))
                        .push(Tween.to(settingsTable, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(0f))
                        .push(Tween.to(backButton, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(1f))

                        // move the graph
                        .push(Tween.to(secondPhase, AnimationClassAccessor.HORIZONTAL_RADIUS, 1f / ANIM_SPEED)
                                .target(scale * secondPhase.startHorizontalRadius))
                        .push(Tween.to(secondPhase, AnimationClassAccessor.VERTICAL_RADIUS, 1f / ANIM_SPEED)
                                .target(scale * secondPhase.startVerticalRadius))
                    .end()
                    .push(Tween.call({ _, _ ->
                        // Disable sliders
                        settingsTable.isVisible = false
                        horizontalSlider.isVisible = false
                        verticalSlider.isVisible = false

                        Timer().scheduleTask(object : Timer.Task() {
                            override fun run() {
                                dataDrawProgression += 0.02f
                            }
                        }, 0f, 0.01f, 50)
                    }))

                    .start(context.inject())

            MyGame.whooshSound.play()
        }
    }
    private val backButton: TextButton = TextButton("Back", context.inject<Skin>()).apply {
        setPosition(Gdx.graphics.width - width, Gdx.graphics.height - height)
        isVisible = false
        alpha = 0f

        // onclick moved to show() because even though backButton is inited by the time it gets here
        // it thinks it has not been initialized....
    }

    private val shootAtTexture = MyGame.xIcon

    private val sliderSize = Gdx.graphics.height * 0.05f
    private val verticalSlider = ExpandingGridSlider(ExpandingSlider.LayoutType.VERTICAL, context.inject(), 10)
            .apply {
                setSize(sliderSize, Gdx.graphics.height.toFloat() - sliderSize)
                setPosition(0f, sliderSize)
                setSliderSettings(ExpandingSlider.ExpandingSliderSettings(maxValue = 10_000f, minValue = 250f, knobMinMaxDiff = 500f))
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
        add(submitButton).colspan(2).size(Gdx.graphics.width * 0.1f, submitButton.height).align(Align.right)

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

        addActor(backButton)
    }

    private var isSecondPhase = false
    private var dataDrawProgression = 0f

    private val secondPhase = AnimationClass()


    override fun show() {
        Gdx.input.inputProcessor = stage

        backButton.onClick {
            Timeline.createSequence()
                    .push(Tween.call({_, _ ->
                        settingsTable.isVisible = true
                        horizontalSlider.isVisible = true
                        verticalSlider.isVisible = true

                        dataDrawProgression = 0f
                    }))
                    // move the graph
                    .beginParallel()
                        .push(Tween.to(secondPhase, AnimationClassAccessor.HORIZONTAL_RADIUS, 1f / ANIM_SPEED)
                                .target(secondPhase.startHorizontalRadius))
                        .push(Tween.to(secondPhase, AnimationClassAccessor.VERTICAL_RADIUS, 1f / ANIM_SPEED)
                                .target(secondPhase.startVerticalRadius))
                    .end()
                    // fade all GUI
                    .beginParallel()
                        .push(Tween.to(verticalSlider, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(1f))
                        .push(Tween.to(horizontalSlider, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(1f))
                        .push(Tween.to(settingsTable, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(1f))
                        .push(Tween.to(backButton, ActorAccessor.ALPHA, 0.5f / ANIM_SPEED).target(0f))
                    .end()
                    .push(Tween.call({ _, _ ->
                        backButton.isVisible = false

                        isSecondPhase = false
                    }))

                    .start(context.inject())

            MyGame.whooshSound.play()
        }
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
            drawSecondPhaseBackground()

            drawArcSecondPhase(alpha1, v0, g, graphLineWidth, graphColor1)
            drawArcSecondPhase(alpha2, v0, g, graphLineWidth, graphColor2)
        } else {
            drawArc(alpha1, v0, g, graphLineWidth, graphColor1)
            drawArc(alpha2, v0, g, graphLineWidth, graphColor2)
        }

        // draw stage
        stage.act(delta)
        stage.draw()
    }

    private fun drawSecondPhaseBackground() {
        if (dataDrawProgression <= 0f) return

        shapeRenderer.apply {
            begin(ShapeRenderer.ShapeType.Line)
            Gdx.gl.glLineWidth(groundLineWidth)

            color = groundColor
            line(0f, sliderSize, Gdx.graphics.width.toFloat() * dataDrawProgression, sliderSize)

            val currLineCount = (groundLineCount * dataDrawProgression).toInt()
            for (i in 0..currLineCount) {
                line(Gdx.graphics.width.toFloat() / groundLineCount * i, 0f,
                        Gdx.graphics.width.toFloat() / groundLineCount * (i + 1), sliderSize)
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
    private fun drawArc(angle: Double, v0: Double, g: Double, lineWidth: Float, color: Color) {
        shapeRenderer.apply {
            begin(ShapeRenderer.ShapeType.Line)
                Gdx.gl.glLineWidth(lineWidth)
                this.color = color

                // Drawing the line based on the horizontal slider's values
                var i = horizontalSlider.knobMinValue // where we at with the drawing currently (what meter)
                var lastPoint = Vector2( // which point was drawn last
                        horizontalSlider.getPointInner(0f).x,
                        verticalSlider.getPointInner(
                                (heightAtDistance(i.toDouble(), angle, v0, g) - verticalSlider.knobMinValue) / verticalSlider.knobMinMaxDifference).y
                )
                val newPoint = Vector2() // which point will be drawn now

                while (i <= horizontalSlider.knobMaxValue) {
                    newPoint.x = horizontalSlider.getPointInner(
                            (i - horizontalSlider.knobMinValue) / horizontalSlider.knobMinMaxDifference).x
                    newPoint.y = verticalSlider.getPointInner(
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
        val graphWidth = Gdx.graphics.width - sliderSize
        val graphHeight = Gdx.graphics.height - sliderSize

        shapeRenderer.apply {
            begin(ShapeRenderer.ShapeType.Line)

            Gdx.gl.glLineWidth(lineWidth)
            this.color = color

            // Drawing the line based on the horizontal slider's values
            var i = -secondPhase.horizontalRadius // where we at with the drawing currently (what meter)

            // We don't want to go negative distance
            if (horizontalSlider.knobMidValue + i <= 0)
                i = -horizontalSlider.knobMidValue

            // we are only interested in the arc above the earth so we need to store the height of the arc
            var height = heightAtDistance((horizontalSlider.knobMidValue + i).toDouble(), angle, v0, g)

            var lastPoint = Vector2( // which point was drawn last
                    sliderSize + (i + secondPhase.horizontalRadius) / (secondPhase.horizontalRadius * 2f) * graphWidth,
                    sliderSize + (height - verticalSlider.knobMidValue + secondPhase.verticalRadius)
                            / (2 * secondPhase.verticalRadius) * graphHeight
            )
            val newPoint = Vector2() // which point will be drawn now

            while (i <= secondPhase.horizontalRadius && height >= 0) {
                height = heightAtDistance((horizontalSlider.knobMidValue + i).toDouble(), angle, v0, g)

                newPoint.x = sliderSize + (i + secondPhase.horizontalRadius) / (secondPhase.horizontalRadius * 2f) * graphWidth
                newPoint.y = sliderSize + (height - verticalSlider.knobMidValue + secondPhase.verticalRadius) /
                        (2 * secondPhase.verticalRadius) * graphHeight

                line(lastPoint, newPoint)

                i += 0.5f
                lastPoint = Vector2(newPoint)
            }


            // drawing the other data
            if (dataDrawProgression > 0f) {
                val newAngleDist = 0.1
                val newAngle = Math.atan((heightAtDistance(newAngleDist, angle, v0, g)) / newAngleDist)

                arrowAngle(sliderSize, sliderSize, newAngle.toFloat() * 100f,
                        (angle * dataDrawProgression).toFloat(), 50)
            }

            end()
        }
    }

    /**
     * Draws and arrow with and arc
     * @param x Where the arrow's start should be
     * @param y Where the arrow's start should be
     * @param radius How long the arc should be. The arrow is a bit bigger
     * @param angle How big of an angle the arc should have (Compared to a vector pointing to the right)
     * @param segments How many segments the arc should have
     */
    private fun ShapeRenderer.arrowAngle(x: Float, y: Float, radius: Float, angle: Float, segments: Int = 50) {
        // angle
        arc(x, y, radius, 0f, Math.toDegrees(angle.toDouble()).toFloat(), segments)

        // ARROW
        val arrowEnd = Vector2(x + Math.cos(angle.toDouble()).toFloat() * radius * 1.5f,
                y + Math.abs(Math.sin(angle.toDouble())).toFloat() * radius * 1.5f)
        line(x, y, arrowEnd.x, arrowEnd.y)

        val normalArrow = Vector2(-(arrowEnd.y - y), arrowEnd.x - x).nor()
        // on the arrow where the triangle's 90° projection ends
        val triangleEnd = Vector2(arrowEnd.x - x, arrowEnd.y - y) * 0.8f
        // how much the triangleEnd point needs to be shifted to get the arrow look
        val endShift = normalArrow * ((triangleEnd.len() / 8f) * Math.tan(Math.toRadians(30.0))).toFloat()

        // the two sides of the arrow's end
        val rightSide = Vector2(x, y) + triangleEnd + endShift
        val leftSide = Vector2(x, y) + triangleEnd - endShift

        // drawing the end
        line(arrowEnd, rightSide)
        line(arrowEnd, leftSide)
    }

    override fun dispose() {
        stage.dispose()

        spriteBatch.dispose()
        shootAtTexture.dispose()
    }

    private data class AnimationClass(val zoomOutScale: Float = 500f,
                                      var startHorizontalRadius: Float = 0f, var horizontalRadius: Float = 0f,
                                      var startVerticalRadius: Float = 0f, var verticalRadius: Float = 0f) {
        companion object {
            init {
                Tween.registerAccessor(AnimationClass::class.java, AnimationClassAccessor())
            }
        }
    }
    private class AnimationClassAccessor : TweenAccessor<AnimationClass> {

        companion object {
            val HORIZONTAL_RADIUS = 1
            val VERTICAL_RADIUS = 2
        }

        override fun getValues(target: AnimationClass?, tweenType: Int, returnValues: FloatArray?): Int {
            if (target == null || returnValues == null) return 0

            return when (tweenType) {
                HORIZONTAL_RADIUS -> {
                    returnValues[0] = target.horizontalRadius
                    1
                }
                VERTICAL_RADIUS -> {
                    returnValues[0] = target.verticalRadius
                    1
                }
                else -> 0
            }
        }

        override fun setValues(target: AnimationClass?, tweenType: Int, newValues: FloatArray?) {
            if (target == null || newValues == null) return

            when (tweenType) {
                HORIZONTAL_RADIUS -> {
                    target.horizontalRadius = newValues[0]
                }
                VERTICAL_RADIUS -> {
                    target.verticalRadius = newValues[0]
                }
            }
        }

    }
}