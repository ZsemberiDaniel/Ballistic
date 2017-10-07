package hu.pilota.ballistic

import com.badlogic.gdx.math.Vector2

/**
 * Constructs a new [Vector2] instance. Basically a "copy" of [Vector2] constructor that supports Kotlin syntax features:
 * named parameters with default values.
 * @param x the X component. Defaults to 0f.
 * @param y the Y component. Defaults to 0f.
 * @return a new [Vector2] instance storing the passed values.
 */
fun vec2(x: Float = 0f, y: Float = 0f): Vector2 = Vector2(x, y)

/**
 * Inverts currently stored X and Y values.
 * @return this vector for chaining.
 */
operator fun Vector2.unaryMinus(): Vector2 = Vector2(-x, -y)

/**
 * @param vector2 values from this vector will be added to this vector.
 * @return this vector for chaining.
 */
operator fun Vector2.plus(vector2: Vector2): Vector2 = Vector2(x + vector2.x, y + vector2.y)

/**
 * @param vector2 values from this vector will be subtracted from this vector.
 * @return this vector for chaining.
 */
operator fun Vector2.minus(vector2: Vector2): Vector2 =  Vector2(x - vector2.x, y - vector2.y)

/**
 * @param vector2 values from this vector will multiply this vector.
 * @return this vector for chaining.
 */
operator fun Vector2.times(vector2: Vector2): Vector2 =  Vector2(x * vector2.x, y * vector2.y)

/**
 * @param vector2 values from this vector will divide this vector.
 * @return this vector for chaining.
 */
operator fun Vector2.div(vector2: Vector2): Vector2 =  Vector2(x / vector2.x, y / vector2.y)

/**
 * @param scalar will be used to multiply both vector values.
 * @return this vector for chaining.
 */
operator fun Vector2.times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)

/**
 * @param scalar will be used to divide both vector values.
 * @return this vector for chaining.
 */
operator fun Vector2.div(scalar: Float): Vector2 = Vector2(x / scalar, y / scalar)

/**
 * @param scalar will be used to multiply both vector values.
 * @return this vector for chaining.
 */
operator fun Vector2.times(scalar: Int): Vector2 = Vector2(x * scalar, y * scalar)

/**
 * @param scalar will be used to divide both vector values.
 * @return this vector for chaining.
 */
operator fun Vector2.div(scalar: Int): Vector2 = Vector2(x / scalar, y / scalar)

/**
 * Increments both vector values - adds 1 to x and y. Note that since [Vector2] is mutable and its instances are usually
 * reused, this operation DOES NOT create a new [Vector2] or keep its previous state. That means that both vector++ and
 * ++vector have the same effect and both will modify the internal state of the vector.
 * @return this vector for chaining.
 */
operator fun Vector2.inc(): Vector2 = this.add(1f, 1f)

/**
 * Decrements both vector values - subtracts 1 from x and y. Note that since [Vector2] is mutable and its instances are
 * usually reused, this operation DOES NOT create a new [Vector2] or keep its previous state. That means that both
 * vector-- and --vector have the same effect and both will modify the internal state of the vector.
 * @return this vector for chaining.
 */
operator fun Vector2.dec(): Vector2 = this.sub(1f, 1f)

/**
 * Operator function that allows to deconstruct this vector.
 * @return X component.
 */
operator fun Vector2.component1(): Float = this.x

/**
 * Operator function that allows to deconstruct this vector.
 * @return Y component.
 */
operator fun Vector2.component2(): Float = this.y

/**
 * Allows to compare which [Vector2] has greater overall length.
 * @param vector2 will be compared to this vector.
 * @return 1 if this vector has greater length. 0 if vectors are equal. -1 if the other vector is greater.
 */
operator fun Vector2.compareTo(vector2: Vector2): Int = this.len2().compareTo(vector2.len2())