package org.usfirst.frc.team4322.math

class Translation(val x : Double, val y : Double) : Interpolable<Translation> {

    companion object {
        val identity: Translation = Translation(0.0, 0.0)
        fun dot(a: Translation, b: Translation): Double {
            return a.x * b.x + a.y * b.y
        }

        fun getAngle(a: Translation, b: Translation): Rotation {
            val cos_angle = dot(a, b) / (a.norml2() * b.norml2())
            return if (java.lang.Double.isNaN(cos_angle)) {
                Rotation()
            } else Rotation.fromRadians(Math.acos(Math.min(1.0, Math.max(cos_angle, -1.0))))
        }

        fun cross(a: Translation, b: Translation): Double {
            return a.x * b.y - a.y * b.x
        }

    }

    constructor() : this(0.0,0.0)

    constructor(other : Translation) : this(other.x,other.y)

    constructor(start : Translation, end : Translation) : this(end.x-start.x,end.y-start.y)

    fun norml2() : Double {
        return Math.hypot(x,y)
    }

    fun norml1() : Double {
        return x*x + y*y
    }

    operator fun plus(other : Translation) : Translation {
        return Translation(other.x + x, other.y + y)
    }

    fun rotateBy(rotation : Rotation) : Translation {
        return Translation(x * rotation.cos - y * rotation.sin, x * rotation.sin + y * rotation.cos)
    }

    fun inverse(): Translation {
        return Translation(-x, -y)
    }

    fun direction() : Rotation {
        return Rotation(x, y, true)
    }

    override fun lerp(other : Translation, x : Double) : Translation {
        return if (x < 0.0) {
            Translation(this)
        }
        else if (x > 1) {
            Translation(other)
        }
        else
        {
            Translation(x * (other.x - x) + x, x * (other.y - y) + y)
        }
    }

    fun scale(s: Double): Translation {
        return Translation(x * s, y * s)
    }
}