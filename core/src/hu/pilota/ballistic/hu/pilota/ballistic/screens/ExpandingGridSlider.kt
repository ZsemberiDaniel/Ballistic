package hu.pilota.ballistic.hu.pilota.ballistic.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import hu.pilota.ballistic.plus
import hu.pilota.ballistic.times
import ktx.actors.alpha


class ExpandingGridSlider(private val layoutType: LayoutType,
                          private val style: Slider.SliderStyle,
                          textfieldStyle: TextField.TextFieldStyle,
                          private val lineCount: Int = 5) : ExpandingSlider(layoutType, style, textfieldStyle) {

    constructor(layoutType: LayoutType, skin: Skin, lineCount: Int = 5) :
            this(layoutType,
                 skin.get("default-${layoutType.toString().toLowerCase()}", Slider.SliderStyle::class.java),
                 skin.get(TextField.TextFieldStyle::class.java),
                 lineCount)

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val linesAt = FloatArray(lineCount)
    private val lineDist = settings.knobMinMaxDiff / lineCount

    init {
        for (i in 0 until linesAt.size) {
            linesAt[i] = knobMinValue + i * lineDist
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (batch == null) return

        batch.end()
        Gdx.gl.glLineWidth(1f)
            shapeRenderer.apply {
                begin(ShapeRenderer.ShapeType.Line)
                alpha = 0.5f
                color = Color.GRAY

                val lineDist = settings.knobMinMaxDiff / lineCount

                // we need to reassign the line to a different value
                if (linesAt[0] < knobMinValue) {
                    for (k in 0 until linesAt.size) linesAt[k] = knobMaxValue - lineDist * (linesAt.size - k - 1)
                } else if (linesAt[linesAt.size - 1] > knobMaxValue) {
                    for (k in 0 until linesAt.size) linesAt[k] = knobMinValue + lineDist * k
                }

                for (i in 0 until linesAt.size) {
                    val fromPoint = getPointAt(SCROLL_FROM + (linesAt[i] - knobMinValue) / settings.knobMinMaxDiff * (1f - 2f * SCROLL_FROM))

                    shapeRenderer.line(fromPoint, fromPoint + layoutType.getNormal() * 1000)
                }


                end()
            }
        batch.begin()

        super.draw(batch, parentAlpha)
    }
}