package hu.pilota.ballistic

/**
 * Created by zsemberi.daniel on 2017. 10. 07..
 */

/**
 * @param x The distance at which the height is needed
 * @param angle The angle with which the projectile was thrown in RADIANS
 * @param v0 The starting velocity
 * @param g The gravity
 */
fun heightAtDistance(x: Double, angle: Double, v0: Double, g: Double = 9.81): Float =
        (x * Math.tan(angle) - (g * x * x) / (2 * Math.pow(v0 * Math.cos(angle), 2.0))).toFloat()