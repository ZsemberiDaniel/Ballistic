package hu.pilota.ballistic.tween

import aurelienribon.tweenengine.TweenAccessor
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.actors.alpha

/**
 * Created by zsemberi.daniel on 2017. 10. 21..
 */

class ActorAccessor : TweenAccessor<Actor> {

    companion object {
        val ALPHA = 1
    }

    override fun setValues(target: Actor?, tweenType: Int, newValues: FloatArray?) {
        if (target == null || newValues == null) return

        when (tweenType) {
            ALPHA -> target.alpha = newValues[0]
        }
    }

    override fun getValues(target: Actor?, tweenType: Int, returnValues: FloatArray?): Int {
        if (target == null || returnValues == null) return 0

        return when (tweenType) {
            ALPHA -> {
                returnValues[0] = target.alpha
                1
            }
            else -> 0
        }
    }

}