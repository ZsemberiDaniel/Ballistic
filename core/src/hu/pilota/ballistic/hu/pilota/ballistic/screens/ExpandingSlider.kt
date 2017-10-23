package hu.pilota.ballistic.hu.pilota.ballistic.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import hu.pilota.ballistic.*
import ktx.actors.alpha


/**
 * From what point should the expanding happen. It means both ways
 */
internal val SCROLL_FROM: Float = 0.02f

open class ExpandingSlider(private val layoutType: LayoutType,
                           private val style: Slider.SliderStyle,
                           textfieldStyle: TextField.TextFieldStyle) : Widget() {

    constructor(layoutType: LayoutType, skin: Skin) :
            this(layoutType,
                 skin.get("default-${layoutType.toString().toLowerCase()}", Slider.SliderStyle::class.java),
                 skin.get(TextField.TextFieldStyle::class.java))

    private val valueChangeListeners: MutableList<ChangeListener> = mutableListOf()
    fun onValueChange(listener: (newValue: Float) -> Unit): ChangeListener {
        val changeListener = object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                listener(realValue)
            }
        }

        valueChangeListeners.add(changeListener)
        return changeListener
    }

    protected var settings: ExpandingSliderSettings = ExpandingSliderSettings()
    /**
     * Sets the settings of this ExpandingSlider. If the parameter is null the default settings will be set
     */
    open fun setSliderSettings(settings: ExpandingSliderSettings?) {
        if (settings == null)
            this.settings = ExpandingSliderSettings()
        else
            this.settings = settings
    }


    /**
     * At what interval the dragging click sound can be played in milliseconds
     */
    protected val clickSoundInterval = 90L
    /**
     * When the next clicking sound can be played in system's millisecond time
     */
    private var nextClickSoundAt = 0L


    /**
     * The 'real' value of this slider
     */
    val realValue
        get() = knobMinValue + knobPercent / (1f - 2f * SCROLL_FROM) * settings.knobMinMaxDiff

    /**
     * If the knob is at the very start what value the real value should be
     */
    var knobMinValue = 300f
        protected set(value) {
            field = value

            // call change listeners
            valueChangeListeners.forEach {
                it.changed(ChangeListener.ChangeEvent(), this)
            }
        }
    /**
     * If the knob is at the very end what value the real value should be
     */
    val knobMaxValue
        get() = knobMinValue + settings.knobMinMaxDiff
    /**
     * If the knob is in the middle what the real value should be
     */
    val knobMidValue
        get() = knobMinValue + knobMinMaxDifference / 2f
    /**
     * The difference between the current min and max of the knob
     */
    val knobMinMaxDifference
        get() = settings.knobMinMaxDiff
    /**
     * The knobs position in percent. Clamped to [SCROLL_FROM, 1f - SCROLL_FROM]
     */
    protected var knobPercent = 0.5f
        private set(value) {
            field = value.clamp(SCROLL_FROM, 1f - SCROLL_FROM)

            valueChangeListeners.forEach {
                it.changed(ChangeListener.ChangeEvent(), this)
            }
        }
    /**
     * Is the knob being dragged at the moment
     */
    protected var isDragging = false

    private val knobDrawable: Drawable
        get() = if (isDragging) { style.knobDown } else { style.knob }


    private val textField: TextField = TextField(realValue.toString(), textfieldStyle)

    init {
        addListener(ExpandingSliderClickListener())
    }


    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (batch == null || !isVisible) return

        val background = style.background

        val knob = knobDrawable
        val knobPos = getKnobPosition()

        batch.color = Color(1f, 1f, 1f, alpha)
        background.draw(batch, x, y, width, height)

        val knobSize = Math.min(width, height)
        knob.draw(batch, knobPos.x - knobSize / 2, knobPos.y - knobSize / 2, knobSize, knobSize)

        textField.draw(batch, alpha)
        batch.color = Color(1f, 1f, 1f, 1f)
    }

    override fun act(delta: Float) {
        if (!isVisible) return

        if (isDragging) {
            when {
                // if at start decrease the real value
                knobPercent <= SCROLL_FROM -> {
                    knobMinValue -= delta * getCurrentExpansion()

                    if (knobMinValue > settings.minValue)
                        playClickSound()
                }
                // if at and increase the real value
                knobPercent >= 1f - SCROLL_FROM -> {
                    knobMinValue += delta * getCurrentExpansion()

                    if (knobMaxValue < settings.maxValue)
                        playClickSound()
                }
            }

            // we are over the limit so we need to set everything back
            if (knobMaxValue > settings.maxValue) {
                knobMinValue = settings.maxValue - settings.knobMinMaxDiff

                // set it a bit smaller so it doesn't end up in an infinite loop of expanding
                knobPercent = 1f - SCROLL_FROM - 0.001f
            // we are under the limit so we need to set everything back
            } else if (knobMinValue < settings.minValue) {
                knobMinValue = settings.minValue

                knobPercent = 0f
            }
        }

        // Update the text field
        val textFieldPos = getKnobPosition() + layoutType.getNormal() * (size() / 2f)
        textFieldPos.x = textFieldPos.x.clamp(0f, Gdx.graphics.width.toFloat() - textField.width)
        textFieldPos.y = textFieldPos.y.clamp(0f, Gdx.graphics.height.toFloat() - textField.height)

        textField.setPosition(textFieldPos.x, textFieldPos.y)
        textField.text = realValue.toString()
    }

    /**
     * Plays the dragging click sound if possible
     */
    private fun playClickSound() {
        if (System.currentTimeMillis() >= nextClickSoundAt) {
            nextClickSoundAt = System.currentTimeMillis() + clickSoundInterval

            MyGame.windingSound.play()
        }
    }

    /**
     * Returns how much the slider should expand per second at the current moment
     */
    private fun getCurrentExpansion(): Float = (if (realValue <= 800f) { Math.pow(realValue.toDouble(), 2.4).toFloat() }
                                                else { realValue * realValue }) * 0.0001f * settings.expandRate

    /**
     * Returns a point on this slider
     * @param t Which point to return. Will be clamped to [SCROLL_FROM; 1f - SCROLL_FROM]
     * @return A point on this slider at t
     */
    fun getPointAtClamped(t: Float): Vector2 =
        layoutType.getProgressPoint(t.clamp(SCROLL_FROM, 1f - SCROLL_FROM), pos(), size())

    /**
     * Returns a point on this slider in range [0;1]
     */
    fun getPointAt(t: Float): Vector2 =
            layoutType.getProgressPoint(t.clamp01(), pos(), size())

    /**
     * Returns a point based on the slider's position. If t is >= 0 and <= 1 then it's just a point on this slider
     * Otherwise it returns a linearly interpolated point outside of the slider
     */
    fun getPointAtFreely(t: Float): Vector2 =
            layoutType.getProgressPoint(t, pos(), size())

    /**
     * Returns a point on this slider.
     * @param t Value which represents where the point is on the slider between the two SCROLL_FROM ends
     *          Will be clamped to 0;1
     */
    fun getPointInner(t: Float): Vector2 =
            layoutType.getProgressPoint(t.clamp01() * (1f - 2 * SCROLL_FROM) + SCROLL_FROM, pos(), size())


    /**
     * Returns the position of the knob
     */
    fun getKnobPosition(): Vector2 = getPointAtClamped(knobPercent)

    /**
     * @param knobMinMaxDiff The difference between the max and min value of the current slider (between the ends)
     * @param expandRate The rate of expansion.
     */
    data class ExpandingSliderSettings(val knobMinMaxDiff: Float = 500f, val expandRate: Float = 1f,
                                       val maxValue: Float = 200_000f, val minValue: Float = 200f)

    inner class ExpandingSliderClickListener : ClickListener() {
        override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            return isVisible
        }

        override fun touchDragged(event: InputEvent?, posX: Float, posY: Float, pointer: Int) {
            if (!isVisible) return

            isDragging = true

            // play sound
            playClickSound()

            if (layoutType == LayoutType.HORIZONTAL) {
                knobPercent = ((posX - x) / width)
            } else if (layoutType == LayoutType.VERTICAL) {
                knobPercent = ((posY - y) / height)
            }
        }

        override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
            isDragging = false
        }
    }

    enum class LayoutType {
        HORIZONTAL {
            private val normalVector = Vector2(0f, 1f)
            private val parallelVector = Vector2(1f, 0f)

            override fun getProgressPoint(t: Float, pos: Vector2, size: Vector2): Vector2 {
                return pos + Vector2(size.x * t, size.y * 0.5f)
            }

            override fun getNormal(): Vector2 = normalVector
            override fun getParallel(): Vector2 = parallelVector
        }, VERTICAL {
            private val normalVector = Vector2(1f, 0f)
            private val parallelVector = Vector2(0f, 1f)

            override fun getProgressPoint(t: Float, pos: Vector2, size: Vector2): Vector2 {
                return pos + Vector2(size.x * 0.5f, size.y * t)
            }

            override fun getNormal(): Vector2 = normalVector
            override fun getParallel(): Vector2 = parallelVector
        };
        /**
         * Returns the screen point of t with the correct layout type.
         * Takes SCROLL_FROM into consideration
         * @param t Will not be clamped so may return an out of screen point as well
         * @param pos The position of the slider
         * @param size The size of the slider
         */
        abstract fun getProgressPoint(t: Float, pos: Vector2, size: Vector2): Vector2

        /**
         * @return The vector which is perpendicular to the slider. It's length is 1.
         */
        abstract fun getNormal(): Vector2

        /**
         * @return The vector which is parallel to the slider. It's length is 1.
         */
        abstract fun getParallel(): Vector2
    }
}