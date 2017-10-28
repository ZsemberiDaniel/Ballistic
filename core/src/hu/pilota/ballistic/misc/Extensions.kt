package hu.pilota.ballistic.misc

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor

val epsilon = 1.401298E-45f

inline fun Float.clamp(min: Float, max: Float): Float =
        if (this < min) { min } else if (this > max) { max } else { this }

inline fun Float.clamp01(): Float =
        if (this < 0f) { 0f } else if (this > 1f) { 1f } else { this }

inline fun Int.clamp(min: Int, max: Int): Int =
        if (this < min) { min } else if (this > max) { max } else { this }

inline fun Float.approximatly(value: Float): Boolean =
    Math.abs(this - value) <= epsilon


inline fun Actor.size(): Vector2 = Vector2(width, height)
inline fun Actor.pos(): Vector2 = Vector2(x, y)