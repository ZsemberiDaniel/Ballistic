package hu.pilota.ballistic

import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter


class DigitFilter : TextFieldFilter {

    private val accepted: CharArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.')

    override fun acceptChar(textField: TextField, c: Char): Boolean {
        return accepted.contains(c)
    }
}